package no.fintlabs.dynamiskadapter.data

import no.fintlabs.dynamiskadapter.IdFieldType

data class IdMetadata(
    val prefix: String,
    val type: IdFieldType,
)