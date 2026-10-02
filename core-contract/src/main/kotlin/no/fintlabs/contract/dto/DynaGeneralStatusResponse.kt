package no.fintlabs.contract.dto

import no.fintlabs.contract.data.ResourceStatus
import no.fintlabs.contract.data.RuntimeJobStatus
import java.time.Instant

data class DynaGeneralStatusResponse(
    val registered: Boolean,
    val offline: Boolean,

    val queueSize: Int,
    val runningJob: RuntimeJobStatus?,
    val currentJobs: List<RuntimeJobStatus>,

    val lastFullSyncAt: Instant?,

    val heartBeatEnabled: Boolean,
    val lastHeartBeatAt: Instant?,

    val eventCheckEnabled: Boolean,
    val eventCheckIntervalInMinutes: Int,

    val deltaSetup: DeltaSetupStatus,
    val resourceStatus: ResourceStatus,
    val systemStatus: SystemStatus,
)

data class DeltaSetupStatus(
    val enabled: Boolean,
    val interval: String,
    val lastPerformed: Instant?,
    val nextScheduled: String,
)

data class SystemStatus(
    val uptimeMs: Long,
    val usedMemoryBytes: Long,
    val maxMemoryBytes: Long,
    val threadCount: Int,
    val systemLoadAverage: Double?,
)