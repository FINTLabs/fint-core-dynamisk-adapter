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
import no.fintlabs.dynamiskadapter.kafka.KafkaBootstrap
import no.fintlabs.dynamiskadapter.util.uiRelated.BoxType
import no.fintlabs.dynamiskadapter.util.uiRelated.infoBox
import no.fintlabs.dynamiskadapter.util.uiRelated.safeSerialize
import java.awt.Dimension

fun launchComposeApp(controller: DynamicAdapterController) {
    application {
        Window(
            onCloseRequest = {
                KafkaBootstrap.stop()
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
    var selectedResource by remember { mutableStateOf<String>("utdanning-elev") }
    var resourceMenuOpen by remember { mutableStateOf<Boolean>(false) }
    var currentErrorMessage by remember { mutableStateOf(listOf<String>()) }
    var headsUpInformation by remember { mutableStateOf(listOf<String>()) }
    var newestDataset by remember { mutableStateOf<String>("") }
    val gson: Gson =
        GsonBuilder()
            .setPrettyPrinting()
            .create()

//    fun runDynamicAdapterCreateFunction() {
//        val component = metamodel.getComponent("utdanning-elev")
//
//        val data = service.create()
//        newestDataset = data.joinToString(separator = ",\n") { safeSerialize(it, gson) }
//    }

    var searchQuery by remember { mutableStateOf("") }

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
                Button(
                    onClick = { },
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .height(48.dp)
                            .fillMaxWidth(),
                ) {
                    Text("Test DynamiskAdapterService til konsoll", fontWeight = FontWeight.Bold)
                }

                if (currentErrorMessage.isNotEmpty()) {
                    infoBox(BoxType.ERROR, currentErrorMessage)
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
