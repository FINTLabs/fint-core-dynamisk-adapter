package no.fintlabs.engine

import no.novari.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.fintlabs.engine.util.EngineRandom
import no.fintlabs.library.ResourceFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class DynamicAdapterEngine(
    private val metadata: MetadataService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val factory: ResourceFactory,
    private val relations: RelationFactory,
    private val random: EngineRandom,
) {
    private val logger = LoggerFactory.getLogger(DynamicAdapterEngine::class.java)

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
    ) {
        factory.resetSeed()
        random.reset()
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
            val metadata = metadata.getMetadataFor(identifier.key)
            if (metadata != null) {
                val amount = random.fromRange(identifier.value)
                deltaStorage.addAllResources(
                    metadata.key,
                    metadata,
                    factory.create(metadata.resource.resourceClass, amount),
                )
            } else logger.warn("No resource metadata found for ${identifier.key}")
        }
        relations.relateDataset(deltaMetadataList, SetType.DELTA)
        val fullList = getAllGeneratedResourcesForSetType(deltaMetadataList, SetType.DELTA)
        deltaStorage.purge()
        return fullList
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

            logger.trace("${i.key} : $setType : ${fullList[i]?.size}")
        }
        return fullList
    }

    fun getAllMetadata(): MutableList<ExpandedMetadata> {
        return metadata.getAllMetadata()
    }

    fun getMetadataFromIdentifier(identifiers: ResourceIdentifiers): ExpandedMetadata? =
        metadata.getMetadataFor(identifiers)

    fun purgeAllStoredResources() {
        deltaStorage.purge()
        storage.purge()
        logger.debug("Purging all stored resources")
    }

}