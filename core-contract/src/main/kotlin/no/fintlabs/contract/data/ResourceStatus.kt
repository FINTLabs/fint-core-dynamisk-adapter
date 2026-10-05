package no.fintlabs.contract.data

data class ResourceStatus(
    val metadataCount: Int,
    val totalResources: Int,
    val maxGeneratedResources: Int,
    val percentageOfMaxGenerated: String,
    val resourcesByKey: Map<String, Int>,
    val registeredCapabilities: List<String>,
)
