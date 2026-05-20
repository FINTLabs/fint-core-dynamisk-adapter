package no.fintlabs.runtime.status

import no.fintlabs.engine.DynamicAdapterEngine
import no.fintlabs.runtime.DynamicAdapterRuntimeService
import no.fintlabs.contract.data.DynaGeneralStatusResponse
import org.springframework.stereotype.Service

@Service
class DynamicAdapterStatusService(
    private val runtime: DynamicAdapterRuntimeService,
    private val engine: DynamicAdapterEngine,
) {
    fun status(): DynaGeneralStatusResponse

}