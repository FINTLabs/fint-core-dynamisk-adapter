package no.fintlabs.contract.data

import no.fintlabs.runtime.config.DynaRuntimeConfig
import no.fintlabs.runtime.model.RuntimeJobStatus
import java.time.Instant

data class DynaGeneralStatusResponse(
    val registered: Boolean,
    val queueSize: Int,
    val runningJob: RuntimeJobStatus?,
    val currentJobs: List<RuntimeJobStatus>,
    val lastHeartBeatAt: Instant?,
    val lastFullSyncAt: Instant?,
    val lastDeltaSyncAt: Instant?,
    val nextScheduledDeltaSyncAt: Instant?,
    val dynaSetup: DynaRuntimeConfig,
    val resourceStatus: ResourceStatus,
    val systemStatus: SystemStatus,
)

data class SystemStatus(
    val uptimeMs: Long,
    val usedMemoryBytes: Long,
    val maxMemoryBytes: Long,
    val threadCount: Int,
    val processCpuLoad: Double?,
)