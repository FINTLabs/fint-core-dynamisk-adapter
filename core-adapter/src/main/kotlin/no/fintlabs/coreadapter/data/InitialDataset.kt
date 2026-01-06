package no.fintlabs.coreadapter.data

data class InitialDataset(
    val name: String,
    val component: String,
    val resource: String,
    val count: Int,
) {
    val resourceKey: String = "$component/$resource"
}
