package no.fintlabs.dynamiskadapter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import no.fintlabs.dynamiskadapter.kafka.KafkaBootstrap
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import java.awt.Dimension

@SpringBootApplication
class Application

fun main(args: Array<String>) {
    Thread {
        runApplication<Application>(*args)
        KafkaBootstrap.start()
    }.start()

    application {
        Window(
            onCloseRequest = {
                KafkaBootstrap.stop()
                exitApplication()
            },
            title = "FINT Dynamisk Adapter",
        ) {
            window.minimumSize = Dimension(500, 700)
            configMenu()
        }
    }
}
