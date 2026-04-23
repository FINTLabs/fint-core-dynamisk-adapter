package no.fintlabs.coreadapter

import no.fintlabs.coreadapter.config.beans.MetamodelConfig
import no.fintlabs.coreadapter.util.generateIdPrefix
import no.novari.metamodel.MetamodelService
import org.junit.jupiter.api.Test


class MetamodelFunctionalityTests {

    private val model: MetamodelService = MetamodelConfig().metamodelService()

    @Test
    fun `print prefix for every resource`() {
        model.getResources()
            .sortedBy { it.resourceClass.simpleName }
            .forEach { resource ->
                println("${resource.generateIdPrefix()} -> ${resource.resourceClass.simpleName}")
            }
    }
}