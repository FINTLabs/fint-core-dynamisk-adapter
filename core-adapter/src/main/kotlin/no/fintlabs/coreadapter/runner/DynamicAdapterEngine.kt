package no.fintlabs.coreadapter.runner

import no.fint.model.resource.FintResource
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
    private val capabilities: List<InitialDataset> = props.initialDataSets
    val metadataList: MutableList<ExpandedMetadata> = mutableListOf()

    fun executeInitialDataset(): MutableList<ExpandedMetadata> {
        capabilities.forEach {
            val resourceData: Resource? = model.getResource(it.component, it.resource)
            if (resourceData != null) {
                val metadata = ExpandedMetadata(resourceData, it.resourceKey)
                metadataList.add(metadata)
                val data: List<FintResource> = generator.create(metadata.resource.resourceType, it.count)
                storage.addAll(it.resourceKey, data)
            } else {
                logIfEnabled("")
                logIfEnabled("⚠️ " + it.component + "/" + it.resource + " was not found in metamodel...")
                logIfEnabled("")
            }
        }
        println("⚙️✅ DynamicAdapterEngine: ${metadataList.size} types of resources created.")
        return metadataList
    }

    private fun logIfEnabled(log: String) {
        if (props.consoleLogging) println(log)
    }
}
