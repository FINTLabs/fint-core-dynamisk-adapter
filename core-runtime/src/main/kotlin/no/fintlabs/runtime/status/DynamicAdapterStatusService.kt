package no.fintlabs.runtime.status

import no.fintlabs.engine.DynamicAdapterEngine
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.contract.data.DynaGeneralStatusResponse
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class DynamicAdapterStatusService(
    private val runtime: DynamicAdapterRuntimeService,
    private val engine: DynamicAdapterEngine,
) {
    fun status(): DynaGeneralStatusResponse =
        DynaGeneralStatusResponse(
            registered = runtime.isResistered(),
            queueSize = runtime.queueSize(),
            runningJob = runtime.getCurrentJobs(),
            lastHeartBeatAt = runtime.lastHeartBeatAt as Instant?,
            lastFullSyncAt = runtime.lastFullSyncAt,
            lastDeltaSyncAt = runtime.lastFullSyncAt
        )

}