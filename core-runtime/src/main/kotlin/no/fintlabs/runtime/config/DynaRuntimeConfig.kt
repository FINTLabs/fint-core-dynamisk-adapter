package no.fintlabs.runtime.config

import no.fintlabs.contract.data.ConfigAmountTierPolicy
import no.fintlabs.contract.data.DualInt
import no.fintlabs.contract.models.ResourceIdentifiers
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dyna.runtime")
data class DynaRuntimeConfig(
    val startupDomains: List<String> = listOf("utdanning"),
    val enableDeltaSync: Boolean = false,
    val resetEveryNight: Boolean = false,
    val amountTierPolicy: ConfigAmountTierPolicy =
        ConfigAmountTierPolicy(
            grouping = DualInt(1, 2),
            core = 10,
            dependant = DualInt(20, 30),
            unknown = DualInt(1, 2)
        ),
    val fintProperties: FintProperties = FintProperties(),
    val deltaConfig: DeltaConfig = DeltaConfig(),
)

data class FintProperties(
    val maxPageSize: Int = 1000,
    val heartbeatIntervalInMinutes: Int = 3,
    val eventCheckIntervalInMinutes: Int = 5,
)

data class DeltaConfig(
    val deltaSyncIntervalInMinutes: Int = 10,
    val resources: List<DeltaResourceConfig> = emptyList(),
    val amountTierPolicy: ConfigAmountTierPolicy =
        ConfigAmountTierPolicy(
            grouping = DualInt(1, 2),
            core = 10,
            dependant = DualInt(20, 30),
            unknown = DualInt(1, 2)
        )
)

data class DeltaResourceConfig(
    val resource: String,
    val min: Int? = null,
    val max: Int? = null,
) {
    fun toIdentifiers() {
        val parts = resource.split("/")
        ResourceIdentifiers(parts[1], parts[2], parts[3])
    }
}

fun Map<String, IntRange?>.toDeltaResourceConfigList(): List<DeltaResourceConfig> {
    val result = mutableListOf<DeltaResourceConfig>()
    for ((key, range) in this) {
        result.add(
            DeltaResourceConfig(
                resource = key,
                min = range?.first(),
                max = range?.last(),
            )
        )
    }
    return result
}