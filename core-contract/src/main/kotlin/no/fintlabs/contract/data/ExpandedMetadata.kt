package no.fintlabs.contract.data

import no.novari.metamodel.model.Resource

enum class IdFieldType {
    IDENTIFIKATOR_MAP,
    DIRECT_FIELD,
}

data class ExpandedMetadata(
    val resource: Resource,
    val key: String,
    var amountTier: AmountTier? = null,
    val idPrefix: String,
    val idFieldType: IdFieldType,
)