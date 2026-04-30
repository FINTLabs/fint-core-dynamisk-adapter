package no.fintlabs.dynamiskadapter

import no.novari.fint.model.resource.FintResource

data class StoredResource(
    val id: String,
    val resource: FintResource,
)
