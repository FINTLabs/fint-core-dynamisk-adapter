package no.fintlabs.contract.data

data class DualInt(
    val min: Int = 0,
    val max: Int = 0,
) {
    fun toRange(): IntRange = IntRange(min, max)
}