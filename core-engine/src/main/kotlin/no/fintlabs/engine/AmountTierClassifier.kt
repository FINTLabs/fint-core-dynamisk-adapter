package no.fintlabs.engine

import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.ExpandedMetadata
import no.novari.fint.model.FintMultiplicity
import org.springframework.stereotype.Component

@Component
class AmountTierClassifier {

    fun classify(resources: MutableList<ExpandedMetadata>) {
        val relations = buildRelations(resources)

        do {
            var changed = false

            changed = markKnownResources(resources) || changed
            changed = markMutualSingleRelationsAsCore(relations) || changed
            changed = propagateTierThroughOneToOne(relations) || changed
            changed = markDependants(relations) || changed
            changed = propagateDependants(relations) || changed
            changed = markGroupings(relations) || changed
            changed = markMembershipGroupings(relations) || changed

        } while (changed)

        resources.forEach {
            it.amountTier = it.amountTier ?: AmountTier.UNKNOWN
        }
    }

    private fun buildRelations(resources: List<ExpandedMetadata>): List<Relation> {
        val byName = resources.associateBy { it.resource.name }

        val direct = resources.flatMap { source ->
            source.resource.relations.mapNotNull { relation ->
                val target = byName[relation.name] ?: return@mapNotNull null

                DirectRelation(
                    source = source,
                    target = target,
                    multiplicity = relation.multiplicity
                )
            }
        }

        return direct.mapNotNull { relation ->
            val reverse = direct.firstOrNull {
                it.source == relation.target && it.target == relation.source
            } ?: return@mapNotNull null

            Relation(
                source = relation.source,
                target = relation.target,
                sourceToTarget = relation.multiplicity,
                targetToSource = reverse.multiplicity
            )
        }
    }

    private fun markKnownResources(resources: List<ExpandedMetadata>): Boolean {
        var changed = false

        resources.forEach { metadata ->
            val name = metadata.resource.name.lowercase()

            if (name == "person") {
                changed = metadata.assignTier(AmountTier.CORE) || changed
            }

            if (name.endsWith("gruppe") || name.endsWith("klasse")) {
                changed = metadata.assignTier(AmountTier.GROUPING) || changed
            }

            if (name.endsWith("medlemsskap")) {
                changed = metadata.assignTier(AmountTier.DEPENDANT) || changed
            }
        }

        return changed
    }

    private fun markMutualSingleRelationsAsCore(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            if (
                relation.sourceToTarget.pointsToOne() &&
                relation.targetToSource.pointsToOne()
            ) {
                changed = relation.source.assignTier(AmountTier.CORE) || changed
                changed = relation.target.assignTier(AmountTier.CORE) || changed
            }
        }

        return changed
    }

    private fun propagateTierThroughOneToOne(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            val sourceTier = relation.source.amountTier
            val targetTier = relation.target.amountTier

            if (relation.sourceToTarget == FintMultiplicity.ONE_TO_ONE && sourceTier != null) {
                changed = relation.target.assignTier(sourceTier) || changed
            }

            if (relation.targetToSource == FintMultiplicity.ONE_TO_ONE && targetTier != null) {
                changed = relation.source.assignTier(targetTier) || changed
            }
        }

        return changed
    }

    private fun markDependants(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            if (
                relation.source.amountTier == null &&
                relation.target.amountTier == AmountTier.CORE &&
                relation.sourceToTarget.pointsToOne() &&
                relation.targetToSource.pointsToMany()
            ) {
                changed = relation.source.assignTier(AmountTier.DEPENDANT) || changed
            }
        }

        return changed
    }

    private fun propagateDependants(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            if (
                relation.source.amountTier == null &&
                relation.target.amountTier == AmountTier.DEPENDANT &&
                relation.sourceToTarget.pointsToOne()
            ) {
                changed = relation.source.assignTier(AmountTier.DEPENDANT) || changed
            }
        }

        return changed
    }

    private fun markGroupings(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            if (
                relation.source.amountTier == null &&
                relation.target.amountTier == AmountTier.CORE &&
                relation.sourceToTarget.pointsToMany()
            ) {
                changed = relation.source.assignTier(AmountTier.GROUPING) || changed
            }
        }

        return changed
    }

    private fun markMembershipGroupings(relations: List<Relation>): Boolean {
        var changed = false

        relations.forEach { relation ->
            val sourceName = relation.source.resource.name.lowercase()

            if (
                relation.source.amountTier == AmountTier.DEPENDANT &&
                sourceName.endsWith("medlemskap") &&
                relation.target.amountTier == null &&
                relation.sourceToTarget.pointsToOne() &&
                relation.targetToSource.pointsToMany()
            ) {
                changed = relation.target.assignTier(AmountTier.GROUPING) || changed
            }
        }

        return changed
    }

    private fun ExpandedMetadata.assignTier(tier: AmountTier): Boolean {
        if (amountTier == tier) return false
        if (amountTier != null) return false

        amountTier = tier
        return true
    }

//    private fun Relation.isMutualOneToOne(): Boolean =
//        sourceToTarget == FintMultiplicity.ONE_TO_ONE &&
//                targetToSource == FintMultiplicity.ONE_TO_ONE

    private fun FintMultiplicity.pointsToOne(): Boolean =
        this == FintMultiplicity.ONE_TO_ONE ||
                this == FintMultiplicity.NONE_TO_ONE

    private fun FintMultiplicity.pointsToMany(): Boolean =
        this == FintMultiplicity.ONE_TO_MANY ||
                this == FintMultiplicity.NONE_TO_MANY

    private data class DirectRelation(
        val source: ExpandedMetadata,
        val target: ExpandedMetadata,
        val multiplicity: FintMultiplicity
    )

    private data class Relation(
        val source: ExpandedMetadata,
        val target: ExpandedMetadata,
        val sourceToTarget: FintMultiplicity,
        val targetToSource: FintMultiplicity
    )
}