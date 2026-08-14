package no.fintlabs.contract.dto

data class AmountTierPolicyRequest(
    val core: Int,
    val groupingMin: Int,
    val groupingMax: Int,
    val dependantMin: Int,
    val dependantMax: Int,
    val unknownMin: Int? = null,
    val unknownMax: Int? = null,
)
