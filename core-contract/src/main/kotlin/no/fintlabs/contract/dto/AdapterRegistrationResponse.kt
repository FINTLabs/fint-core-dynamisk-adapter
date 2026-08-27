package no.fintlabs.contract.dto

data class AdapterRegistrationResponse(
    val registered: Boolean,
    val offline: Boolean,
    val eventCheckIntervalMinutes: Int,
)
