package no.fintlabs.coreadapter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@ConfigurationPropertiesScan
@SpringBootApplication
@EnableScheduling
class CoreAdapterApplication

fun main(args: Array<String>) {
    runApplication<CoreAdapterApplication>(*args)
}
