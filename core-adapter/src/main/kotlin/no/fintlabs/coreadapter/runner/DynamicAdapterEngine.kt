package no.fintlabs.coreadapter.runner

import no.fint.model.FintMultiplicity
import no.fint.model.FintRelation
import no.fint.model.resource.FintResource
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.data.InitialDataset
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.util.putLink
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
                val metadata = ExpandedMetadata(resourceData, it.resourceKey)
                metadataList.add(metadata)
                val data: List<FintResource> = generator.create(metadata.resource.resourceType, it.count)
                storage.addAll(it.resourceKey, data)
            } else {
                println("")
                println("⚠️ " + it.component + it.resource + " was not found in metamodel...")
                println("")
            }
        }
        if (props.link) {
            // Check Relations and Link accordingly
            relateInitialDataset(metadataList)
            println("✅ Engine.ExecuteInitialDataset//relateInitialDataset finished relating required resources.")
        }
        println("DynamicAdapterEngine.executeInitialDataset --- ${metadataList.size} resources created.")
        for (metadata in metadataList) {
            val data = storage.getAll(metadata.key)
            for (i in data) {
                println(i)
            }
        }
    }

    private fun relateInitialDataset(resources: List<ExpandedMetadata>) {
        val skipList: List<String> = mutableListOf()

        for (resource in resources) {
            val relations: List<FintRelation> = resource.resource.relations
            for (relation in relations) {
                if (skipList.contains("${relation.toResourceKey()}-${resource.resource.name}")) {
                    continue
                }
                if (relation.multiplicity != FintMultiplicity.NONE_TO_ONE ||
                    relation.multiplicity != FintMultiplicity.NONE_TO_MANY
                ) {
                    // If multiplicity starts with none, skip.
                    val secondaryMetadata: ExpandedMetadata? =
                        resources.firstOrNull { it.key == relation.toResourceKey() }
                    if (secondaryMetadata == null) {
                        if (relation.multiplicity == FintMultiplicity.ONE_TO_ONE) {
                            println("")
                            println("⚠️ ${resource.resource.name}'s required relation ${relation.name} not found in localStorage.")
                            println("Add ${relation.packageName} to initialDataSets.")
                            println("")
                        } else {
                            // One_To_Many and can't find relation, skip.
                            // For now, we only care about linking one way and One_To_One
                            // takes care of that in most currently relevant situations.
                            continue
                        }
                        // When both main and related are present:
                    } else {
                        val secondaryMultiplicity =
                            secondaryMetadata.resource.relations
                                .firstOrNull {
                                    it.name.equals(
                                        resource.resource.name,
                                        ignoreCase = true,
                                    )
                                }!!
                                .multiplicity
                        if (secondaryMultiplicity == FintMultiplicity.ONE_TO_ONE ||
                            relation.multiplicity == FintMultiplicity.ONE_TO_MANY
                        ) {
                            // If secondaryMultiplicity is ONE_TO_ONE, and primaryMultiplicity is ONE_TO_MANY,
                            // wait with linking until the linking one is the ONE_TO_ONE.
                            continue
                        } else {
                            val primary = storage.getAll(resource.key)
                            val secondary = storage.getAll(secondaryMetadata.key)
                            // Links each to a separate following index, if second is longer than primary,
                            // loops back to 0 and continues up again.
                            primary.forEachIndexed { index, item ->
                                println("⚙️ Linking: ${resource.key} -WITH- ${secondaryMetadata.key}")
                                val target = secondary[index % secondary.size]
                                val targetId: String = target.getFirstId() ?: "IdNotFound"
                                item.putLink(relation.name, targetId)
                            }
                            storage.updateAll(resource.key, primary)
                            skipList + ("${resource.key}-${relation.toResourceKey()}")
                            println("Engine.relateInitialDataset --- skipList + ${resource.key}-${relation.toResourceKey()}")
                            println("Engine.relateInitialDataset --- ${resource.resource.name} now has links to ${relation.name}")
                        }
                    }
                } else {
                    continue
                }
            }
        }
    }

    private fun FintResource.getFirstId(): String? = identifikators.firstNotNullOf { it.value }.identifikatorverdi

    private fun FintRelation.toResourceKey(): String =
        packageName
            .substringAfter("model.")
            .replace(".", "/")
            .lowercase()
}
