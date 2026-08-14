package no.fintlabs.contract.models

data class HeartBeatRequest(
    val adapterId: String,
    val username: String,
    val orgId: String,
    val time: Long,
)
