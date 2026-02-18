package no.fintlabs.coreadapter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@ConfigurationPropertiesScan
@SpringBootApplication
class CoreAdapterApplication

fun main(args: Array<String>) {
    runApplication<CoreAdapterApplication>(*args)
}
