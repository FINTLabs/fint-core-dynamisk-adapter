package no.fintlabs.contract.dto

data class GenerateSpecifiedValueResourceRequest(
    val amount: Int = 1,
    val specifiedResource: List<SpecifiedResource>,
    val supportingResources: List<String> = emptyList(),
)

data class SpecifiedResource(
    val resource: String,
    val fieldName: String,
    val fieldValue: String,
)