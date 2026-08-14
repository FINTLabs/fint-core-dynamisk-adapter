package no.fintlabs.api

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication(scanBasePackages = ["no.fintlabs"])
@ConfigurationPropertiesScan(basePackages = ["no.fintlabs"])
class CoreApiApplication

fun main(args: Array<String>) {
    runApplication<CoreApiApplication>(*args)
}
