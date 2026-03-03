package no.fintlabs.coreadapter.relations

import no.fint.model.FintMultiplicity
import no.fint.model.FintRelation
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.util.putLink
import no.fintlabs.coreadapter.util.toResourceKey
import no.fintlabs.metamodel.MetamodelService
import org.springframework.stereotype.Component

private data class Edge(
    val from: String,
    val to: String,
)

private enum class LinkRule {
    ROUND_ROBIN,
    UNIQUE_STRICT,
}

@Component
class RelationFactory(
    private val props: DynamicAdapterProperties,
    private val model: MetamodelService,
    private val storage: ResourceStore,
) {
    // TODO: REFACTOR AFTER STORE CHANGE
    fun relateInitialDataset(metadataList: MutableList<ExpandedMetadata>) {
        val skip = mutableSetOf<Edge>()

        for (resource in metadataList) {
            val relations: List<FintRelation> = resource.resource.relations
            for (relation in relations) {
                val edgeToSkip = Edge(relation.toResourceKey(), resource.key)
                if (edgeToSkip in skip) {
                    logIfEnabled("⛓️ ${resource.resource.name} Link skipped for ${relation.name}")
                    continue
                } else {
                    when (relation.multiplicity) {
                        FintMultiplicity.NONE_TO_MANY,
                        FintMultiplicity.NONE_TO_ONE,
                        -> {
                            continue
                        }

                        FintMultiplicity.ONE_TO_MANY -> {
                            val secondaryMetadata = getSecondaryMetadata(relation)
                            if (secondaryMetadata != null) {
                                val secondaryMultiplicity = getSecondaryMultiplicity(resource.key, secondaryMetadata)
                                if (secondaryMultiplicity == FintMultiplicity.ONE_TO_MANY) {
                                    giveLink(resource.key, relation, LinkRule.ROUND_ROBIN)
                                    skip.add(Edge(resource.key, relation.toResourceKey()))
                                }
                            }
                        }

                        FintMultiplicity.ONE_TO_ONE -> {
                            val secondaryMetadata = getSecondaryMetadata(relation)
                            if (secondaryMetadata != null) {
                                val secondaryMultiplicity = getSecondaryMultiplicity(resource.key, secondaryMetadata)
                                val linkRule =
                                    if (secondaryMultiplicity == FintMultiplicity.ONE_TO_ONE) {
                                        LinkRule.UNIQUE_STRICT
                                    } else {
                                        LinkRule.ROUND_ROBIN
                                    }
                                giveLink(resource.key, relation, linkRule)
                                skip.add(Edge(resource.key, relation.toResourceKey()))
                            }
                        }
                    }
                }
            }
        }
        println("")
        println("⚙️✅ InitialDataset relating complete.")
    }

    private fun giveLink(
        primaryKey: String,
        relation: FintRelation,
        linkRule: LinkRule,
    ) {
        val secondaryKey = relation.toResourceKey()
        val primaryIds = storage.getAll(primaryKey).map { it.id }
        val secondaryIds = storage.getAll(secondaryKey).map { it.id }

        primaryIds.forEachIndexed { i, primaryId ->
            val target =
                if (linkRule == LinkRule.UNIQUE_STRICT) {
                    "NOT_ENOUGH_$secondaryKey"
                } else {
                    secondaryIds[i % secondaryIds.size]
                }

            storage.updateResource(primaryKey, primaryId) { r ->
                r.putLink(relation.name, target)
            }
        }
        logIfEnabled("⛓️ $primaryKey now has links to $secondaryKey")
    }

    private fun getSecondaryMetadata(relation: FintRelation): ExpandedMetadata? {
        val key = relation.toResourceKey()
        val component = key.substringBeforeLast("/").replace('/', '.')
        val resource = key.substringAfterLast("/")
        val resourceData = model.getResource(component, resource)
        if (resourceData == null) {
            println("getSecondaryMetadata $component/$resource not found")
            return null
        } else {
            return ExpandedMetadata(resourceData, relation.toResourceKey())
        }
    }

    private fun getSecondaryMultiplicity(
        primaryKey: String,
        secondaryMetaData: ExpandedMetadata,
    ): FintMultiplicity? =
        secondaryMetaData.resource.relations
            .firstOrNull {
                it.toResourceKey() == primaryKey
            }?.multiplicity

    private fun logIfEnabled(log: String) {
        if (props.consoleLogging) println(log)
    }
}
