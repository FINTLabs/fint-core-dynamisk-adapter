package no.fintlabs.coreadapter.data

data class DeltaSyncDataset(
    val name: String,
    val component: String,
    val resource: String,
    val minSize: Int,
    val maxSize: Int,
)