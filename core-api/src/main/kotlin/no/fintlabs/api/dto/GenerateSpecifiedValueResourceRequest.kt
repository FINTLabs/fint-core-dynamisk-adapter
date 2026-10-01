package no.fintlabs.api.dto

data class GenerateSpecifiedValueResourceRequest(
    val resource: String,
    val fieldName: String,
    val fieldValue: String,
    val amount: Int = 1,
)