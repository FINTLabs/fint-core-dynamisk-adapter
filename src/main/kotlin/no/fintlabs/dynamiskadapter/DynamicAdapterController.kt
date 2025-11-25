package no.fintlabs.dynamiskadapter

import no.fint.model.resource.FintResource
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
    fun getComponents(): List<String> = model.getComponents().map { it.name }

    fun getResources(component: String): List<String> =
        model.getComponent(component)?.let { component ->
            component.resources.map { resource ->
                resource.name
            }
        } ?: emptyList()

    fun generateResources(
        component: String,
        resource: String,
    ): List<FintResource> {
        println("⚠️ DynamicAdapterController generating \"$component/$resource\" ")
        val resourceModelClass: Class<out FintResource>? = model.getResource(component, resource)?.resourceType

        return service.create(resourceModelClass!!, "$component/$resource", 2)
    }
}
