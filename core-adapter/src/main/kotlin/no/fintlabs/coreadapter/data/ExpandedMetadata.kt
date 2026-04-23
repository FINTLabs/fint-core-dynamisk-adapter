package no.fintlabs.coreadapter.data

import no.novari.metamodel.model.Resource

enum class IdFieldType {
    IDENTIFIKATOR_MAP,
    DIRECT_FIELD,
}

data class ExpandedMetadata(
    val resource: Resource,
    val key: String,
    val idPrefix: String,
    val idFieldType: IdFieldType,
)

data class ExpandedDeltaMetadata(
    val resource: Resource,
    val key: String,
    val idPrefix: String,
    val idFieldType: IdFieldType,
    val minSize: Int,
    val maxSize: Int,
)

fun MutableList<ExpandedDeltaMetadata>.toExpandedMetadata(): MutableList<ExpandedMetadata> =
    this.map { ExpandedMetadata(it.resource, it.idPrefix, it.key, it.idFieldType) } as MutableList<ExpandedMetadata>

fun ExpandedDeltaMetadata.toExpandedMetadata(): ExpandedMetadata =
    ExpandedMetadata(this.resource, this.idPrefix, this.key, this.idFieldType)