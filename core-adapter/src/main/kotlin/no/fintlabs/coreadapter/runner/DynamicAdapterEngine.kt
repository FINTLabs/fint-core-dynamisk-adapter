package no.fintlabs.coreadapter.runner

import no.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.data.InitialDataset
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.dynamiskadapter.DynamicAdapterService
import no.fintlabs.metamodel.MetamodelService
import no.fintlabs.metamodel.model.Resource
import org.springframework.stereotype.Component

@Component
class DynamicAdapterEngine(
    private val props: DynamicAdapterProperties,
    private val model: MetamodelService,
    private val generator: DynamicAdapterService,
    private val storage: ResourceStore,
) {
    private val initialDataSets: List<InitialDataset> = props.initialDataSets
    val metadataList: MutableList<ExpandedMetadata> = mutableListOf()

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
                val data: List<FintResource> = generator.create(metadata.resource.resourceType, it.count)
                storage.addAllResources(it.resourceKey, data)
            } else {
                logIfEnabled("")
                logIfEnabled("⚠️ " + it.component + "/" + it.resource + " was not found in metamodel...")
                logIfEnabled("")
            }
        }
        println("⚙️✅ DynamicAdapterEngine: ${metadataList.size} types of resources created.")
    }

    fun printAllDataIfEnabled() {
        if (props.consoleLogDataset) {
            for (metadata in metadataList) {
                val data = storage.getAll(metadata.key)
                for (i in data) {
                    println(i.resource)
                }
            }
        }
    }

    private fun logIfEnabled(log: String) {
        if (props.consoleLogging) println(log)
    }
}
