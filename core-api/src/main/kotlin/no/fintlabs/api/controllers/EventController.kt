package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.runtime.model.EventFetchCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    @PostMapping("/check")
    fun checkForEvents() = runtime.submit(EventFetchCommand())
}