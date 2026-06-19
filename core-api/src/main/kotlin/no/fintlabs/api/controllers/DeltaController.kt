package no.fintlabs.api.controllers

import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.dto.AmountTierPolicyRequest
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/delta")
class DeltaController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    @PostMapping("/addResources")
    fun addResources(
        @RequestBody(required = true)
        resources: Map<ResourceIdentifiers, IntRange?>
    ) = runtime.addDeltaSyncResources(resources)

    @PostMapping("/setResources")
    fun setResources(
        @RequestBody(required = true)
        resources: Map<ResourceIdentifiers, IntRange?>
    ) = runtime.setDeltaSyncResources(resources)

    @PatchMapping("/setInterval")
    fun setInterval(
        @RequestBody(required = true)
        interval: Int
    ) = runtime.setDeltaSyncInterval(interval)
    
    @PatchMapping("/resetInterval")
    fun resetInterval() = runtime.resetDeltaSyncInterval()

    @PostMapping("/enable")
    fun enable() = runtime.setEnableDeltaSync()

    @PostMapping("/disable")
    fun disable() = runtime.setDisableDeltaSync()

    @PatchMapping("/setAmountTierPolicy")
    suspend fun setAmountTierPolicy(
        @RequestBody(required = true)
        body: AmountTierPolicyRequest
    ) {
        val unknown =
            if (
                body.unknownMin != null &&
                body.unknownMax != null
            ) {
                body.unknownMin!!..body.unknownMax!!
            } else {
                body.groupingMin..body.groupingMax
            }
        runtime.setAmountTierPolicy(
            AmountTierPolicy(
                grouping = body.groupingMin..body.groupingMax,
                core = body.core..body.core,
                dependant = body.dependantMin..body.dependantMax,
                unknown = unknown,
            )
        )
    }

    @PatchMapping("/resetAmountTierPolicy")
    suspend fun resetAmountTierPolicy() = runtime.resetAmountTierPolicy()

}