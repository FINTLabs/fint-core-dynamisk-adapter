package no.fintlabs.coreadapter.runner

import no.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.coreadapter.data.DeltaSyncDataset
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedDeltaMetadata
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.data.InitialDataset
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.store.TempDeltaSyncStore
import no.fintlabs.dynamiskadapter.DynamicAdapterService
import no.fintlabs.metamodel.MetamodelService
import no.fintlabs.metamodel.model.Resource
import org.springframework.stereotype.Component
import kotlin.random.Random

@Component
class DynamicAdapterEngine(
    private val props: DynamicAdapterProperties,
    private val model: MetamodelService,
    private val generator: DynamicAdapterService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
) {
    private val initialDataSets: List<InitialDataset> = props.initialDataSets
    private val deltaSyncDataSets: List<DeltaSyncDataset> = props.deltaSyncDataSets
    val metadataList: MutableList<ExpandedMetadata> = mutableListOf()
    val deltaMetadataList: MutableList<ExpandedDeltaMetadata> = mutableListOf()

    fun generateCapabilities(): MutableSet<AdapterCapability> {
        val capabilities: MutableSet<AdapterCapability> = mutableSetOf()
        for (it in props.initialDataSets) {
            val capability =
                AdapterCapability(
                    it.component.substringBefore("."),
                    it.component.substringAfter("."),
                    it.resource,
                    1,
                    AdapterCapability.DeltaSyncInterval.IMMEDIATE,
                )
            capabilities.add(capability)
        }
        return capabilities
    }

    fun executeInitialDataset() {
        initialDataSets.forEach {
            val resourceData: Resource? = model.getResource(it.component, it.resource)
            if (resourceData != null) {
                val metadata = ExpandedMetadata(resourceData, it.resourceKey)
                metadataList.add(metadata)
                val data: List<FintResource> =
                    generator.create(metadata.resource.resourceType, it.count, props.consoleLogging)
                storage.addAllResources(it.resourceKey, data)
            } else {
                println("")
                println("⚠️ " + it.component + "/" + it.resource + " was not found in metamodel...")
                println("")
            }
        }
        println("⚙️✅ DynamicAdapterEngine: ${metadataList.size} types of resources created.")
    }

    fun executeDeltaSyncDataset() {
        for (it in deltaMetadataList) {
            val count = Random.nextInt(it.minSize, it.maxSize)
            val data: List<FintResource> = generator.create(it.resource.resourceType, count)
            deltaStorage.addAllResources(it.key, data)
        }
    }

    fun generateDeltaSyncMetadata() {
        if (props.enableDeltaSync && deltaSyncDataSets.isNotEmpty()) {
            deltaSyncDataSets.forEach {
                val resourceData: Resource? = model.getResource(it.component, it.resource)
                if (resourceData != null) {
                    val metaData = ExpandedDeltaMetadata(resourceData, it.resourceKey, it.minSize, it.maxSize)
                    deltaMetadataList.add(metaData)
                }
            }
            logIfEnabled("⚙️✅ DynamicAdapterEngine: ${deltaMetadataList.size} types of resources created for deltaSync.")
        }
    }

    fun printAllDataIfEnabled() {
        if (props.consoleLogDataset) {
            println("⚙️ PRINTING ALL DATA:")
            for (metadata in metadataList) {
                val data = storage.getAll(metadata.key)
                println("FULL ${metadata.key}, x${data.size}")
                for (i in data) {
                    println(i.resource)
                }
            }
            println("")
        }
    }

    fun printAllDeltaDataIfEnabled() {
        if (props.consoleLogDataset) {
            println("⚙️ PRINTING ALL DELTA DATA:")
            for (metadata in deltaMetadataList) {
                val data = deltaStorage.getAll(metadata.key)
                println("DELTA ${metadata.key}, x${data.size}")
                for (i in data) {
                    println(i.resource)
                }
            }
            println("")
        }
    }

    fun deltaDoneLogAmountOfResources() {
        for (it in deltaSyncDataSets) {
            logIfEnabled("FullStorage now contains ${storage.countResources(it.resourceKey)} ${it.resourceKey}")
        }
    }

    private fun logIfEnabled(log: String) {
        if (props.consoleLogging) println(log)
    }
}
