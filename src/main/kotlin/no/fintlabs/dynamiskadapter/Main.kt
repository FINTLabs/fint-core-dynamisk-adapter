package no.fintlabs.dynamiskadapter

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() =
    application {
        Window(onCloseRequest = ::exitApplication, title = "FINT Dynamisk Adapter") {
            configMenu()
        }
    }

@Composable
@Preview
fun configMenu() {
    var orgId by remember { mutableStateOf<String>("fint-no") }
    var amountOfResources by remember { mutableStateOf<String>("4") }
    var domainContext by remember { mutableStateOf<String>("fint-core") }
    var selectedResource by remember { mutableStateOf<String>("utdanning-elev") }
    var resourceMenuOpen by remember { mutableStateOf<Boolean>(false) }
    var currentErrorMessage by remember { mutableStateOf<String?>(null) }
    val resourceOptionList =
        listOf(
            "utdanning-elev",
            "utdanning-elevfravar",
        )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "FINT Dynamisk Adapter",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.h4,
        )
        Text(text = "Denne tjenesten lager Kafka Topics du kan utnytte til lokal testing av tjenester under utvikling.")
        Text(text = "Fyll inn ønskede dataparameter under. Om du ikke velger blir default verdiene brukt.")
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Kafka configuration:",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.h5,
                )
                TextField(
                    value = orgId,
                    onValueChange = { orgId = it },
                    label = { Text("orgId:") },
                )
                TextField(
                    value = domainContext,
                    onValueChange = { domainContext = it },
                    label = { Text("domainContext:") },
                )
                TextField(
                    value = amountOfResources,
                    onValueChange = { amountOfResources = it },
                    label = { Text("Antall ønskede Resurser") },
                )
                Text("Selected Resource: ")
                Text(selectedResource, style = MaterialTheme.typography.h6)
                Button(onClick = { resourceMenuOpen = !resourceMenuOpen }) {
                    Text("select another resource")
                }
                DropdownMenu(
                    expanded = resourceMenuOpen,
                    onDismissRequest = { resourceMenuOpen = false },
                ) {
                    resourceOptionList.forEach { resourceOption ->
                        DropdownMenuItem(onClick = {
                            selectedResource = resourceOption
                            resourceMenuOpen = false
                        }) {
                            Text(resourceOption)
                        }
                    }
                }
            }
            Column(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (currentErrorMessage != null) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .border(2.dp, Color.Red)
                                .padding(16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text("ERROR:", style = MaterialTheme.typography.h6)
                            Text(currentErrorMessage!!)
                        }
                    }
                }
            }
        }
    }
}
