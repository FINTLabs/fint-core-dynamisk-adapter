@file:Suppress("ktlint:standard:no-wildcard-imports")

package no.fintlabs.dynamiskadapter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.fintlabs.dynamiskadapter.util.uiRelated.BoxType
import no.fintlabs.dynamiskadapter.util.uiRelated.infoBox
import no.fintlabs.dynamiskadapter.util.uiRelated.safeSerialize
import searchBar
import java.awt.Dimension

fun launchComposeApp(controller: DynamicAdapterController) {
    application {
        Window(
            onCloseRequest = {
//                KafkaBootstrap.stop()
                exitApplication()
            },
            title = "FINT Dynamisk Adapter",
        ) {
            window.minimumSize = Dimension(800, 800)
            configMenu(controller)
        }
    }
}

@Composable
fun configMenu(controller: DynamicAdapterController) {
    var orgId by remember { mutableStateOf<String>("fint-no") }
    var amountOfResources by remember { mutableStateOf<String>("2") }
    var domainContext by remember { mutableStateOf<String>("fint-core") }
    val currentErrorMessage = remember { mutableStateOf(emptyList<String>()) }
    var headsUpInformation by remember { mutableStateOf(listOf<String>()) }
    var newestDataset by remember { mutableStateOf<String>("") }
    val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

//        val data = service.create()
//        newestDataset = data.joinToString(separator = ",\n") { safeSerialize(it, gson) }
//    }

    val allComponents: List<String> = controller.getComponents()
    val selectedComponent = remember { mutableStateOf("") }
    val selectedResource = remember { mutableStateOf("") }
    var allResources by remember { mutableStateOf(emptyList<String>()) }

    fun selectComponent(component: String) {
        selectedComponent.value = component
        allResources = controller.getResources(component)
        selectedResource.value = ""
    }

    fun runCreateFunction() {
        if (selectedResource.value == "") {
            currentErrorMessage.value = mutableListOf("Please select a resource")
        } else {
            val data = controller.generateResources(resource = selectedResource.value, component = selectedComponent.value)
            newestDataset = data.joinToString(separator = ",\n") { safeSerialize(it, gson) }
        }
    }

    Column(
        //
        // ## HEADER ##
        //
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
                //
                // ## CONTROL COLUMN ##
                //
                modifier = Modifier.fillMaxWidth().weight(0.40f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                searchBar(
                    label = "Component: ",
                    allItems = allComponents,
                    onSelectItem = { selectComponent(it) },
                )

                searchBar(
                    label = "Resource: ",
                    allItems = allResources,
                    onSelectItem = { selectedResource.value = it },
                )

                TextField(
                    value = amountOfResources,
                    onValueChange = { amountOfResources = it },
                    label = { Text("Amount: ") },
                )

                Button(
                    onClick = { runCreateFunction() },
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .height(48.dp)
                            .fillMaxWidth(),
                ) {
                    Text("Kjør funksjon", fontWeight = FontWeight.Bold)
                }
            }
            Column(
                //
                // ## DATA COLUMN ##
                //
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(0.60f)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            )
            //
            // Warnings
            //
            {
                if (currentErrorMessage.value.isNotEmpty()) {
                    infoBox(BoxType.ERROR, currentErrorMessage.value)
                }
                if (headsUpInformation.isNotEmpty()) {
                    infoBox(BoxType.INFO, headsUpInformation)
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    )
                    //
                    // Data Preview
                    //
                    {
                        Text(
                            "Newest dataset preview:",
                            style = MaterialTheme.typography.h6,
                            modifier = Modifier.padding(16.dp),
                        )
                        val scrollState = rememberScrollState()
                        Text(
                            text = newestDataset,
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(scrollState)
                                    .border(2.dp, Color.Gray),
                        )
                    }
                }
            }
        }
    }
}
