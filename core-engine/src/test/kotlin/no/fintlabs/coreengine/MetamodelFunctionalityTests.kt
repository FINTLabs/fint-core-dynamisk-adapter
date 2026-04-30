package no.fintlabs.coreengine

import no.fintlabs.coreengine.beans.MetamodelConfig
import no.fintlabs.coreengine.util.generateIdMetadata
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
}