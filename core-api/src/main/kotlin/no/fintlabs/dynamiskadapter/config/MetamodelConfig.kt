package no.fintlabs.dynamiskadapter.config

import no.fintlabs.metamodel.MetamodelService
import no.fintlabs.metamodel.ReflectionService
import no.fintlabs.metamodel.config.MetamodelProperties
import no.fintlabs.metamodel.mapper.ResourceMapper
import no.fintlabs.metamodel.model.builder.ComponentBuilder
import no.fintlabs.metamodel.model.builder.ResourceBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetamodelConfig {

    @Bean
    fun metamodelService(): MetamodelService = MetamodelService(
        componentBuilder = ComponentBuilder(
            metamodelProperties = MetamodelProperties(),
            resourceBuilder = ResourceBuilder(ResourceMapper(), ReflectionService())
        )
    )
}