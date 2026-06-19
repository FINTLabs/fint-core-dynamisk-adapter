package no.fintlabs.api.controllers

import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.dto.AmountTierPolicyRequest
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.runtime.model.CreateDataCommand
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
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
        resources: Map<ResourceIdentifiers, Int>
    ): String {
        val activeDomains = runtime.getActiveDomains()
        for (resource in resources.keys) {
            if (!activeDomains.contains(resource.domain)) return "ERROR: $resource is not in active domains. \n " +
                    "update dataset to contain ${resource.domain} if you wish to generate this resource."
        }
        return runtime.submit(
            CreateDataCommand(
                resources = resources,
            )
        )
    }

    @PatchMapping("/update-dataset")
    suspend fun updateDataset(
        @RequestBody(required = true)
        domains: List<String>
    ) {
        runtime.updateDataset(domains)
        // TODO: /data/updateDataset
        // replace current dataset with new dataset. If new set
        // does not include current, remove current data,

        // If new datasets contain non-registered resources, re-register
    }

    @PatchMapping("/reset-dataset")
    suspend fun resetDataset(): String {
        return runtime.resetDataset()
    }

    @PostMapping("/reset-data")
    suspend fun resetData() = runtime.hardReset()

    @PatchMapping("/set-Amount-tier-policy")
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
        runtime.setDeltaAmountTierPolicy(
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
}