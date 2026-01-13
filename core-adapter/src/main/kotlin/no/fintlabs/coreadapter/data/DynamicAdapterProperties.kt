package no.fintlabs.coreadapter.data

import no.fintlabs.adapter.models.AdapterCapability
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dynamisk-adapter")
data class DynamicAdapterProperties(
    val recursive: Boolean = false,
    val link: Boolean = true,
    val enableDeltaSync: Boolean = false,
    val fullSyncIntervalInMinutes: Int? = 0,
    val initialDataSets: List<InitialDataset>,
    val deltaSyncSetup: DeltaSyncSetup? = null,
)

data class InitialDataset(
    val name: String,
    val component: String,
    val resource: String,
    val count: Int,
) {
    val resourceKey: String = "${component.replace(".", "/")}/$resource"

    fun toCapability(): AdapterCapability {
        val domain: String = component.substringBefore(".")
        return AdapterCapability(
            domain,
            component,
            resource,
            1,
            AdapterCapability.DeltaSyncInterval.IMMEDIATE,
        )
    }
}

data class DeltaSyncSetup(
    val deltaSyncIntervalInMinutes: Int? = null,
    val dataSets: List<DeltaSyncDataset>,
)
