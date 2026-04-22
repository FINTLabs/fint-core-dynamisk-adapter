package no.fintlabs.coreadapter.data

import no.novari.metamodel.model.Resource

data class ExpandedMetadata(
    val resource: Resource,
    val idPrefix: String,
    val key: String,
)

data class ExpandedDeltaMetadata(
    val resource: Resource,
    val idPrefix: String,
    val key: String,
    val minSize: Int,
    val maxSize: Int,
)

fun MutableList<ExpandedDeltaMetadata>.toExpandedMetadata(): MutableList<ExpandedMetadata> =
    this.map { ExpandedMetadata(it.resource, it.idPrefix, it.key) } as MutableList<ExpandedMetadata>
