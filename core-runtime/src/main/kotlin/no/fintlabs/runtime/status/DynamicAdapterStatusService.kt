package no.fintlabs.runtime.status

import no.fintlabs.engine.DynamicAdapterEngine
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.contract.dto.DynaGeneralStatusResponse
import no.fintlabs.contract.data.RuntimeJobStatus
import no.fintlabs.contract.dto.SystemStatus
import org.springframework.stereotype.Service
import java.lang.management.ManagementFactory

@Service
class DynamicAdapterStatusService(
    private val runtime: DynamicAdapterRuntimeService,
    private val engine: DynamicAdapterEngine,
) {
    fun status(): DynaGeneralStatusResponse =
        DynaGeneralStatusResponse(
            registered = runtime.isRegistered(),
            queueSize = runtime.queueSize(),
            runningJob = runtime.getRunningJob(),
            currentJobs = runtime.getCurrentJobs(),
            lastHeartBeatAt = runtime.getLastHeartbeat(),
            lastFullSyncAt = runtime.getLastFullSync(),
            lastDeltaSyncAt = runtime.getLastDeltaSync(),
            nextScheduledDeltaSyncAt = runtime.nextScheduledDeltaSync(),
            resourceStatus = engine.resourceStatus(),
            systemStatus = systemStatus(),
        )

    fun allJobs(): List<RuntimeJobStatus> =
        runtime.getAllJobs().sortedByDescending { it.requestedAt }

    fun currentJobs(): List<RuntimeJobStatus> =
        runtime.getCurrentJobs().sortedByDescending { it.requestedAt }

    private fun systemStatus(): SystemStatus {
        val rt = Runtime.getRuntime()

        return SystemStatus(
            uptimeMs = ManagementFactory.getRuntimeMXBean().uptime,
            usedMemoryBytes = rt.totalMemory() - rt.freeMemory(),
            maxMemoryBytes = rt.maxMemory(),
            threadCount = ManagementFactory.getThreadMXBean().threadCount,
            systemLoadAverage = ManagementFactory.getOperatingSystemMXBean().systemLoadAverage,
        )
    }

}