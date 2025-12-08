package no.fintlabs.dynamiskadapter

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import no.fintlabs.dynamiskadapter.util.freePort
import java.io.File
import java.util.concurrent.TimeUnit


fun main() = application {

    // Makes sure port 8182 is free. This very specific port is where dynamisk-adapter's API runs.
    freePort(8182)

    val apiClient = MetamodelApiClient()

    val backend = ProcessBuilder(
        "java", "-jar", "./build/libs/core-api-1.0.0.jar"
    ).directory(File("./core-api"))
        .inheritIO()
        .start()

    Window(
        title = "Dynamisk Adapter",
        onCloseRequest = {
            println("Stopping backend...")
            backend.destroy()
            backend.waitFor(1000, TimeUnit.MILLISECONDS)
            if (backend.isAlive) {
                println("Forcing backend termination.")
                backend.destroyForcibly()
            }
            exitApplication()
        }
    ) {
        configMenu(apiClient)
    }
}