package no.fintlabs.engine.beans

import no.fintlabs.library.ResourceFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class CoreLibConfig {
    @Bean
    fun dynamicAdapterService() = ResourceFactory()
}