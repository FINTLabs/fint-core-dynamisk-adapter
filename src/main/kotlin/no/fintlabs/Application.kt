package no.fintlabs

import no.fintlabs.dynamiskadapter.DynamicAdapterController
import no.fintlabs.dynamiskadapter.launchComposeApp
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.ConfigurableApplicationContext

lateinit var context: ConfigurableApplicationContext

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    System.setProperty("java.awt.headless", "false")
//    val bootstrapServers = KafkaBootstrap.start()
//    KafkaSingleton.init(bootstrapServers)
    context = runApplication<Application>(*args)
    println("Kafka App ID: " + context.environment.getProperty("fint.kafka.application-id"))

    // lateinit and this to make controller accessible from ConfigMenu.kt
    val controller = context.getBean(DynamicAdapterController::class.java)
    launchComposeApp(controller)
}
