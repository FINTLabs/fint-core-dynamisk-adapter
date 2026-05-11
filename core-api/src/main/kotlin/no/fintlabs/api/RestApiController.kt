package no.fintlabs.api

import no.fint.model.resource.FintResource
import no.fintlabs.metamodel.MetamodelService
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class RestApiController(
    private val service: DynamicAdapterRuntimeService,
) {
    
}
