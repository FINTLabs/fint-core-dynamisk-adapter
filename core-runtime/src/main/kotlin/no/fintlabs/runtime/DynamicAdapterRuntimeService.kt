package no.fintlabs.runtime

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import no.fintlabs.adapter.DynamicAdapterPublisher
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.engine.DynamicAdapterEngine
import no.fintlabs.runtime.config.DynaRuntimeConfig
import no.fintlabs.runtime.model.CreateDataCommand
import no.fintlabs.runtime.model.DeltaSyncCommand
import no.fintlabs.runtime.model.FullSyncCommand
import no.fintlabs.contract.data.JobState
import no.fintlabs.runtime.model.RuntimeCommand
import no.fintlabs.contract.data.RuntimeJobStatus
import no.fintlabs.runtime.config.DeltaConfig
import no.fintlabs.runtime.model.StartupSequence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

@Component
class DynamicAdapterRuntimeService(
    val engine: DynamicAdapterEngine,
    val adapter: DynamicAdapterPublisher,
    val props: DynaRuntimeConfig,
) {
    val logger: Logger = LoggerFactory.getLogger(DynamicAdapterRuntimeService::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val runtimeMutex = Mutex()

    private var queue = Channel<RuntimeCommand>(capacity = Channel.UNLIMITED)
    private val currentJobs = ConcurrentHashMap<String, RuntimeJobStatus>()
    private val allJobs = ConcurrentHashMap<String, RuntimeJobStatus>()
    private var activeWorkerJob: Job? = null

    private val registered = AtomicBoolean(false)
    private val lastFullSyncAt = AtomicReference<Instant?>(null)
    private val lastHeartBeatAt = AtomicReference<Instant?>(null)
    private val lastDeltaSyncAt = AtomicReference<Instant?>(null)
    private val lastScheduledDeltaSyncAt = AtomicReference<Instant?>(null)

    private val heartBeatActive = AtomicBoolean(true)

    private val enableDeltaSync = AtomicBoolean(props.enableDeltaSync)
    private val deltaSyncConfig = AtomicReference<DeltaConfig>(props.deltaConfig)

    private val resetEveryNight = AtomicBoolean(props.resetEveryNight)
    private val startupDomains = AtomicReference<List<String>>(props.startupDomains)
    private val amountTierPolicy = AtomicReference<AmountTierPolicy>(props.amountTierPolicy)

    private val registeredCapabilities = mutableSetOf<AdapterCapability>()

    init {
        scope.launch {
            activeWorkerJob = scope.launch {
                workerLoop()
            }
        }
    }

    @PostConstruct
    fun startupSequence() {
        scope.launch {
            submit(StartupSequence(domains = startupDomains.get()))
        }
    }

    fun submit(command: RuntimeCommand): String {
        logger.debug("Submitting ${command.javaClass.simpleName}, ${command.id}")
        markQueued(command)

        val result = queue.trySend(command)
        if (result.isFailure) {
            markFailed(command, IllegalStateException("Failed to submit ${command.id}"))
            logger.error("Failed to submit ${command.id}")
        }
        return "command ${command.id} submitted. \n There are ${queueSize() - 1} queued jobs before yours."
    }

    private suspend fun workerLoop() {
        for (command in queue) {
            markRunning(command)

            try {
                handle(command)
                markSuccess(command)
            } catch (e: Exception) {
                markFailed(command, e)
            }
        }
    }

    private suspend fun handle(command: RuntimeCommand) {
        when (command) {
            is StartupSequence -> handleStartup(command)
            is FullSyncCommand -> handleFullSync()
            is CreateDataCommand -> {
                val resources: MutableMap<ResourceIdentifiers, IntRange> = mutableMapOf()
                for (res in command.resources) {
                    resources[res.key] = IntRange(res.value, res.value)
                }
                handleGenerateResources(resources)
            }

            is DeltaSyncCommand -> {
                val resources: MutableMap<ResourceIdentifiers, IntRange> = mutableMapOf()
                val delta = deltaSyncConfig.get()
                for (res in delta.resources) {
                    val metadata: ExpandedMetadata? = engine.getMetadataFromIdentifier(res.key)
                    if (metadata != null) {
                        resources[metadata.toIdentifiers()] =
                            delta.amountTierPolicy.getRange(metadata.amountTier ?: AmountTier.UNKNOWN)
                    }
                }
                handleGenerateResources(resources)
            }
        }
    }

    private suspend fun handleStartup(command: StartupSequence) {
        val capabilities = engine.generateCapabilitiesForDomains(command.domains)
        if (capabilities.isNotEmpty()) {
            updateJobMessage(command.id, "Registering adapter with ${capabilities.size} capabilities")
            val registration = adapter.register(capabilities)
            registered.set(registration)
            heartBeatActive.set(registration)
            if (registered.get()) {
                updateJobMessage(command.id, "Registeration successful")
                registeredCapabilities.addAll(capabilities)
                generateAndDeployInitialDataset()

                deltaLoop()
                heartbeatLoop(props.fintProperties.heartbeatIntervalInMinutes)
            } else throw IllegalStateException(
                """Failed to register to provider with capabilities: 
                $capabilities
                """.trimMargin()
            )
        } else throw IllegalStateException("No capabilities to register")
    }

    private suspend fun handleFullSync() {
        logger.debug("Performing full sync...")
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        adapter.performSync(metadata, allData, SyncType.FULL, props.fintProperties.maxPageSize)
    }

    private suspend fun handleGenerateResources(requested: Map<ResourceIdentifiers, IntRange>) {
        if (engine.verifyResourceLimitNotReached()) {
            val metadataList: MutableList<ExpandedMetadata> = mutableListOf()

            val resources = engine.generateDeltaSyncData(requested)

            for (res in requested) {
                val metadata = engine.getMetadataFromIdentifier(res.key)
                if (metadata != null) {
                    metadataList.add(metadata)
                }
            }
            adapter.performSync(metadataList, resources, syncType = SyncType.DELTA, props.fintProperties.maxPageSize)
            lastDeltaSyncAt.set(Instant.now())
        } else {
            logger.warn("Failed to generate resources. Max amount of resources limit reached.")
            enableDeltaSync.set(false)
        }
    }

    private suspend fun generateAndDeployInitialDataset() {
        engine.executeInitialDataset(amountTierPolicy.get())
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        adapter.performSync(metadata, allData, SyncType.FULL, props.fintProperties.maxPageSize)
    }

    suspend fun hardReset() {
        runtimeMutex.withLock {
            logger.warn("Performing hard runtime reset...")

            activeWorkerJob?.cancelAndJoin()

            currentJobs.values.forEach {
                updateStatus(it.id) { status ->
                    status.copy(
                        state = JobState.CANCELLED,
                        finishedAt = Instant.now(),
                        message = "Cancelled by nightly reset"
                    )
                }
            }
            currentJobs.clear()
            queue.close()
            queue = Channel(capacity = Channel.UNLIMITED)

            engine.purgeAllStoredResources()

            activeWorkerJob =
                scope.launch {
                    workerLoop()
                }

            submit(StartupSequence(domains = startupDomains.get()))
        }
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Oslo")
    private fun scheduledDataReset() {
        if (!resetEveryNight.get()) return

        scope.launch {
            hardReset()
        }
    }

    var deltaSyncLoopStartedAt = AtomicReference<Instant?>(null)
    private suspend fun deltaLoop() {
        if (!enableDeltaSync.get()) return
        else {
            deltaSyncLoopStartedAt.set(Instant.now())
            while (scope.isActive) {
                // TODO: If resources exceed maxResources, stop
                // TODO: If props.deltaSetup.resources is empty, stop
                delay(deltaSyncConfig.get().deltaSyncIntervalInMinutes * 60_000L)
                submit(DeltaSyncCommand())
            }
        }
    }

    private suspend fun heartbeatLoop(minutes: Int) {
        while (scope.isActive) {
            delay(minutes * 60_000L)
            if (heartBeatActive.get()) {
                lastHeartBeatAt.set(Instant.now())
                adapter.giveHeartBeat()
            } else logger.warn("HEARTBEAT HAS BEEN DEACTIVATED")
        }
    }

    // Delta setup stuff

    private fun deltaConfig(): DeltaConfig =
        deltaSyncConfig.get()

    fun setEnableDeltaSync() = enableDeltaSync.set(true)

    fun setDisableDeltaSync() = enableDeltaSync.set(false)

    fun addDeltaSyncResources(
        resources: Map<ResourceIdentifiers, IntRange?>
    ) {
        deltaSyncConfig.updateAndGet { current ->
            current.copy(
                resources =
                    current.resources + resources
            )
        }
    }

    fun setDeltaSyncResources(
        resources: Map<ResourceIdentifiers, IntRange?>
    ) {
        deltaSyncConfig.updateAndGet {
            it.copy(
                resources = resources,
            )
        }
    }

    fun setDeltaAmountTierPolicy(newPolicy: AmountTierPolicy) {
        deltaSyncConfig.updateAndGet {
            it.copy(
                amountTierPolicy = newPolicy,
            )
        }
    }

    // Configuration tweaking

    fun setAmountTierPolicy(newPolicy: AmountTierPolicy) = amountTierPolicy.set(newPolicy)

    fun resetAmountTierPolicy() = amountTierPolicy.set(props.amountTierPolicy)

    fun setMaxGeneratedResources(int: Int) = engine.setMaxResources(int)

    fun resetMaxGeneratedResources() = engine.resetMaxResources()

    // Job Status stuff

    private fun updateJobMessage(id: String, message: String) {
        currentJobs[id]?.message = message
        allJobs[id]?.message = message
        logger.info("JOB UPDATE ${Instant.now()} --- $id: $message")
    }

    private fun updateStatus(
        id: String,
        update: (RuntimeJobStatus) -> RuntimeJobStatus,
    ) {
        val old = allJobs[id] ?: return
        val new = update(old)
        allJobs[id] = new
        if (new.state == JobState.SUCCESS) {
            currentJobs.remove(id)
        } else {
            currentJobs[id] = new
        }
    }

    private fun markQueued(command: RuntimeCommand) {
        val status = RuntimeJobStatus(
            id = command.id,
            type = command::class.simpleName ?: "UNKNOWN",
            state = JobState.QUEUED,
            requestedAt = command.requestedAt,
        )

        currentJobs[command.id] = status
        allJobs[command.id] = status
    }

    private fun markRunning(command: RuntimeCommand) {
        updateStatus(command.id) {
            it.copy(
                state = JobState.RUNNING,
                startedAt = Instant.now(),
            )
        }
    }

    private fun markSuccess(command: RuntimeCommand, message: String? = null) {
        updateStatus(command.id) {
            it.copy(
                state = JobState.SUCCESS,
                message = message,
                finishedAt = Instant.now(),
            )
        }
        logger.debug("JOB DONE: ${command.id}, $message")
        when (command) {
            is StartupSequence -> lastFullSyncAt.set(Instant.now())
            is FullSyncCommand -> lastFullSyncAt.set(Instant.now())
            is CreateDataCommand -> lastDeltaSyncAt.set(Instant.now())
            is DeltaSyncCommand -> {
                lastDeltaSyncAt.set(Instant.now())
                lastScheduledDeltaSyncAt.set(Instant.now())
            }
        }
        currentJobs.remove(command.id)
    }

    private fun markFailed(command: RuntimeCommand, error: Throwable) {
        updateStatus(command.id) {
            it.copy(
                state = JobState.FAILED,
                message = error.message,
                finishedAt = Instant.now(),
            )
        }
        logger.error("JOB FAILED: ${command.id}, $error")
    }

    // Status stuff

    fun isRegistered() = registered.get()

    fun getRunningJob(): RuntimeJobStatus? =
        currentJobs.values.firstOrNull { it.state == JobState.RUNNING }

    fun getCurrentJobs(): List<RuntimeJobStatus> = currentJobs.values.sortedBy { it.requestedAt }

    fun getAllJobs(): List<RuntimeJobStatus> = allJobs.values.sortedByDescending { it.requestedAt }

    fun queueSize(): Int = currentJobs.values.count { it.state == JobState.QUEUED }

    fun getLastHeartbeat(): Instant? = lastHeartBeatAt.get()
    fun getLastFullSync(): Instant? = lastFullSyncAt.get()
    fun getLastDeltaSync(): Instant? = lastDeltaSyncAt.get()

    fun nextScheduledDeltaSync(): String {
        if (!props.enableDeltaSync) return "Scheduled DeltaSync is DISABLED"

        val lastRun = lastScheduledDeltaSyncAt.get()
            ?: deltaSyncLoopStartedAt.get() ?: return "no idea lol *shrug*"

        val nextRun = lastRun.plus(
            Duration.ofMinutes(
                props.deltaConfig.deltaSyncIntervalInMinutes.toLong()
            )
        )
        val localTime = nextRun.atZone(ZoneId.systemDefault()).toLocalDateTime()

        return "Next Scheduled DeltaSync will take place at: " + localTime.truncatedTo(ChronoUnit.SECONDS).toString()
    }

}