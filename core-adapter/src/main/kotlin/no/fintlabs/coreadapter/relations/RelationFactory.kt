package no.fintlabs.coreadapter.relations

import no.fint.model.FintMultiplicity
import no.fint.model.FintRelation
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.util.getFirstId
import no.fintlabs.coreadapter.util.putLink
import no.fintlabs.coreadapter.util.toResourceKey
import org.springframework.stereotype.Component

@Component
class RelationFactory(
    private val storage: ResourceStore,
    private val props: DynamicAdapterProperties,
) {
    fun relateInitialDataset(metadataList: MutableList<ExpandedMetadata>) {
        val skipList: MutableList<String> = mutableListOf()

        for (resource in metadataList) {
            val relations: List<FintRelation> = resource.resource.relations
            for (relation in relations) {
                if (skipList.contains("${relation.toResourceKey()}-${resource.key}")) {
                    continue
                }
                if (relation.multiplicity != FintMultiplicity.NONE_TO_ONE &&
                    relation.multiplicity != FintMultiplicity.NONE_TO_MANY
                ) {
                    // If multiplicity starts with none, skip.
                    val secondaryMetadata: ExpandedMetadata? =
                        metadataList.firstOrNull { it.key == relation.toResourceKey() }
                    if (secondaryMetadata == null) {
                        if (relation.multiplicity == FintMultiplicity.ONE_TO_ONE) {
                            logIfEnabled("")
                            logIfEnabled("⚠️ ${resource.resource.name}'s required relation ${relation.name} not found in localStorage.")
                            logIfEnabled("Add ${relation.packageName} to initialDataSets.")
                            logIfEnabled("")
                        } else {
                            // One_To_Many and can't find relation, skip.
                            // For now, we only care about linking one way and One_To_One
                            // takes care of that in most currently relevant situations.
                            continue
                        }
                        // When both main and related are present:
                    } else {
                        val reverseRelation =
                            secondaryMetadata.resource.relations.firstOrNull {
                                it.toResourceKey() == resource.key
                            }
                        val secondaryMultiplicity = reverseRelation?.multiplicity
                        if (secondaryMultiplicity == FintMultiplicity.ONE_TO_ONE &&
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
                                val target = secondary[index % secondary.size]
                                val targetId: String =
                                    (target.identifikators.firstNotNullOf { it.key }) + "/" +
                                        (target.getFirstId() ?: "NO_IDENTIFIERS_FOUND")
                                // TODO: Expand links to reflect real data.
                                // Current links: links={elev=[brukernavn/HughJazz]}
                                // production links:
                                item.putLink(relation.name, targetId)
                            }
                            storage.updateAll(resource.key, primary)
                            skipList.add("${resource.key}-${relation.toResourceKey()}")

                            logIfEnabled("⛓️ ${resource.resource.name} now has links to ${relation.name}")
                            logIfEnabled("")
                        }
                    }
                } else {
                    continue
                }
            }
        }
        println("⚙️✅ InitialDataset relating complete.")
    }

    private fun logIfEnabled(log: String) {
        if (props.consoleLogging) println(log)
    }
}
