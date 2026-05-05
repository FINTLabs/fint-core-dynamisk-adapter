package no.fintlabs.runtime.model

import java.time.Instant

enum class JobState {
    QUEUED,
    RUNNING,
    SUCCESS,
    FAILED
}

data class RuntimeJobStatus(
    val id: String,
    val type: String,
    val state: JobState,
    var message: String? = null,
    val requestedAt: Instant,
    val startedAt: Instant? = null,
    val finishedAt: Instant? = null,
)