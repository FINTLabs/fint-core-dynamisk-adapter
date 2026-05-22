package no.fintlabs.runtime.config

import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.models.ResourceIdentifiers
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dyna.runtime")
class DynaRuntimeConfig {
    val startupDomains: List<String> = listOf("utdanning")
    val enableDeltaSync: Boolean = false
    val resetEveryNight: Boolean = false
    val amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
    val fintProperties: FintProperties = FintProperties()
    val deltaConfig: DeltaConfig = DeltaConfig()
}

data class FintProperties(
    val maxPageSize: Int = 1000,
    val heartbeatIntervalInMinutes: Int = 3,
)

data class DeltaConfig(
    val deltaSyncIntervalInMinutes: Int = 10,
    val resources: Map<ResourceIdentifiers, IntRange?> = mapOf(),
    val amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
)