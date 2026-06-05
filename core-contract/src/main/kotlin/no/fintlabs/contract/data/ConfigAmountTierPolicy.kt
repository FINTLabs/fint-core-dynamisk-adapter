package no.fintlabs.contract.data

data class ConfigAmountTierPolicy(
    val grouping: DualInt,
    val core: Int,
    val dependant: DualInt,
    val unknown: DualInt,
) {
    fun toAmountTierPolicy(): AmountTierPolicy =
        AmountTierPolicy(
            grouping = IntRange(this.grouping.min, this.grouping.max),
            core = IntRange(this.core, this.core),
            dependant = IntRange(this.dependant.min, this.dependant.max),
            unknown = IntRange(this.unknown.min, this.unknown.max),
        )
}
