package no.fintlabs.dynamiskadapter

import jakarta.annotation.PostConstruct
import no.fintlabs.dynamiskadapter.constructors.dynamic.DynamicAdapterService
import no.fintlabs.metamodel.MetamodelService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping

@Controller
class DynamicAdapterController(
    private val service: DynamicAdapterService,
    private val model: MetamodelService,
) {
    // GETS RESOURCE MODEL CLASSES
    @PostConstruct
    fun getComponents() =
        model.getComponents().forEach {
            println(it.name)
        }

    fun getResource(component: String): List<String> =
        model.getComponent(component)?.let { component ->
            component.resources.map { resource ->
                println(resource.name)
                resource.name
            }
        } ?: emptyList()

    @PostMapping("{component}/{resource}")
    fun generateResources(
        @PathVariable component: String,
        @PathVariable resource: String,
    ) {
        model.getResource(component, resource)?.let { resource ->

            resource.resourceType

            // Relations TBD
            resource.relations
        }
    }
}
