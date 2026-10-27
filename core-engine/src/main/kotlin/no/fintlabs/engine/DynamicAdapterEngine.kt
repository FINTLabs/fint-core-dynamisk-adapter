package no.fintlabs.engine

import no.novari.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.data.ResourceStatus
import no.fintlabs.contract.dto.SpecifiedResource
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.engine.config.DynaEngineConfig
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.fintlabs.engine.util.EngineRandom
import no.fintlabs.library.ResourceFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.Locale
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

@Component
class DynamicAdapterEngine(
    private val metadata: MetadataService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val factory: ResourceFactory,
    private val relations: RelationFactory,
    private val random: EngineRandom,
    private val props: DynaEngineConfig,
    private val eventHandler: EventHandler,
) {
    private val logger = LoggerFactory.getLogger(DynamicAdapterEngine::class.java)
    private val maxGeneratedResources = AtomicInteger(props.maxGeneratedResources)

    fun generateCapabilitiesForDomains(
        domains: List<String>
    ): MutableSet<AdapterCapability> {
        for (domain in domains) {
            metadata.generateMetadataFromDomain(domain)
        }
        return metadata.generateCapabilities()
    }

    fun getAllGeneratedCapabilities(): MutableSet<AdapterCapability> = metadata.getAllCapabilities()

    fun getAllGeneratedResources(
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>> =
        getAllGeneratedResourcesForSetType(metadata.getAllMetadata(), SetType.INITIAL)

    fun executeInitialDataset(
        amountTierPolicy: AmountTierPolicy,
        resetSeed: Boolean = true,
    ) {
        if (resetSeed) {
            factory.resetSeed()
            random.reset()
        }
        val metadata = metadata.getAllMetadata()
        var amountOfResources = 0

        for (resource in metadata) {
            val amountRange: IntRange = amountTierPolicy.getRange(resource.amountTier ?: AmountTier.UNKNOWN)
            val amount = amountRange.random()
            val generated = factory.create(resource.resource.resourceClass, amount)
            amountOfResources += amount
            storage.addAllResources(resource, generated)
        }
        logger.info("$amountOfResources resources generated.")
        relations.relateDataset(metadata, setType = SetType.INITIAL)
    }

    fun generateDeltaSyncData(
        identifiers: Map<ResourceIdentifiers, IntRange>,
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>> {
        val deltaMetadataList = mutableListOf<ExpandedMetadata>()
        var amountOfResources = 0

        for (identifier in identifiers) {
            println("generating delta data for ${identifier.key.toKey()}")
            val meta = metadata.getMetadataFor(identifier.key)
            if (meta == null) {
                logger.error("No resource metadata found for ${identifier.key}")
                continue
            } else {
                deltaMetadataList.add(meta)
                var amount = random.fromRange(identifier.value)
                val remaining = resourcesLeft()
                if (remaining <= amount) {
                    amount = remaining.coerceAtLeast(0)
                    logger.warn("max amount of resources reached. ")
                    if (amount == 0) {
                        logger.warn("No resources can be generated. (MAX REACHED)")
                        continue
                    }
                }
                amountOfResources += amount
                deltaStorage.addAllResources(
                    meta.key,
                    meta,
                    factory.create(meta.resource.resourceClass, amount),
                )
            }
        }
        logger.info("$amountOfResources new resources generated.")
        relations.relateDataset(deltaMetadataList, SetType.DELTA)
        val fullList = getAllGeneratedResourcesForSetType(deltaMetadataList, SetType.DELTA)
        mergeDeltaStorageToFull(fullList)
        deltaStorage.purge()
        debugGenCapPercentage()
        return fullList
    }

    fun executeEventRequest(
        event: RequestFintEvent,
    ): ResponseFintEvent = eventHandler.handle(event)

    fun generateResourceWithSpecifiedFieldValue(
        specifiedResources: List<SpecifiedResource>,
        supportingResources: List<String> = emptyList(),
        amount: Int
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>>? {
        val metaList = mutableListOf<ExpandedMetadata>()

        for (resource in specifiedResources) {
            val meta = metadata.getMetadataFromString(resource.resource)
                ?: throw IllegalArgumentException("No resource metadata found for ${resource.resource}")

            deltaStorage.addAllResources(
                meta.key,
                meta,
                factory.createWithSingleSpecifiedValue(
                    resource = meta.resource.resourceClass,
                    fieldName = resource.fieldName,
                    fieldValue = resource.fieldValue,
                    amount = amount
                )
            )
            metaList.add(meta)
        }
        for (resource in supportingResources) {
            val meta = metadata.getMetadataFromString(resource)
                ?: throw IllegalArgumentException("No resource metadata found for $resource")

            deltaStorage.addAllResources(
                meta.key,
                meta,
                factory.create(
                    resource = meta.resource.resourceClass,
                    amount = amount
                ),
            )
            metaList.add(meta)
        }

        relations.relateDataset(metaList, SetType.DELTA)
        val resources = getAllGeneratedResourcesForSetType(metaList, SetType.DELTA)
        mergeDeltaStorageToFull(resources)
        deltaStorage.purge()
        debugGenCapPercentage()
        return resources
    }

    private fun getAllGeneratedResourcesForSetType(
        metadataList: MutableList<ExpandedMetadata>,
        setType: SetType,
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>> {
        val fullList = ConcurrentHashMap<ExpandedMetadata, List<FintResource>>()

        for (i in metadataList) {

            fullList[i] =
                if (setType == SetType.DELTA)
                    deltaStorage.getAllResources(i.key)
                else storage.getAllResources(i.key)

            logger.trace("{} : {} now contains {} resources.", i.key, setType, fullList[i]?.size)
        }
        return fullList
    }

    fun setFullSyncInterval(interval: Int) = metadata.setFullSyncInterval(interval)

    // ----------
    //   STATUS
    // ----------

    private fun resourcesLeft(): Int = maxGeneratedResources.get() - storage.totalCount()

    fun verifyResourceLimitNotReached(): Boolean {
        return resourcesLeft() > 0
    }

    fun getAllMetadata(): MutableList<ExpandedMetadata> {
        return metadata.getAllMetadata()
    }

    fun getMetadataFromKey(key: String): ExpandedMetadata? = metadata.getMetadataFromString(key)

    fun getMetadataFromIdentifier(identifiers: ResourceIdentifiers): ExpandedMetadata? =
        metadata.getMetadataFor(identifiers)

    fun purgeAllStoredResources() {
        deltaStorage.purge()
        storage.purge()
        logger.info("Purged all stored resources")
        debugGenCapPercentage()
    }

    private fun mergeDeltaStorageToFull(collection: ConcurrentHashMap<ExpandedMetadata, List<FintResource>>) {
        for (resource in collection) {
            storage.addAllResources(resource.key, resource.value)
        }
    }

    // ------------------------------
    //   Configuration Manipulation
    // ------------------------------

    fun setMaxResources(amount: Int) = maxGeneratedResources.set(amount)

    fun resetMaxResources() = maxGeneratedResources.set(props.maxGeneratedResources)

    fun setSeed(newSeed: String? = null) {
        factory.setSeedValue(newSeed)
        random.setSeed(newSeed)
    }

    fun debugGenCapPercentage() =
        logger.debug("Percentage of max generated resources: ${generationCapacityPercentage()}")

    // ----------------
    //   Status Stuff
    // ----------------

    private fun generationCapacityPercentage(): String {
        val percentage = (storage.totalCount().toDouble() / maxGeneratedResources.toDouble() * 100.0).toInt()
        return "$percentage%"
    }

    private fun formatMaxGenerated(): String {
        return "%,d".format(Locale.GERMANY, maxGeneratedResources.get())
    }

    fun resourceStatus(): ResourceStatus =
        ResourceStatus(
            metadataCount = metadata.getAllMetadata().size,
            totalResources = storage.totalCount(),
            maxGeneratedResources = formatMaxGenerated(),
            percentageOfMaxGenerated = generationCapacityPercentage(),
            resourcesByKey = storage.countsByKey(),
            registeredCapabilities = metadata.getNamesOfCapabilities(),
        )
}