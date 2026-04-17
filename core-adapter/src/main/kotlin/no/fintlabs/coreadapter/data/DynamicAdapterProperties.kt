package no.fintlabs.coreadapter.data

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dynamisk-adapter")
data class DynamicAdapterProperties(
    val recursive: Boolean = false,
    val link: Boolean = true,
    val consoleLogging: Boolean = false,
    val consoleLogDataset: Boolean = false,
    val enableDeltaSync: Boolean = false,
    val localLogicTest: Boolean = false,
    val maxPageSize: Int = 1000,
    val initialDataSets: List<InitialDataset>,
    val fullSyncIntervalInDays: Int = 0,
    val deltaSyncIntervalInMinutes: Int? = null,
    val errorPercentage: Int = 0,
    val logErrorBreakdown: Boolean = false,
    val deltaSyncDataSets: List<DeltaSyncDataset> = mutableListOf(),
)

data class InitialDataset(
    val name: String,
    val component: String,
    val resource: String,
    val count: Int,
) {
    val resourceKey: String = "${component.replace(".", "/")}/$resource"
}

data class DeltaSyncDataset(
    val name: String,
    val component: String,
    val resource: String,
    val minSize: Int,
    val maxSize: Int,
) {
    val resourceKey: String = "${component.replace(".", "/")}/$resource"
}
