package no.fintlabs.runtime.config

import no.fintlabs.contract.data.AmountTierPolicy
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("dyna.runtime")
class DynaRuntimeConfig {
    var startupDomains: List<String> = listOf("utdanning")
    var amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
}