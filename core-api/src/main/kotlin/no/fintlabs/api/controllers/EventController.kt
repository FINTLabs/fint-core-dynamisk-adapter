package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.stereotype.Controller

@Controller("/event")
class EventController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    //TODO
}