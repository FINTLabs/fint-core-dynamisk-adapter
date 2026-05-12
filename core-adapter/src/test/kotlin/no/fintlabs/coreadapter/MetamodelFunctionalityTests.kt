package no.fintlabs.coreadapter

import no.fintlabs.coreadapter.config.beans.MetamodelConfig
import no.fintlabs.coreadapter.util.generateIdMetadata
import no.fintlabs.dynamiskadapter.DynamicAdapterService
import no.novari.fint.model.resource.utdanning.elev.ElevResource
import no.novari.metamodel.MetamodelService
import org.junit.jupiter.api.Test

class MetamodelFunctionalityTests {

    private val model: MetamodelService = MetamodelConfig().metamodelService()

    @Test
    fun `print prefix for every resource`() {
        model.getResources()
            .sortedBy { it.resourceClass.simpleName }
            .forEach { resource ->
                println("${resource.generateIdMetadata()} -> ${resource.resourceClass.simpleName}")
            }
    }

    @Test
    fun `DynA core-lib demo`() {
        val service = DynamicAdapterService()

        val toElever = service.create(ElevResource::class.java, 2)
        println(toElever)

    }
}