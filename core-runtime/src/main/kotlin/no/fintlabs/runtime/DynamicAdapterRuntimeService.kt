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
import no.fintlabs.contract.util.getKeys
import no.fintlabs.runtime.config.DeltaConfig
import no.fintlabs.runtime.config.toDeltaResourceConfigList
import no.fintlabs.runtime.model.CreateSpecificDataCommand
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
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import kotlin.collections.iterator


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
    private val deltaSyncIntervalInMinutes = AtomicInteger(props.deltaConfig.deltaSyncIntervalInMinutes)
    private val deltaSyncConfig = AtomicReference<DeltaConfig>(props.deltaConfig)

    private val resetEveryNight = AtomicBoolean(props.resetEveryNight)
    private val activeDomains = AtomicReference<List<String>>(props.startupDomains)
    private val amountTierPolicy = AtomicReference<AmountTierPolicy>(props.amountTierPolicy.toAmountTierPolicy())
    private val maxPageSize = AtomicInteger(props.fintProperties.maxPageSize)
    private val registeredCapabilities = mutableSetOf<AdapterCapability>()
    private val registeredCapabilitiesFor = AtomicReference<List<String>>(listOf())


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
            submit(StartupSequence(domains = activeDomains.get()))
        }
    }

    fun submit(command: RuntimeCommand): String {
        logger.info("Submitting ${command.javaClass.simpleName}, ${command.id}")
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
        logger.info("Handling ${command.javaClass.simpleName}, ${command.id}")
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

            is CreateSpecificDataCommand -> {
                handleGenerateSpecifiedResources(command)
            }

            is DeltaSyncCommand -> {
                val resources: MutableMap<ResourceIdentifiers, IntRange> = mutableMapOf()
                val delta = deltaSyncConfig.get()
                for (res in delta.resources) {
                    val metadata: ExpandedMetadata? = engine.getMetadataFromIdentifier(res.toIdentifiers())
                    if (metadata != null) {
                        resources[metadata.toIdentifiers()] =
                            delta.amountTierPolicy.toAmountTierPolicy()
                                .getRange(metadata.amountTier ?: AmountTier.UNKNOWN)
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
                registeredCapabilitiesFor.set(capabilities.getKeys())
                generateAndDeployInitialDataset()

                startBackgroundLoops()

                updateJobMessage(command.id, "Startup sequence successful")
            } else throw IllegalStateException(
                """Failed to register to provider with capabilities: 
                $capabilities
                """.trimMargin()
            )
        } else throw IllegalStateException("No capabilities to register")
    }

    private var deltaLoopJob: Job? = null
    private var heartbeatLoopJob: Job? = null

    private fun startBackgroundLoops() {
        if (deltaLoopJob?.isActive != true) {
            deltaLoopJob = scope.launch {
                deltaLoop()
            }
        }

        if (heartbeatLoopJob?.isActive != true) {
            heartbeatLoopJob = scope.launch {
                heartbeatLoop(props.fintProperties.heartbeatIntervalInMinutes)
            }
        }
    }

    private suspend fun handleFullSync() {
        logger.debug("Performing full sync...")
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        adapter.performSync(
            metadataList = metadata,
            dataList = allData,
            syncType = SyncType.FULL,
            maxPageSize = maxPageSize.get(),
        )
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
            adapter.performSync(
                metadataList = metadataList,
                dataList = resources,
                syncType = SyncType.DELTA,
                maxPageSize = maxPageSize.get(),
            )
            lastDeltaSyncAt.set(Instant.now())
        } else {
            logger.error("Failed to generate resources. Max amount of resources limit reached.")
            enableDeltaSync.set(false)
        }
    }

    private suspend fun handleGenerateSpecifiedResources(command: CreateSpecificDataCommand) {
        if (engine.verifyResourceLimitNotReached()) {

            val resources = engine
                .generateResourceWithSpecifiedFieldValue(
                    command.resource,
                    command.fieldName,
                    command.fieldValue,
                    command.amount
                )

            if (resources!!.values.isNotEmpty()) {
                val metadata = resources.keys.first()

                adapter.performSync(
                    metadataList = mutableListOf(metadata),
                    dataList = resources,
                    syncType = SyncType.DELTA,
                    maxPageSize = maxPageSize.get(),
                )
                lastDeltaSyncAt.set(Instant.now())
            } else logger.error("Failed to generate specified resources for ${command.resource}")

        } else {
            logger.error("Failed to generate resources. Max amount of resources limit reached.")
            enableDeltaSync.set(false)
        }
    }

    private suspend fun generateAndDeployInitialDataset() {
        engine.executeInitialDataset(amountTierPolicy.get())
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        logger.info("Attempting to deploy initial dataset...")
        adapter.performSync(
            metadataList = metadata,
            dataList = allData,
            syncType = SyncType.FULL,
            maxPageSize = maxPageSize.get(),
        )
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
                        message = "Cancelled by hard reset"
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

            submit(StartupSequence(domains = activeDomains.get()))
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
        if (!enableDeltaSync.get()) {
            logger.info("Delta sync is disabled.")
        } else {
            deltaSyncLoopStartedAt.set(Instant.now())
            logger.info("Delta sync loop started.")
            while (scope.isActive) {
                val interval = deltaSyncIntervalInMinutes.get()
                delay(interval.toLong() * 60_000L)
                if (engine.verifyResourceLimitNotReached()) {
                    submit(DeltaSyncCommand())
                }
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

    // Controller functions

    fun updateDataset(domains: List<String>): String {
        var returnString = ""

        if (domains == activeDomains.get()) {
            return "Updating dataset failed because dataset is already as specified."
        }
        val newDomains = domains.filter { !activeDomains.get().contains(it) }
        if (newDomains.isEmpty()) {
            returnString = "All specified domains already exist in instance."
        } else {
            val allCapabilities: MutableSet<AdapterCapability> =
                (engine.generateCapabilitiesForDomains(newDomains)
                        + registeredCapabilities) as MutableSet<AdapterCapability>
            val registered = adapter.register(allCapabilities)
            if (registered) {
                returnString = "Dataset has been successfully updated. " +
                        "\n Dataset successfully registered to Provider." +
                        "\n If you want data from the new dataset, run a POST to /data/reset-data. "

            } else returnString = "Failed to register with $newDomains."
        }
        return returnString
    }

    fun resetDataset(): String {
        if (activeDomains.get() != props.startupDomains) {
            activeDomains.set(props.startupDomains)
            return "Dataset is already set as standard"
        } else return "Original dataset has been restored. run a POST to /data/reset-data to reset the generated data."
    }

    // Delta setup stuff

    fun setEnableDeltaSync() {
        enableDeltaSync.set(true)

        if (deltaLoopJob?.isActive != true)
            deltaLoopJob = scope.launch {
                deltaLoop()
            }
    }

    fun setDisableDeltaSync() = enableDeltaSync.set(false)

    fun addDeltaSyncResources(
        resources: Map<ResourceIdentifiers, IntRange?>
    ) {
        val configResources = resources.toDeltaResourceConfigList()

        deltaSyncConfig.updateAndGet { current ->
            current.copy(
                resources =
                    current.resources + configResources,
            )
        }
    }

    fun setDeltaSyncInterval(intervalInMinutes: Int) = deltaSyncIntervalInMinutes.set(intervalInMinutes)

    fun resetDeltaSyncInterval() = deltaSyncIntervalInMinutes.set(props.deltaConfig.deltaSyncIntervalInMinutes)

    fun setDeltaSyncResources(
        resources: Map<ResourceIdentifiers, IntRange?>
    ) {
        deltaSyncConfig.updateAndGet {
            it.copy(
                resources = resources.toDeltaResourceConfigList(),
            )
        }
    }

    fun setDeltaAmountTierPolicy(newPolicy: AmountTierPolicy) {
        deltaSyncConfig.updateAndGet {
            it.copy(
                amountTierPolicy = newPolicy.toConfigAmountTier(),
            )
        }
    }

    // Configuration tweaking

    fun setAmountTierPolicy(newPolicy: AmountTierPolicy) = amountTierPolicy.set(newPolicy)

    fun resetAmountTierPolicy() = amountTierPolicy.set(props.amountTierPolicy.toAmountTierPolicy())

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
        logger.info("JOB DONE: ${command.id}, $message")
        when (command) {
            is StartupSequence -> lastFullSyncAt.set(Instant.now())
            is FullSyncCommand -> lastFullSyncAt.set(Instant.now())
            is DeltaSyncCommand -> {
                lastDeltaSyncAt.set(Instant.now())
                lastScheduledDeltaSyncAt.set(Instant.now())
            }

            is CreateDataCommand -> lastDeltaSyncAt.set(Instant.now())
            is CreateSpecificDataCommand -> lastDeltaSyncAt.set(Instant.now())
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

    fun getActiveDomains(): List<String> = activeDomains.get()

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
                deltaSyncIntervalInMinutes.toLong()
            )
        )
        val localTime = nextRun.atZone(ZoneId.systemDefault()).toLocalDateTime()

        return "Next Scheduled DeltaSync will take place at: " + localTime.truncatedTo(ChronoUnit.SECONDS).toString()
    }

}