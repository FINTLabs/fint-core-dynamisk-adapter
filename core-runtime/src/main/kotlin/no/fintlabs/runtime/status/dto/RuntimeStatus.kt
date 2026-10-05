package no.fintlabs.runtime.status.dto

import java.time.Instant

data class RuntimeStatus(
    val registered: Boolean = false,
    val offline: Boolean = false,
    val domains: List<String> = emptyList(),

    val deltaSyncEnabled: Boolean = false,
    val deltaSyncIntervalMinutes: Int = 0,

    val heartbeatEnabled: Boolean = false,
    val lastHeartBeatAt: Instant? = null,

    val eventCheckEnabled: Boolean = false,
    val eventCheckIntervalMinutes: Int = 0,
)
