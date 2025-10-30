package no.fintlabs.dynamiskadapter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import no.fintlabs.dynamiskadapter.constructors.dynamic.DynamicAdapterService
import no.fintlabs.dynamiskadapter.kafka.KafkaBootstrap
import no.fintlabs.dynamiskadapter.kafka.KafkaSingleton
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.awt.Dimension

@SpringBootApplication
class Application

fun main(args: Array<String>) {
//    val bootstrapServers = KafkaBootstrap.start()
//    KafkaSingleton.init(bootstrapServers)

    Thread {
        runApplication<Application>(*args)
    }.start()

    application {
        Window(
            onCloseRequest = {
//                KafkaBootstrap.stop()
                exitApplication()
            },
            title = "FINT Dynamisk Adapter",
        ) {
            window.minimumSize = Dimension(800, 800)
            configMenu(DynamicAdapterService())
        }
    }
}
