package no.fintlabs.coreadapter.runner

import no.fint.model.FintMultiplicity
import no.fint.model.FintRelation
import no.fint.model.resource.FintResource
import no.fint.model.resource.Link
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

    fun executeInitialDataset() {
        // Generates and stores
        val metadataList: MutableList<ExpandedMetadata> = mutableListOf()
        capabilities.forEach {
            val resourceData: Resource? = model.getResource(it.component, it.resource)
            if (resourceData != null) {
                val metadata = ExpandedMetadata(resourceData, "${it.component}/${it.component}")
                metadataList.add(metadata)
                val data: List<FintResource> = generator.create(metadata.resource.resourceType, it.count)
                storage.addAll(it.resourceKey, data)
            }
            println("")
            println("⚠️ " + it.component + it.resource + "was not found in metamodel...")
            println("")
        }
        if (props.link) {
            // Check Relations and Link accordingly
            relateInitialDataset(metadataList)
        }

        // Publish all when linking is complete
    }

    private fun relateInitialDataset(resources: List<ExpandedMetadata>) {
        val skipList: List<String> = mutableListOf()

        for (resource in resources) {
            val relations: List<FintRelation> = resource.resource.relations
            for (relation in relations) {
                if (skipList.contains("${relation.name}/${resource.resource.name}")) {
                    continue
                }
                if (relation.multiplicity != FintMultiplicity.NONE_TO_ONE ||
                    relation.multiplicity != FintMultiplicity.NONE_TO_MANY
                ) {
                    val secondaryMetadata: ExpandedMetadata? =
                        resources.firstOrNull { resource.resource.name == relation.name }
                    if (secondaryMetadata == null) {
                        if (relation.multiplicity == FintMultiplicity.ONE_TO_ONE) {
                            println("")
                            println("⚠️ ${resource.resource.name}'s required relation ${relation.name} not found in localStorage.")
                            println("Add ${relation.name} to initialDataSets.")
                            println("")
                        } else {
                            continue
                        }
                    } else {
                        skipList + ("${resource.resource.name}/${relation.name}")
                        val primary = storage.getAll(resource.key)
                        val secondary = storage.getAll(secondaryMetadata.key)
                        primary.forEachIndexed { index, item ->
                            item.links[relation.name] =
                                listOf(Link.with(secondary[index].getFirstId()))
                        }
                        storage.updateAll(resource.key, primary)
                    }
                }
            }
        }
    }

    private fun FintResource.getFirstId(): String? = identifikators.firstNotNullOf { it.value }.identifikatorverdi
}
