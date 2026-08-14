package no.fintlabs.engine

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan(basePackages = ["no.fintlabs"])
class CoreEngineApplication

fun main(args: Array<String>) {
    runApplication<CoreEngineApplication>(*args)
}
