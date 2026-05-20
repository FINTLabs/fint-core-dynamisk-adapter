package no.fintlabs.contract.data

data class ResourceStatus(
    val metadataCount: Int,
    val totalResources: Int,
    val resourcesByKey: Map<String, Int>,
    val registeredCapabilities: List<String>,
)
