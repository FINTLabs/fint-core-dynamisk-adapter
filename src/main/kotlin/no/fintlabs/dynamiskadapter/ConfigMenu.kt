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
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import no.fintlabs.dynamiskadapter.constructors.dynamic.DynamicAdapterService
import no.fintlabs.dynamiskadapter.constructors.dynamic.ResourceEnum
import no.fintlabs.dynamiskadapter.constructors.premade.utdanning.elev.elevFactory
import no.fintlabs.dynamiskadapter.constructors.premade.utdanning.vurdering.NoStudentsException
import no.fintlabs.dynamiskadapter.constructors.premade.utdanning.vurdering.fravarsRegistreringFactory
import no.fintlabs.dynamiskadapter.kafka.makeKafkaTopic
import no.fintlabs.dynamiskadapter.util.uiRelated.BoxType
import no.fintlabs.dynamiskadapter.util.uiRelated.infoBox
import no.fintlabs.dynamiskadapter.util.uiRelated.safeSerialize

@Composable
fun configMenu(service: DynamicAdapterService) {
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

    fun runDynamicAdapterCreateFunction() {
        // TODO: OPPKLARING: Hente modell fra metamodel isteden for hardkodet Enum?
//        val component = metamodel.getComponent("utdanning-elev")

        val data = service.create(ResourceEnum.UTDANNING_VURDERING_FRAVARSREGISTRERING, 20)
        newestDataset = data.joinToString(separator = ",\n") { safeSerialize(it, gson) }
    }

    val resourceOptionList =
        listOf(
            "utdanning-elev",
            "utdanning-fravarsregistrering",
        )

    fun createData() {
        currentErrorMessage = emptyList()
        headsUpInformation = emptyList()
        if (amountOfResources.toIntOrNull() != null) {
            when (selectedResource) {
                "utdanning-elev" -> {
                    val data = elevFactory(amountOfResources.toInt(), orgId, domainContext)
                    headsUpInformation =
                        listOf(
                            "Data also added to: ",
                            makeKafkaTopic(orgId, domainContext, "utdanning-elev-person"),
                            "and: ",
                            makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevforhold"),
                        )
                    newestDataset = gson.toJson(data)
                }
                "utdanning-fravarsregistrering" -> {
                    try {
                        val data = fravarsRegistreringFactory(amountOfResources.toInt(), orgId, domainContext)
                        headsUpInformation =
                            listOf(
                                "Data also added to: ",
                                makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevfravar"),
                                "Data updated in: ",
                                makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevforhold"),
                            )
                        newestDataset = gson.toJson(data)
                    } catch (ex: NoStudentsException) {
                        currentErrorMessage = listOf(ex.message!!)
                    }
                }
            }
        } else {
            currentErrorMessage = listOf("Antall ønskede Resurser må være ett tall")
        }
    }

    var searchQuery by remember { mutableStateOf("") }

    @Composable
    fun ResourceSearch() {
        val allResources = ResourceEnum.entries.toList()
        val filteredResources =
            allResources.filter {
                it.name.contains(searchQuery, ignoreCase = true)
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
                Button(
                    onClick = { resourceMenuOpen = !resourceMenuOpen },
                    modifier =
                        Modifier
                            .fillMaxWidth(),
                ) {
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
                Button(
                    onClick = { createData() },
                    modifier =
                        Modifier
                            .padding(top = 16.dp)
                            .height(48.dp)
                            .fillMaxWidth(),
                ) {
                    Text("Produser data til Kafka", fontWeight = FontWeight.Bold)
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
                Button(
                    onClick = { runDynamicAdapterCreateFunction() },
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
