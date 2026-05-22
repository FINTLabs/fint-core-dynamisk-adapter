package no.fintlabs.contract.data

import java.time.Instant

data class DynaGeneralStatusResponse(
    val registered: Boolean,
    val queueSize: Int,
    val runningJob: RuntimeJobStatus?,
    val currentJobs: List<RuntimeJobStatus>,
    val lastHeartBeatAt: Instant?,
    val lastFullSyncAt: Instant?,
    val lastDeltaSyncAt: Instant?,
    val nextScheduledDeltaSyncAt: String,
//    val dynaSetup: DynaRuntimeConfig,
    val resourceStatus: ResourceStatus,
    val systemStatus: SystemStatus,
)

data class SystemStatus(
    val uptimeMs: Long,
    val usedMemoryBytes: Long,
    val maxMemoryBytes: Long,
    val threadCount: Int,
    val systemLoadAverage: Double?,
)