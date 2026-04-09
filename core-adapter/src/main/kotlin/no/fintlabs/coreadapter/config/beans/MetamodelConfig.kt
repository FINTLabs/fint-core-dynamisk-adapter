package no.fintlabs.coreadapter.config.beans


import no.novari.metamodel.ComponentBuilder
import no.novari.metamodel.MetamodelService
import no.novari.metamodel.ReflectionService
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MetamodelConfig {
    @Bean
    fun metamodelService(): MetamodelService = MetamodelService(
        componentBuilder = ComponentBuilder(
            reflectionService = ReflectionService()
        )
    )
}