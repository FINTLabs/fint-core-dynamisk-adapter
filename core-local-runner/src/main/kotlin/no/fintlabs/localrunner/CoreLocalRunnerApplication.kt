package no.fintlabs.localrunner

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class CoreLocalRunnerApplication

fun main(args: Array<String>) {
    runApplication<CoreLocalRunnerApplication>(*args)
}
