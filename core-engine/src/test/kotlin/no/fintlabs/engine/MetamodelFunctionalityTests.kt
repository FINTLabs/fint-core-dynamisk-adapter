package no.fintlabs.engine

import no.fintlabs.engine.beans.MetamodelConfig
import no.fintlabs.engine.util.generateIdMetadata
import no.novari.metamodel.MetamodelService
import org.junit.jupiter.api.Test

class MetamodelFunctionalityTests {

    private val model: MetamodelService = MetamodelConfig().metamodelService()

    @Test
    fun `print link prefix for every resource`() {
        model.getResources()
            .sortedBy { it.resourceClass.simpleName }
            .forEach { resource ->
                println("${resource.generateIdMetadata()} -> ${resource.resourceClass.simpleName}")
            }
    }
}