package no.fintlabs.dynamiskadapter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "FINT Dynamisk Adapter") {
            app()
        }
    }

@Composable
@Preview
fun app() {
    Column {
        Text(
            text = "FINT Dynamisk Adapter",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.h5,
            textAlign = TextAlign.Center,
        )
        Text(text = "Denne tjenesten lager Kafka Topics du kan utnytte til lokal texting av tjenester under utvikling.")
        Text(text = "")
    }
}
