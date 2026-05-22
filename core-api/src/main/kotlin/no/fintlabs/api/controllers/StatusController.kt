package no.fintlabs.api.controllers

import no.fintlabs.contract.data.DynaGeneralStatusResponse
import no.fintlabs.contract.data.RuntimeJobStatus
import no.fintlabs.runtime.status.DynamicAdapterStatusService
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping

@Controller("/status")
class StatusController(
    private val statusService: DynamicAdapterStatusService,
) {
    @GetMapping
    fun status(): DynaGeneralStatusResponse =
        statusService.status()

    @GetMapping("/jobs/current")
    fun currentJobs(): List<RuntimeJobStatus> =
        statusService.currentJobs()

    @GetMapping("/jobs/history")
    fun jobHistory(): List<RuntimeJobStatus> =
        statusService.allJobs()
}