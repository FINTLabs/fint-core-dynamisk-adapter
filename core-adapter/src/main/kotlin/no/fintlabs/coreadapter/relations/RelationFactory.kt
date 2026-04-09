package no.fintlabs.coreadapter.relations

import no.novari.fint.model.FintMultiplicity
import no.novari.fint.model.FintRelation
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.store.TempDeltaSyncStore
import no.fintlabs.coreadapter.util.putLink
import no.fintlabs.coreadapter.util.toResourceKey
import no.novari.metamodel.MetamodelService
import org.springframework.stereotype.Component
import kotlin.random.Random

private data class Edge(
    val from: String,
    val to: String,
)

private enum class LinkRule {
    ROUND_ROBIN,
    UNIQUE_STRICT,
}

enum class SetType {
    INITIAL,
    DELTA,
}

@Component
class RelationFactory(
    private val props: DynamicAdapterProperties,
    private val model: MetamodelService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
) {
    fun relateDataset(
        metadataList: MutableList<ExpandedMetadata>,
        setType: SetType,
    ) {
        val skip = mutableSetOf<Edge>()

        for (resource in metadataList) {
            val relations: List<FintRelation> = resource.resource.relations
            for (relation in relations) {
                val edgeToSkip = Edge(relation.toResourceKey(), resource.key)
                if (edgeToSkip in skip) {
                    continue
                } else {
                    when (relation.multiplicity) {
                        FintMultiplicity.NONE_TO_MANY,
                        FintMultiplicity.NONE_TO_ONE,
                            -> {
                            continue
                        }

                        FintMultiplicity.ONE_TO_MANY -> {
                            val secondaryMetadata = getSecondaryMetadata(relation, resource.key)
                            if (secondaryMetadata != null) {
                                val secondaryMultiplicity = getSecondaryMultiplicity(resource.key, secondaryMetadata)
                                if (secondaryMultiplicity == FintMultiplicity.ONE_TO_MANY) {
                                    giveLink(
                                        resource.key,
                                        secondaryMetadata.key,
                                        relation,
                                        LinkRule.ROUND_ROBIN,
                                        setType
                                    )
                                    skip.add(Edge(resource.key, relation.toResourceKey()))
                                }
                            }
                        }

                        FintMultiplicity.ONE_TO_ONE -> {
                            val secondaryMetadata = getSecondaryMetadata(relation, resource.key)
                            if (secondaryMetadata != null) {
                                val secondaryMultiplicity = getSecondaryMultiplicity(resource.key, secondaryMetadata)
                                val linkRule =
                                    if (secondaryMultiplicity == FintMultiplicity.ONE_TO_ONE) {
                                        LinkRule.UNIQUE_STRICT
                                    } else {
                                        LinkRule.ROUND_ROBIN
                                    }
                                giveLink(resource.key, secondaryMetadata.key, relation, linkRule, setType)
                                skip.add(Edge(resource.key, relation.toResourceKey()))
                            } else {
                                logIfEnabled("")
                                logIfEnabled("⛓️⚠️ ${resource.key}'s required relation $relation not found.")
                                logIfEnabled("⛓️⚠️ Adding ${relation.packageName} is recommended.")
                                logIfEnabled("")
                            }
                        }
                    }
                }
            }
        }
        println("⛓️✅ $setType set relating complete.")
        println("")
        skip.clear()
    }

    private fun giveLink(
        primaryKey: String,
        secondaryKey: String,
        relation: FintRelation,
        linkRule: LinkRule,
        setType: SetType,
    ) {
        val primaryIds =
            if (setType == SetType.INITIAL) storage.getIdsFor(primaryKey) else deltaStorage.getIdsFor(primaryKey)
        val secondaryIds = deltaStorage.getIdsFor(secondaryKey) + storage.getIdsFor(secondaryKey)
        val primName = primaryKey.substringAfterLast("/")
        val secName = secondaryKey.substringAfterLast("/")
        if (secondaryIds.isEmpty()) {
            logIfEnabled("Zero $secName's found to link with $primName")
            return
        }

        primaryIds.forEachIndexed { i, primaryId ->
            val target =
                if (linkRule == LinkRule.UNIQUE_STRICT) {
                    "NOT_ENOUGH_$secondaryKey"
                } else {
                    if (setType == SetType.DELTA) {
                        secondaryIds[Random.nextInt(1, secondaryIds.size)]
                    } else {
                        secondaryIds[i % secondaryIds.size]
                    }
                }

            if (setType == SetType.DELTA) {
                deltaStorage.updateResource(primaryKey, primaryId) { r ->
                    r.putLink(relation.name, target)
                }
            } else {
                storage.updateResource(primaryKey, primaryId) { r ->
                    r.putLink(relation.name, target)
                }
            }
        }
        logIfEnabled("⛓️ $setType : $primName now has links to $secName")
    }

    private fun getSecondaryMetadata(relation: FintRelation, primaryKey: String): ExpandedMetadata? {
        val rawKey = relation.toResourceKey()
        val parts = rawKey.split("/")

        val key = if (parts.size == 3) {
            rawKey
        } else {
            val relationName = rawKey.substringAfterLast("/")
            val primaryParts = primaryKey.split("/")
            require(primaryParts.size >= 2) { "Invalid primaryKey format: $primaryKey" }

            "${primaryParts[0]}/${primaryParts[1]}/$relationName"
        }
        val keyParts = key.split("/")
        require(keyParts.size == 3) { "Invalid resolved key format: $key" }

        val (domain, packageName, resource) = keyParts

        val resourceData = model.getResource(domain, packageName, resource)

        return if (resourceData != null) ExpandedMetadata(resourceData, key) else null
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
