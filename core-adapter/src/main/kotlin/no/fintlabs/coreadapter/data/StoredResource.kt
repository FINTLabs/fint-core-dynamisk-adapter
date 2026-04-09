package no.fintlabs.coreadapter.data

import no.novari.fint.model.resource.FintResource

data class StoredResource(
    val id: String,
    val resource: FintResource,
)
