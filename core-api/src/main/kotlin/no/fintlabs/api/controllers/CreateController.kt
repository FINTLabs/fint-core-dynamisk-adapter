package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.stereotype.Controller

@Controller(value = "/create")
class CreateController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    //TODO
}