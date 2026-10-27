package no.fintlabs.api.controllers

import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.dto.AmountTierPolicyRequest
import no.fintlabs.contract.dto.GenerateSpecifiedValueResourceRequest
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.runtime.model.CreateDataCommand
import no.fintlabs.runtime.model.CreateSpecificDataCommand
import no.fintlabs.runtime.model.FullSyncCommand
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/data")
class DataController(
    private val runtime: DynamicAdapterRuntimeService,
) {
    @PostMapping("/generate-resources")
    fun generateResources(
        @RequestBody(required = true)
        resources: Map<String, Int>
    ): String {
        val activeDomains = runtime.getActiveDomains()
        for (resource in resources.keys) {
            val domain = resource.substringBefore("/")
            if (!activeDomains.contains(domain)) return "ERROR: $resource is not in active domains. \n " +
                    "update dataset to contain $domain if you wish to generate this resource."
        }
        return runtime.submit(
            CreateDataCommand(
                resources = resources,
            )
        )
    }

    @PostMapping("/generate-specified-value-resources")
    fun generateSpecifiedValueResources(
        @RequestBody(required = true)
        request: GenerateSpecifiedValueResourceRequest
    ) =
        runtime.submit(
            CreateSpecificDataCommand(
                specifiedResources = request.specifiedResource,
                supportingResources = request.supportingResources,
                amount = request.amount,
            )
        )

    @PostMapping("/fullsync")
    fun performFullSync() = runtime.submit(FullSyncCommand())

    @PatchMapping("/fullsync-interval-in-days-{interval}")
    fun setFullSyncInterval(@PathVariable interval: Int) = runtime.setFullSyncInterval(interval)

    @PatchMapping("/set-domains")
    suspend fun setDomains(
        @RequestBody(required = true)
        domains: List<String>
    ) = runtime.setDataset(domains)

    @PatchMapping("/reset-domains")
    suspend fun resetDomains(): String = runtime.resetDataset()

    @PostMapping("/reset-data")
    suspend fun resetData() = runtime.hardReset()

    @PatchMapping("/reset-data-nightly-{bool}")
    suspend fun setResetDataNightly(@PathVariable bool: Boolean) = runtime.setResetDataNightly(bool)

    @PatchMapping("/set-amount-tier-policy")
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

    @PatchMapping("/reset-amount-tier-policy")
    suspend fun resetAmountTierPolicy() = runtime.resetAmountTierPolicy()

    @PatchMapping("/set-max-generated-resources")
    fun setMaxGeneratedResources(
        @RequestBody(required = true)
        amount: Int
    ) = runtime.setMaxGeneratedResources(amount)

    @PatchMapping("/reset-max-generated-resources")
    fun resetMaxGeneratedResources() = runtime.resetMaxGeneratedResources()

    @PatchMapping("/seed-set")
    fun setSeed(
        @RequestBody(required = true)
        newSeed: String
    ) = runtime.setSeed(newSeed)

    @PatchMapping("/seed-reset")
    fun resetSeed() = runtime.resetSeed()

    @PatchMapping("/seed-disable")
    fun disableSeed() = runtime.setSeed(null)
}