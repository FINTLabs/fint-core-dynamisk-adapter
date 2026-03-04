package no.fintlabs.coreadapter.data

import no.fintlabs.metamodel.model.Resource

data class ExpandedMetadata(
    val resource: Resource,
    val key: String,
)

data class ExpandedDeltaMetadata(
    val resource: Resource,
    val key: String,
    val minSize: Int,
    val maxSize: Int,
)

fun MutableList<ExpandedDeltaMetadata>.toExpandedMetadata(): MutableList<ExpandedMetadata> =
    this.map { ExpandedMetadata(it.resource, it.key) } as MutableList<ExpandedMetadata>
