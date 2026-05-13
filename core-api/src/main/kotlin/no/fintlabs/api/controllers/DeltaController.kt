package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.stereotype.Controller

@Controller(value = "/delta")
class DeltaController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    //TODO
}