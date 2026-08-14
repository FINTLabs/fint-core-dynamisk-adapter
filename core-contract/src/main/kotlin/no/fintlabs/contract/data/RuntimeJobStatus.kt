package no.fintlabs.contract.data

import java.time.Instant

enum class JobState {
    QUEUED,
    RUNNING,
    SUCCESS,
    CANCELLED,
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
) {
    val durationMs: Long?
        get() = if (startedAt != null && finishedAt != null) {
            finishedAt.toEpochMilli() - startedAt.toEpochMilli()
        } else {
            null
        }
}