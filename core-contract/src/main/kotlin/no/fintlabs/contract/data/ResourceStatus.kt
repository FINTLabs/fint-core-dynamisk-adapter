package no.fintlabs.contract.data

data class ResourceStatus(
    val metadataCount: Int,
    val totalResources: Int,
    val percentageOfMaxGenerated: Double,
    val resourcesByKey: Map<String, Int>,
    val registeredCapabilities: List<String>,
)
