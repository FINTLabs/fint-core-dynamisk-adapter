package no.fintlabs.engine.config

import no.fintlabs.library.ResourceFactory
import no.fintlabs.library.ResourceFactoryConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ResourceFactoryConfiguration {

    @Bean
    fun resourceFactory(props: DynaEngineConfig): ResourceFactory =
        ResourceFactory(
            config = ResourceFactoryConfig(
                seed = props.seed,
                minimumRandomPoolSize = props.minimumRandomPoolSize,
                firstnameList = props.firstnameList,
                lastnameList = props.lastnameList,
                quoteList = props.quoteList,
                cityNameList = props.cityNameList,
                streetNameList = props.streetNameList,
            )
        )
}