package no.fintlabs.dynamiskadapter

import no.fint.model.resource.FintResource
import no.fintlabs.metamodel.MetamodelService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class DynamiskAdapterController(
    private val service: DynamicAdapterService,
    private val model: MetamodelService,
) {
    @GetMapping("/api/getAllComponents")
    fun getAllComponents(): List<String> = model.getComponents().map { it.name }

    @GetMapping("/api/getResources")
    fun getResources(@RequestParam component: String): List<String> =
        model.getComponent(component)?.let { component ->
            component.resources.map { resource ->
                resource.name
            }
        } ?: emptyList()

    @GetMapping("/api/ping")
    fun ping(): String = "ok"

    @PostMapping("/api/create")
    fun create(
        @RequestParam component: String,
        @RequestParam resource: String,
        @RequestParam count: Int
    ): List<FintResource> {
        val classname = "$component/$resource"
        val resourceModelClass = model.getResource(component, resource)

        if (resourceModelClass == null) {
            println("CREATION ERROR: model.getResource($component, $resource) returned NULL")
            throw IllegalArgumentException("Unknown component/resource: $component/$resource")
        }

        return service.create(resourceModelClass.resourceType, classname, count)
    }
}