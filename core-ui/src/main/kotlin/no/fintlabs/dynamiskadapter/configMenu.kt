@file:Suppress("ktlint:standard:no-wildcard-imports")

package no.fintlabs.dynamiskadapter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import no.fintlabs.dynamiskadapter.util.BoxType
import no.fintlabs.dynamiskadapter.util.infoBox
import no.fintlabs.dynamiskadapter.util.parseFintResources
import no.fintlabs.dynamiskadapter.util.searchBar

@Composable
fun configMenu(api: MetamodelApiClient) {
    val scope = rememberCoroutineScope()
    var orgId by remember { mutableStateOf("fint-no") }
    var domainContext by remember { mutableStateOf("fint-core") }
    val currentErrorMessage = remember { mutableStateOf(emptyList<String>()) }
    var headsUpInformation by remember { mutableStateOf(listOf<String>()) }
    var newestDataset by remember { mutableStateOf("") }
    val gson: Gson =
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()

    var amountOfResources by remember { mutableStateOf("2") }
    var allComponents by remember { mutableStateOf(emptyList<String>()) }
    var allResources by remember { mutableStateOf(emptyList<String>()) }
    val selectedComponent = remember { mutableStateOf("") }
    val selectedResource = remember { mutableStateOf("") }
    var backendStatus by remember { mutableStateOf("⚠️") }

    LaunchedEffect(Unit) {

        while (true) {
            try {
                val ping = api.ping()
                if (ping == "ok") {
                    backendStatus = "✅"
                    break
                }
            } catch (e: Exception) {
                delay(500)
            }
        }

        try {
            allComponents = api.getAllComponents()
        } catch (e: Exception) {
            currentErrorMessage.value.apply { e.message ?: "unknown Error" }
        }
    }

    fun selectComponent(component: String) {
        selectedComponent.value = component
        selectedResource.value = ""
        scope.launch {
            allResources = api.getResources(component)
        }
    }

    fun runCreateFunction() {
        if (selectedResource.value == "") {
            currentErrorMessage.value = mutableListOf("Please select a resource")
        } else {
            if (amountOfResources.toIntOrNull() == null) {
                currentErrorMessage.value = mutableListOf("Amount of Resources is not a valid number.")
            } else {
                scope.launch {
                    val rawJson = api.createResources(
                        resource = selectedResource.value,
                        component = selectedComponent.value,
                        count = amountOfResources.toInt()
                    )
                    val data = parseFintResources(rawJson, gson)
                    newestDataset = data
                }
            }
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
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "backend Status = $backendStatus",
                )


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

                        SelectionContainer {
                            Text(
                                text = newestDataset,
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .verticalScroll(scrollState)
                                        .border(2.dp, Color.Gray)
                                        .padding(16.dp),
                            )
                        }
                    }
                }
            }
        }
    }
}