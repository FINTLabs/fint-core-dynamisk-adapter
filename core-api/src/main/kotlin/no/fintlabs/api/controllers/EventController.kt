package no.fintlabs.api.controllers

import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.runtime.model.EventFetchCommand
import no.fintlabs.runtime.model.EventHandlingCommand
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/event")
class EventController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    @PostMapping("/check")
    fun checkForEvents() = runtime.submit(EventFetchCommand())

    @PostMapping("/setInterval")
    fun setInterval(@RequestBody i: Int) = runtime.setEventInterval(i)

    @PostMapping("/resetInterval")
    fun resetInterval() = runtime.resetEventInterval()

    @PostMapping("/disable")
    fun disableAutomaticEventCheck() = runtime.disableEventCheck()
}