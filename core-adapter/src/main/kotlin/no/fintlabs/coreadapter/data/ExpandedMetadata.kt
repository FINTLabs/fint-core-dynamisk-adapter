package no.fintlabs.coreadapter.data

import no.fintlabs.metamodel.model.Resource

data class ExpandedMetadata(
    val resource: Resource,
    val key: String,
)
