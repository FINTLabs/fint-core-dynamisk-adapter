package no.fintlabs.runtime

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication(scanBasePackages = ["no.fintlabs"])
@ConfigurationPropertiesScan
class CoreRuntimeApplication

fun main(args: Array<String>) {
    runApplication<CoreRuntimeApplication>(*args)
}
