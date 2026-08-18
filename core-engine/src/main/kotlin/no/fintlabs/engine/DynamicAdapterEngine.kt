package no.fintlabs.engine

import no.novari.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.data.ResourceStatus
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.engine.config.DynaEngineConfig
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.fintlabs.engine.util.EngineRandom
import no.fintlabs.library.ResourceFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
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
        for (resource in metadata) {
            val amountRange: IntRange = amountTierPolicy.getRange(resource.amountTier ?: AmountTier.UNKNOWN)
            val amount = amountRange.random()
            val generated = factory.create(resource.resource.resourceClass, amount)
            storage.addAllResources(resource, generated)
        }
        relations.relateDataset(metadata, setType = SetType.INITIAL)
    }

    fun generateDeltaSyncData(
        identifiers: Map<ResourceIdentifiers, IntRange>,
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>> {
        val deltaMetadataList = mutableListOf<ExpandedMetadata>()

        for (identifier in identifiers) {
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
                deltaStorage.addAllResources(
                    meta.key,
                    meta,
                    factory.create(meta.resource.resourceClass, amount),
                )
            }
        }
        relations.relateDataset(deltaMetadataList, SetType.DELTA)
        val fullList = getAllGeneratedResourcesForSetType(deltaMetadataList, SetType.DELTA)
        deltaStorage.purge()
        debugGenCapPercentage()
        return fullList
    }

    fun generateResourceWithSpecifiedFieldValue(
        identifiers: ResourceIdentifiers,
        fieldName: String,
        fieldValue: String,
        amount: Int
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>>? {
        val meta = metadata.getMetadataFor(identifiers)
            ?: throw IllegalArgumentException("No resource metadata found for ${identifiers.toKey()}")

        if (resourcesLeft() <= amount) {
            throw IllegalStateException("max amount of resources reached. ")
        }

        deltaStorage.addAllResources(
            meta.key,
            meta,
            factory.createWithSingleSpecifiedValue(
                meta.resource.resourceClass,
                fieldName,
                fieldValue,
                amount
            )
        )
        relations.relateDataset(mutableListOf(meta), SetType.DELTA)
        val resources = getAllGeneratedResourcesForSetType(mutableListOf(meta), SetType.DELTA)
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

            logger.trace("${i.key} : $setType now contains ${fullList[i]?.size} resources.")
        }
        return fullList
    }

    private fun resourcesLeft(): Int = maxGeneratedResources.get() - storage.totalCount()

    fun verifyResourceLimitNotReached(): Boolean {
        return resourcesLeft() > 0
    }

    fun getAllMetadata(): MutableList<ExpandedMetadata> {
        return metadata.getAllMetadata()
    }

    fun getMetadataFromIdentifier(identifiers: ResourceIdentifiers): ExpandedMetadata? =
        metadata.getMetadataFor(identifiers)

    fun purgeAllStoredResources() {
        deltaStorage.purge()
        storage.purge()
        logger.debug("Purged all stored resources")
        debugGenCapPercentage()
    }

    // Configuration Manipulation

    fun setMaxResources(amount: Int) = maxGeneratedResources.set(amount)

    fun resetMaxResources() = maxGeneratedResources.set(props.maxGeneratedResources)

    fun debugGenCapPercentage() =
        logger.debug("Percentage of max generated resources: ${generationCapacityPercentage()}")

    // Status Stuff

    private fun generationCapacityPercentage(): Double =
        (storage.totalCount().toDouble() / maxGeneratedResources.toDouble() * 100.0)

    fun resourceStatus(): ResourceStatus =
        ResourceStatus(
            metadataCount = metadata.getAllMetadata().size,
            totalResources = storage.totalCount(),
            percentageOfMaxGenerated = generationCapacityPercentage(),
            resourcesByKey = storage.countsByKey(),
            registeredCapabilities = metadata.getNamesOfCapabilities(),
        )

}