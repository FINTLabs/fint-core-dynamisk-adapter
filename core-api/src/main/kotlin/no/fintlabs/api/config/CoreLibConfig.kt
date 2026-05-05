package no.fintlabs.api.config

import no.fintlabs.contract.DynamicAdapterService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoreLibConfig {
    @Bean
    fun dynamicAdapterService() = DynamicAdapterService()
}