package no.fintlabs.engine

import no.novari.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.fintlabs.library.ResourceFactory
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

@Component
class DynamicAdapterEngine(
    private val metadata: MetadataService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val relations: RelationFactory,
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
    ): ConcurrentHashMap<ExpandedMetadata, List<FintResource>> {
        val meta = metadata.getAllMetadata()
        val fullList = ConcurrentHashMap<ExpandedMetadata, List<FintResource>>()

        for (i in meta) {
            fullList[i] = storage.getAllResources(i.key)
            logger.trace("${i.key} : ${fullList[i]?.size}")
        }
        return fullList
    }

    fun executeInitialDataset(
        amountTierPolicy: AmountTierPolicy,
        seed: String = "",
    ) {
        val generator = ResourceFactory(seed)
        val metadata = metadata.getAllMetadata()
        for (resource in metadata) {
            val amountRange: IntRange = amountTierPolicy.getRange(resource.amountTier ?: AmountTier.UNKNOWN)
            val amount = amountRange.random()
            val generated = generator.create(resource.resource.resourceClass, amount)
            storage.addAllResources(resource, generated)
        }
        relations.relateDataset(metadata, setType = SetType.INITIAL)
    }

    fun getAllMetadata(): MutableList<ExpandedMetadata> {
        return metadata.getAllMetadata()
    }

    fun purgeAllStoredResources() {
        deltaStorage.purge()
        storage.purge()
        logger.debug("Purging all stored resources")
    }

}

//    fun executeInitialDataset() {
//        initialDataSets.forEach {
//            val resourceData: Resource? =
//                model.getResource(
//                    it.component.substringBefore("."),
//                    it.component.substringAfter("."),
//                    it.resource
//                )
//            if (resourceData != null) {
//                val idMeta = resourceData.generateIdMetadata()
//                val metadata = ExpandedMetadata(resourceData, it.resourceKey, idMeta.prefix, idMeta.type)
//                metadataList.add(metadata)
//                val data: List<FintResource> =
//                    generator.create(
//                        metadata.resource.resourceClass,
//                        it.count,
//                        props.consoleLogging,
//                        props.errorPercentage
//                    )
//                storage.addAllResources(it.resourceKey, metadata, data)
//            } else {
//            }
//        }
//        println("⚙️✅ DynamicAdapterEngine: ${metadataList.size} types of resources created.")
//        println("")
//    }

//    fun executeDeltaSyncDataset() {
//        for (it in deltaMetadataList) {
//            val count = Random.Default.nextInt(it.minSize, it.maxSize)
//            val data: List<FintResource> = generator.create(it.resource.resourceClass, count)
//            deltaStorage.addAllResources(it.key, it.toExpandedMetadata(), data)
//        }
//    }

//    fun generateDeltaSyncMetadata() {
//        if (props.enableDeltaSync && deltaSyncDataSets.isNotEmpty()) {
//            deltaSyncDataSets.forEach {
//                val resourceData: Resource? = model.getResource(
//                    it.component, it.component, it.resource,
//                )
//                if (resourceData != null) {
//                    val idMeta = resourceData.generateIdMetadata()
//                    val metaData = ExpandedDeltaMetadata(
//                        resourceData,
//                        it.resourceKey,
//                        idMeta.prefix,
//                        idMeta.type,
//                        it.minSize,
//                        it.maxSize
//                    )
//                    deltaMetadataList.add(metaData)
//                }
//            }
//            logIfEnabled("⚙️✅ DynamicAdapterEngine: ${deltaMetadataList.size} types of resources created for deltaSync.")
//        }
//    }
