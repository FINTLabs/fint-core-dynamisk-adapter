package no.fintlabs.coreadapter.runner

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.toExpandedMetadata
import no.fintlabs.coreadapter.publish.DynamicAdapterPublisher
import no.fintlabs.coreadapter.relations.RelationFactory
import no.fintlabs.coreadapter.relations.SetType
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
            println("Failed to register adapter. Shutting down...")
            return
        }
        performInitialDatasetRoutine()

        if (props.localLogicTest && !props.enableDeltaSync) {
            println("All jobs are completed. Shutting down...")
            return
        }

        if (fintProps.baseUrl.startsWith("https://api")) {
            println(" : : MOCK DATA HAS NOTHING TO DO IN PROD! : :")
            println("change baseUrl to beta and try again.")
            return
        }

        if (!props.localLogicTest) {
            scope.launch { heartBeatLoop() }
        }

        if (props.fullSyncIntervalInDays > 0) {
            scope.launch { fullSyncLoop(props.fullSyncIntervalInDays) }
        }

        if (props.enableDeltaSync && props.deltaSyncIntervalInMinutes != null) {
            scope.launch { deltaSyncLoop(props.deltaSyncIntervalInMinutes) }
        }

        runBlocking { scope.coroutineContext[Job]!!.join() }
    }

    private fun registerAndBootstrap(): Boolean {
        val capabilities: MutableSet<AdapterCapability> = engine.generateCapabilities()
        val isRegistered: Boolean = publisher.register(capabilities)
        return isRegistered
    }

    private fun performInitialDatasetRoutine() {
        engine.executeInitialDataset()
        engine.generateDeltaSyncMetadata()
        relationFactory.relateDataset(engine.metadataList, SetType.INITIAL)
        engine.printAllDataIfEnabled()

        publisher.performSync(engine.metadataList, SyncType.FULL)
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
                    publisher.performSync(engine.metadataList, SyncType.FULL)
                } catch (e: Exception) {
                    println("⚠️ FULLSYNC Error: ${e.message}")
                }
            }
        }
    }

    private suspend fun deltaSyncLoop(int: Int) {
        val interval = Duration.ofMinutes(int.toLong())
        var count = 0
        println("DeltaSync will happen in ${props.deltaSyncIntervalInMinutes} minutes")
        println("")
        while (scope.isActive) {
            delay(interval.toMillis())
            syncMutex.withLock {
                try {
                    engine.executeDeltaSyncDataset()
                    val metadata = engine.deltaMetadataList.toExpandedMetadata()
                    relationFactory.relateDataset(metadata, SetType.DELTA)
                    engine.printAllDeltaDataIfEnabled()
                    publisher.performSync(metadata, SyncType.DELTA)
                    engine.printAllDataIfEnabled()
                    engine.deltaDoneLogAmountOfResources()
                    count++
                    println("DeltaSync #$count completed. Next deltaSync in ${props.deltaSyncIntervalInMinutes} minutes")
                } catch (e: Exception) {
                    println("⚠️ DELTASYNC Error: ${e.message}")
                }
            }
        }
    }
}
