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
//    val bootstrapServers = KafkaBootstrap.start()
//    KafkaSingleton.init(bootstrapServers)
    context = runApplication<Application>(*args)

    //
    val controller = context.getBean(DynamicAdapterController::class.java)
    launchComposeApp(controller)
}
