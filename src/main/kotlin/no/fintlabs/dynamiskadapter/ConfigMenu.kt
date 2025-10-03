package no.fintlabs.dynamiskadapter

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.gson.GsonBuilder
import no.fintlabs.dynamiskadapter.constructors.utdanning.elev.elevFactory
import no.fintlabs.dynamiskadapter.constructors.utdanning.vurdering.NoStudentsException
import no.fintlabs.dynamiskadapter.constructors.utdanning.vurdering.fravarsRegistreringFactory
import no.fintlabs.dynamiskadapter.util.infoBox
import no.fintlabs.dynamiskadapter.util.makeKafkaTopic

@Composable
fun configMenu() {
    val gson = GsonBuilder().setPrettyPrinting().create()
    var orgId by remember { mutableStateOf<String>("fint-no") }
    var amountOfResources by remember { mutableStateOf<String>("2") }
    var domainContext by remember { mutableStateOf<String>("fint-core") }
    var selectedResource by remember { mutableStateOf<String>("utdanning-elev") }
    var resourceMenuOpen by remember { mutableStateOf<Boolean>(false) }
    var currentErrorMessage by remember { mutableStateOf<String?>(null) }
    var headsUpInformation by remember { mutableStateOf<String?>(null) }
    val resourceOptionList =
        listOf(
            "utdanning-elev",
            "utdanning-fravarsregistrering",
        )

    var newestDataset by remember { mutableStateOf<String>("") }

    fun createData() {
        currentErrorMessage = null
        headsUpInformation = null
        if (amountOfResources.toIntOrNull() != null) {
            when (selectedResource) {
                "utdanning-elev" -> {
                    val data = elevFactory(amountOfResources.toInt(), orgId, domainContext)
                    headsUpInformation = """
                        Data also added to
                        ${makeKafkaTopic(orgId, domainContext, "utdanning-elev-person")}
                        and ${makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevforhold")} """
                    newestDataset = gson.toJson(data)
                }
                "utdanning-fravarsregistrering" -> {
                    try {
                        val data = fravarsRegistreringFactory(amountOfResources.toInt(), orgId, domainContext)
                        headsUpInformation = """
                            Data also added to
                            ${makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevfravar")}.
                            Data updated in ${makeKafkaTopic(orgId, domainContext, "utdanning-vurdering-elevforhold")} """
                        newestDataset = gson.toJson(data)
                    } catch (ex: NoStudentsException) {
                        currentErrorMessage = ex.message
                    }
                }
            }
        } else {
            currentErrorMessage = "Antall ønskede Resurser må være ett tall"
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
        Text(text = "Kafka containeren kjører på localhost:62763.")
        Row(
            modifier = Modifier.fillMaxWidth().weight(1f),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Column(
                //
                // ## CONTROL COLUMN ##
                //
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
                Button(onClick = { createData() }) {
                    Text("Produser data til Kafka")
                }
            }
            Column(
                //
                // ## DATA COLUMN ##
                //
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                if (currentErrorMessage != null) {
                    infoBox("Error", currentErrorMessage!!)
                }
                if (headsUpInformation != null) {
                    infoBox("", headsUpInformation!!)
                }
                Box(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                ) {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text("Newest dataset preview:")
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
