package no.fintlabs.api.dto

import no.fintlabs.contract.models.ResourceIdentifiers

data class GenerateSpecifiedValueResourceRequest(
    val resource: ResourceIdentifiers,
    val fieldName: String,
    val fieldValue: String,
    val amount: Int = 1,
)