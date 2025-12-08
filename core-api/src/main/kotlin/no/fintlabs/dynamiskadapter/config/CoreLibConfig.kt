package no.fintlabs.dynamiskadapter.config

import no.fintlabs.dynamiskadapter.DynamicAdapterService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoreLibConfig {
    @Bean
    fun dynamicAdapterService() = DynamicAdapterService()
}