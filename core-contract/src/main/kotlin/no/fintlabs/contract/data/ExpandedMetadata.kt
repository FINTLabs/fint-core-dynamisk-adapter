package no.fintlabs.contract.data

import no.fintlabs.contract.models.ResourceIdentifiers
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
) {
    fun toIdentifiers(): ResourceIdentifiers {
        val parts = key.split("/")
        return ResourceIdentifiers(
            domain = parts[0],
            component = parts[1],
            resource = parts[2],
        )
    }
}