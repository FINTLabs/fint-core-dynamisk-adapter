package no.fintlabs.engine

import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.ExpandedMetadata
import no.novari.fint.model.FintMultiplicity
import org.springframework.stereotype.Component

@Component
class AmountTierClassifier {

    fun classify(resources: MutableList<ExpandedMetadata>) {
        val byName = resources.associateBy { it.resource.name }

        val relations = resources.flatMap { source ->
            source.resource.relations.mapNotNull { relation ->
                val target = byName[relation.name] ?: return@mapNotNull null

                ResourceRelation(
                    source = source,
                    target = target,
                    multiplicity = relation.multiplicity
                )
            }
        }

        val relationPairs = relations.mapNotNull { relation ->
            val reverse = relations.firstOrNull {
                it.source == relation.target && it.target == relation.source
            }

            if (reverse == null) {
                null
            } else {
                ResourceRelationPair(
                    left = relation.source,
                    right = relation.target,
                    leftToRight = relation.multiplicity,
                    rightToLeft = reverse.multiplicity
                )
            }
        }
        var changed: Boolean
        do {
            changed = false
            markKnownCoreResources(resources)
            markKnownGroupingResources(resources)
            markCore(relationPairs)
            changed = propagateCoreThroughSingleRelations(relationPairs) || false
            changed = markDependants(relationPairs) || changed
            changed = propagateDependants(relationPairs) || changed
            changed = markGroupingsFromDependants(relationPairs) || changed
            changed = markGroupingsFromMemberships(relationPairs) || changed
            changed = markGroupings(relationPairs) || changed
        } while (changed)

        resources.forEach {
            if (it.amountTier == null) {
                it.amountTier = AmountTier.UNKNOWN
            }
        }
    }

    private fun markCore(pairs: List<ResourceRelationPair>) {
        pairs.forEach { pair ->
            if (pair.leftToRight.isSingle() && pair.rightToLeft.isSingle()) {
                pair.left.amountTier = AmountTier.CORE
                pair.right.amountTier = AmountTier.CORE
            }
        }
    }

    private fun markDependants(pairs: List<ResourceRelationPair>): Boolean {
        var changed = false

        pairs
            .flatMap { it.bothDirections() }
            .forEach { relation ->
                if (
                    relation.source.amountTier == null &&
                    relation.target.amountTier == AmountTier.CORE &&
                    relation.sourceToTarget.isSingle() &&
                    relation.targetToSource.isMany()
                ) {
                    relation.source.amountTier = AmountTier.DEPENDANT
                    changed = true
                }
            }

        return changed
    }

    private fun markGroupings(pairs: List<ResourceRelationPair>): Boolean {
        var changed = false

        pairs.forEach { pair ->
            if (
                pair.left.amountTier == null &&
                pair.right.amountTier == AmountTier.CORE &&
                pair.leftToRight.isMany()
            ) {
                pair.left.amountTier = AmountTier.GROUPING
                changed = true
            }

            if (
                pair.right.amountTier == null &&
                pair.left.amountTier == AmountTier.CORE &&
                pair.rightToLeft.isMany()
            ) {
                pair.right.amountTier = AmountTier.GROUPING
                changed = true
            }
        }
        return changed
    }

    private fun markGroupingsFromDependants(pairs: List<ResourceRelationPair>): Boolean {
        var changed = false

        pairs.forEach { pair ->
            if (
                pair.left.amountTier == AmountTier.DEPENDANT &&
                pair.right.amountTier == null &&
                pair.leftToRight.isSingle() &&
                pair.rightToLeft.isMany()
            ) {
                pair.right.amountTier = AmountTier.GROUPING
                changed = true
            }

            if (
                pair.right.amountTier == AmountTier.DEPENDANT &&
                pair.left.amountTier == null &&
                pair.rightToLeft.isSingle() &&
                pair.leftToRight.isMany()
            ) {
                pair.left.amountTier = AmountTier.GROUPING
                changed = true
            }
        }
        return changed
    }

    private fun propagateDependants(pairs: List<ResourceRelationPair>): Boolean {
        var changed = false

        pairs.forEach { pair ->
            if (
                pair.left.amountTier == null &&
                pair.right.amountTier == AmountTier.DEPENDANT &&
                pair.leftToRight.isSingle()
            ) {
                pair.left.amountTier = AmountTier.DEPENDANT
                changed = true
            }

            if (
                pair.right.amountTier == null &&
                pair.left.amountTier == AmountTier.DEPENDANT &&
                pair.rightToLeft.isSingle()
            ) {
                pair.right.amountTier = AmountTier.DEPENDANT
                changed = true
            }
        }
        return changed
    }


    private fun markGroupingsFromMemberships(pairs: List<ResourceRelationPair>): Boolean {
        var changed = false

        pairs
            .flatMap { it.bothDirections() }
            .forEach { relation ->
                if (
                    relation.source.amountTier == AmountTier.DEPENDANT &&
                    relation.target.amountTier == null &&
                    relation.source.resource.name.endsWith("medlemskap", ignoreCase = true) &&
                    relation.sourceToTarget.isSingle() &&
                    relation.targetToSource.isMany()
                ) {
                    relation.target.amountTier = AmountTier.GROUPING
                    changed = true
                }
            }

        return changed
    }

    private fun propagateCoreThroughSingleRelations(
        pairs: List<ResourceRelationPair>
    ): Boolean {
        var changed = false

        pairs.forEach { pair ->
            if (
                pair.left.amountTier == null &&
                pair.right.amountTier == AmountTier.CORE &&
                pair.leftToRight.isSingle() &&
                pair.rightToLeft.isSingle()
            ) {
                pair.left.amountTier = AmountTier.CORE
                changed = true
            }

            if (
                pair.right.amountTier == null &&
                pair.left.amountTier == AmountTier.CORE &&
                pair.rightToLeft.isSingle() &&
                pair.leftToRight.isSingle()
            ) {
                pair.right.amountTier = AmountTier.CORE
                changed = true
            }
        }
        return changed
    }

    private fun markKnownCoreResources(resources: List<ExpandedMetadata>) {
        resources.forEach {
            if (it.resource.name == "person") {
                it.amountTier = AmountTier.CORE
            }
        }
    }

    private fun markKnownGroupingResources(resources: List<ExpandedMetadata>) {
        resources.forEach {
            if (it.resource.name.endsWith("gruppe")) {
                it.amountTier = AmountTier.GROUPING
            }
        }
    }

    private fun ResourceRelationPair.bothDirections(): List<DirectionalPair> =
        listOf(
            DirectionalPair(
                source = left,
                target = right,
                sourceToTarget = leftToRight,
                targetToSource = rightToLeft
            ),
            DirectionalPair(
                source = right,
                target = left,
                sourceToTarget = rightToLeft,
                targetToSource = leftToRight
            )
        )

    private data class DirectionalPair(
        val source: ExpandedMetadata,
        val target: ExpandedMetadata,
        val sourceToTarget: FintMultiplicity,
        val targetToSource: FintMultiplicity
    )

    private fun FintMultiplicity.isSingle(): Boolean =
        this == FintMultiplicity.ONE_TO_ONE ||
                this == FintMultiplicity.NONE_TO_ONE

    private fun FintMultiplicity.isMany(): Boolean =
        this == FintMultiplicity.ONE_TO_MANY ||
                this == FintMultiplicity.NONE_TO_MANY

    private data class ResourceRelation(
        val source: ExpandedMetadata,
        val target: ExpandedMetadata,
        val multiplicity: FintMultiplicity
    )

    private data class ResourceRelationPair(
        val left: ExpandedMetadata,
        val right: ExpandedMetadata,
        val leftToRight: FintMultiplicity,
        val rightToLeft: FintMultiplicity
    )
}