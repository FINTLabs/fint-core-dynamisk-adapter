package no.fintlabs.runtime.config

import no.fintlabs.contract.data.AmountTierPolicy
import org.springframework.context.annotation.Configuration

@Configuration
class DynamicAdapterConfig {
    val startupDomains: List<String> = listOf("utdanning")
    val amountTierPolicy: AmountTierPolicy =
        AmountTierPolicy(
            grouping = 1..2,
            core = 10..10,
            dependant = 20..30,
            unknown = 1..2,
        )
}