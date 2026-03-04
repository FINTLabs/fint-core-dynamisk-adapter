package no.fintlabs.coreadapter.runner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.publish.DynamicAdapterPublisher
import no.fintlabs.coreadapter.relations.RelationFactory
import org.springframework.boot.ApplicationArguments
import org.springframework.boot.ApplicationRunner
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class DynamicAdapterRunner(
    private val engine: DynamicAdapterEngine,
    private val props: DynamicAdapterProperties,
    private val publisher: DynamicAdapterPublisher,
    private val fintProps: AdapterProperties,
    private val relationFactory: RelationFactory,
) : ApplicationRunner {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val syncMutex = Mutex()

    override fun run(args: ApplicationArguments) {
        if (props.initialDataSets.isEmpty()) {
            println("No initial dataset found. Shutting down...")
        }
        val isRegistered = registerAndBootstrap()
        if (!isRegistered) {
            println("failed to register adapter. Shutting down...")
        }
        performInitialDatasetRoutine()

        scope.launch { heartBeatLoop() }

        if (props.fullSyncIntervalInDays > 0) {
            scope.launch { fullSyncLoop(props.fullSyncIntervalInDays) }
        }

        if (props.enableDeltaSync && props.deltaSyncIntervalInMinutes != null) {
            scope.launch { deltaSyncLoop(props.deltaSyncIntervalInMinutes) }
        }
    }

    private fun registerAndBootstrap(): Boolean {
        val capabilities: MutableSet<AdapterCapability> = engine.generateCapabilities()
        val isRegistered: Boolean = publisher.register(capabilities)
        return isRegistered
    }

    private fun performInitialDatasetRoutine() {
        engine.executeInitialDataset()
        relationFactory.relateInitialDataset(engine.metadataList)
        engine.printAllDataIfEnabled()

        publisher.performFullSync(engine.metadataList)
    }

    private suspend fun heartBeatLoop() {
        val interval = Duration.ofMinutes(fintProps.heartbeatIntervalInMinutes.toLong())
        while (scope.isActive) {
            try {
                publisher.giveHeartBeat()
            } catch (e: Exception) {
                println("💔 Heartbeat Error: ${e.message}")
            }
            delay(interval.toMillis())
        }
    }

    private suspend fun fullSyncLoop(int: Int) {
        val interval = Duration.ofDays(int.toLong())
        while (scope.isActive) {
            delay(interval.toMillis())
            syncMutex.withLock {
                try {
                    publisher.performFullSync(engine.metadataList)
                } catch (e: Exception) {
                    println("⚠️ FULLSYNC Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun deltaSyncLoop(int: Int) {
        val interval = Duration.ofMinutes(int.toLong())
        while (scope.isActive) {
            delay(interval.toMillis())
            syncMutex.withLock {
                try {
                } catch (e: Exception) {
                    println("⚠️ DELTASYNC Error: ${e.message}")
                }
            }
        }
    }
}
