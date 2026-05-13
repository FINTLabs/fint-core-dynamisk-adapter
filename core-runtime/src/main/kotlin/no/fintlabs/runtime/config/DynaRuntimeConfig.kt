package no.fintlabs.runtime.config

import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.models.ResourceIdentifiers
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dyna.runtime")
class DynaRuntimeConfig {
    var seed: String = ""
    var startupDomains: List<String> = listOf("utdanning")
    var enableDeltaSync: Boolean = false
    var resetEveryNight: Boolean = false
    var amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
    var fintProperties: FintProperties = FintProperties()
    var deltaConfig: DeltaConfig = DeltaConfig()
}

data class FintProperties(
    var maxPageSize: Int = 1000,
    var maxGeneratedResources: Int = 10000,
    var heartbeatIntervalInMinutes: Int = 3,
)

data class DeltaConfig(
    var deltaSyncIntervalInMinutes: Int = 10,
    var resources: List<ResourceIdentifiers> = listOf(),
    var amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
)