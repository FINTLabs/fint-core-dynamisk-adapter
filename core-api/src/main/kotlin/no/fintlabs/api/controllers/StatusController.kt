package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.stereotype.Controller

@Controller("/status")
class StatusController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    //TODO
}