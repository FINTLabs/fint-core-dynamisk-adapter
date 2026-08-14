package no.fintlabs.api.controllers

import no.fintlabs.contract.dto.DynaGeneralStatusResponse
import no.fintlabs.contract.data.RuntimeJobStatus
import no.fintlabs.runtime.status.DynamicAdapterStatusService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/status")
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