package no.fintlabs.engine

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.fintlabs.library.ResourceFactory
import no.novari.metamodel.MetamodelService
import org.springframework.stereotype.Component

@Component
class DynamicAdapterEngine(
    private val generator: ResourceFactory,
    private val metadata: MetadataService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val relations: RelationFactory,
) {
    fun generateCapabilitiesForDomains(
        domains: List<String>
    ): MutableSet<AdapterCapability> {
        for (domain in domains) {
            metadata.generateMetadataFromDomain(domain)
        }
        return metadata.generateCapabilities()
    }

    fun executeInitialDataset(amountTierPolicy: AmountTierPolicy) {
        val metadata = metadata.getAllMetadata()
        for (resource in metadata) {
            val amountRange: IntRange = amountTierPolicy.getRange(resource.amountTier ?: AmountTier.UNKNOWN)
            val amount = amountRange.random()
            val generated = generator.create(resource.resource.resourceClass, amount)
            storage.addAllResources(resource, generated)
        }
        relations.relateDataset(metadata, setType = SetType.INITIAL)
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

//    fun printAllDataIfEnabled() {
//        if (props.consoleLogDataset) {
//            println("⚙️ PRINTING ALL DATA:")
//            for (metadata in metadataList) {
//                val data = storage.getAll(metadata.key)
//                println("FULL ${metadata.key}, x${data.size}")
//                for (i in data) {
//                    println(i.resource)
//                }
//            }
//            println("")
//        }
//    }

//    fun printAllDeltaDataIfEnabled() {
//        if (props.consoleLogDataset) {
//            println("⚙️ PRINTING ALL DELTA DATA:")
//            for (metadata in deltaMetadataList) {
//                val data = deltaStorage.getAll(metadata.key)
//                println("DELTA ${metadata.key}, x${data.size}")
//                for (i in data) {
//                    println(i.resource)
//                }
//            }
//            println("")
//        }
//    }

//    fun deltaDoneLogAmountOfResources() {
//        for (it in deltaSyncDataSets) {
//            logIfEnabled("FullStorage now contains ${storage.countResources(it.resourceKey)} ${it.resourceKey}")
//        }
//    }