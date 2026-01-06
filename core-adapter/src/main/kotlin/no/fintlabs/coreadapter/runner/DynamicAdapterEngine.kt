package no.fintlabs.coreadapter.runner

import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.InitialDataset
import no.fintlabs.dynamiskadapter.DynamicAdapterService
import no.fintlabs.metamodel.MetamodelService

class DynamicAdapterEngine(
    private val props: DynamicAdapterProperties,
    private val model: MetamodelService,
    private val generator: DynamicAdapterService
) {
    private val capabilities: List<InitialDataset> = props.initialDataSets

    fun executeInitialDataset() {
        capabilities.forEach { it ->
            val metadata = model.getResource(it.component, it.resource)

        }

        // Generate Data from props

        if (props.links) {

            // Check Relations and Link accordingly

        }

        // Publish

    }

    private fun


}