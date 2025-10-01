package no.fintlabs.dynamiskadapter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
    var orgId by remember { mutableStateOf<String>("fint.no") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FINT Dynamisk Adapter",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.h5,
        )
        Text(text = "Denne tjenesten lager Kafka Topics du kan utnytte til lokal texting av tjenester under utvikling.")
        Text(text = "")
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(text = "Kafka configuration:")
                TextField(
                    value = orgId,
                    onValueChange = { orgId = it },
                )

                Text(text = "Data configuration:")
                TextField(
                    value = orgId,
                    onValueChange = { orgId = it },
                    label = { Text("orgId") },
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
            }
        }
    }
}
