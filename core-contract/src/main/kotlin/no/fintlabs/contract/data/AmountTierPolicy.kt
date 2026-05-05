package no.fintlabs.contract.data

data class AmountTierPolicy(
    val grouping: IntRange = 1..2,
    val core: IntRange = 10..10,
    val dependant: IntRange = 20..30,
    val unknown: IntRange = 1..2,
) {
    fun getRange(tier: AmountTier): IntRange =
        when (tier) {
            AmountTier.GROUPING -> grouping
            AmountTier.CORE -> core
            AmountTier.DEPENDANT -> dependant
            else -> {
                unknown
            }
        }
}
