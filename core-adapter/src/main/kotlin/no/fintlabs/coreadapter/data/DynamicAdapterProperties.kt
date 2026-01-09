package no.fintlabs.coreadapter.data

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
}

data class DeltaSyncSetup(
    val deltaSyncIntervalParameters: DeltaSyncIntervalParameters? = null,
    val dataSets: List<DeltaSyncDataset>,
)

data class DeltaSyncIntervalParameters(
    val minMinutes: Int = 1,
    val maxMinutes: Int = 5,
)
