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
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.contract.data.AmountTier
import no.fintlabs.contract.data.AmountTierPolicy
import no.fintlabs.contract.data.DualInt
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
import no.fintlabs.contract.util.resourceToIdentifiers
import no.fintlabs.runtime.config.DeltaConfig
import no.fintlabs.runtime.config.toDeltaResourceConfigList
import no.fintlabs.runtime.model.CreateSpecificDataCommand
import no.fintlabs.runtime.model.EventFetchCommand
import no.fintlabs.runtime.model.EventHandlingCommand
import no.fintlabs.runtime.model.StartupSequence
import no.fintlabs.runtime.status.dto.RuntimeStatus
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
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

    private val offline = AtomicBoolean(false)
    private val registered = AtomicBoolean(false)
    private val lastFullSyncAt = AtomicReference<Instant?>(null)
    private val lastScheduledFullSyncAt = AtomicReference<LocalDate?>(null)
    private val scheduledFullSyncIntervalInDays = AtomicInteger(props.scheduledFullSyncIntervalInDays)
    private val lastHeartBeatAt = AtomicReference<Instant?>(null)
    private val lastDeltaSyncAt = AtomicReference<Instant?>(null)
    private val lastScheduledDeltaSyncAt = AtomicReference<Instant?>(null)
    private val deltaSyncLoopStartedAt = AtomicReference<Instant?>(null)

    private val heartBeatActive = AtomicBoolean(true)

    private val eventCheckIntervalMinutes = AtomicInteger(props.fintProperties.eventCheckIntervalInMinutes)
    private val eventCache: MutableMap<String, RequestFintEvent> = mutableMapOf()

    private val enableDeltaSync = AtomicBoolean(props.enableDeltaSync)
    private val deltaSyncIntervalInMinutes = AtomicInteger(props.deltaConfig.deltaSyncIntervalInMinutes)
    private val deltaSyncConfig = AtomicReference(props.deltaConfig)

    private val resetEveryNight = AtomicBoolean(props.resetEveryNight)
    private val activeDomains = AtomicReference(props.startupDomains)
    private val amountTierPolicy = AtomicReference(props.amountTierPolicy.toAmountTierPolicy())
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

    fun submit(command: RuntimeCommand, log: Boolean = true): String {
        if (log) {
            logger.info("Submitting command : ${command.javaClass.simpleName}, ${command.id}")
        }
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
            is FullSyncCommand -> handleFullSync(command.scheduled)
            is CreateDataCommand -> {
                val resources: MutableMap<ResourceIdentifiers, IntRange> = mutableMapOf()
                for (res in command.resources) {
                    resources[resourceToIdentifiers(res.key)] = IntRange(res.value, res.value)
                }
                handleGenerateResources(resources)
            }

            is CreateSpecificDataCommand -> {
                handleGenerateSpecifiedResources(command)
            }

            is DeltaSyncCommand -> {
                val resources: MutableMap<ResourceIdentifiers, IntRange> = mutableMapOf()
                val delta = deltaSyncConfig.get()
                for (resource in delta.resources) {
                    val metadata: ExpandedMetadata? = engine.getMetadataFromKey(resource.resource)
                    if (metadata != null) {
                        resources[metadata.toIdentifiers()] =
                            delta.amountTierPolicy.toAmountTierPolicy()
                                .getRange(metadata.amountTier ?: AmountTier.UNKNOWN)
                    }
                }
                handleGenerateResources(resources)
            }

            is EventHandlingCommand -> {
                executeEventRequest(command)
            }

            is EventFetchCommand -> {
                checkForEvents(command)
            }
        }
    }

    private suspend fun handleStartup(command: StartupSequence) {
        var capabilities = engine.getAllGeneratedCapabilities()
        if (capabilities.isEmpty()) {
            capabilities = engine.generateCapabilitiesForDomains(command.domains)
        }

        if (capabilities.isNotEmpty()) {
            updateJobMessage(command.id, "Registering adapter with ${capabilities.size} capabilities", false)
            logger.info("Registering adapter with ${capabilities.size} capabilities")
            attemptRegistration(capabilities)

            if (!registered.get()) {
                updateJobMessage(command.id, "Registration failed, waiting 5 minutes to retry...")
                delay(300_000L)
                updateJobMessage(command.id, "Retrying registration...")
                attemptRegistration(capabilities)
            }

            if (registered.get()) {
                updateJobMessage(command.id, "Registration successful")
                registeredCapabilities.addAll(capabilities)
                registeredCapabilitiesFor.set(capabilities.getKeys())
                generateAndDeployInitialDataset(command)

                startBackgroundLoops()

                updateJobMessage(command.id, "Startup sequence successful", log = false)
            } else throw IllegalStateException(
                "Failed to register to provider with capabilities:\n" +
                        capabilities.getKeys()
                            .groupBy { it.split("/")[1] }
                            .entries
                            .joinToString("\n") { (component, keys) ->
                                "$component:      ${keys.joinToString(", ") { it.substringAfterLast("/") }}"
                            }
            )
        } else throw IllegalStateException("No capabilities to register")
    }

    private suspend fun attemptRegistration(capabilities: MutableSet<AdapterCapability>) {
        val registration = adapter.register(capabilities)
        registered.set(registration.registered)
        heartBeatActive.set(registration.registered)
        offline.set(registration.offline)
    }

    private var deltaLoopJob: Job? = null
    private var heartbeatLoopJob: Job? = null
    private var eventCheckLoopJob: Job? = null

    private fun startBackgroundLoops() {
        if (deltaLoopJob?.isActive != true) {
            deltaLoopJob = scope.launch {
                deltaLoop()
            }
        }

        if (heartbeatLoopJob?.isActive != true) {
            heartbeatLoopJob = scope.launch {
                heartbeatLoop()
            }
        }

        if (eventCheckLoopJob?.isActive != true) {
            eventCheckLoopJob = scope.launch {
                eventCheckLoop()
            }
        }
    }

    private suspend fun handleFullSync(scheduled: Boolean) {
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        adapter.performSync(
            metadataList = metadata,
            dataList = allData,
            syncType = SyncType.FULL,
            maxPageSize = maxPageSize.get(),
        )
        if (scheduled) {
            lastScheduledFullSyncAt.set(LocalDate.now(ZoneId.of("Europe/Oslo")))
        }
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
            enableDeltaSync.set(false)
            throw IllegalStateException("Failed to generate resources. Max amount of resources limit reached.")
        }
    }

    private suspend fun handleGenerateSpecifiedResources(command: CreateSpecificDataCommand) {
        if (engine.verifyResourceLimitNotReached()) {

            val resources = engine
                .generateResourceWithSpecifiedFieldValue(
                    command.specifiedResources,
                    command.supportingResources,
                    command.amount
                )

            val resourceKeys =
                listOf(command.supportingResources + (command.specifiedResources.map { it.resource }))
            if (resources!!.values.isNotEmpty()) {
                val metadata = resources.keys.first()

                adapter.performSync(
                    metadataList = mutableListOf(metadata),
                    dataList = resources,
                    syncType = SyncType.DELTA,
                    maxPageSize = maxPageSize.get(),
                )
                lastDeltaSyncAt.set(Instant.now())

            } else throw IllegalStateException(
                "Failed to generate specified resources for request ${command.id}, with requested resources: $resourceKeys"
            )
        } else {
            enableDeltaSync.set(false)
            throw IllegalStateException("Failed to generate resources. Max amount of resources limit reached.")
        }
    }

    private suspend fun generateAndDeployInitialDataset(command: StartupSequence) {
        engine.executeInitialDataset(amountTierPolicy.get())
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        updateJobMessage(command.id, "Attempting to deploy initial dataset...", false)
        adapter.performSync(
            metadataList = metadata,
            dataList = allData,
            syncType = SyncType.FULL,
            maxPageSize = maxPageSize.get(),
        )
    }

    suspend fun hardReset() {
        runtimeMutex.withLock {
            logger.info("\n")
            logger.info("Performing hard runtime reset...")

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
    private fun nightlyScheduledTask() {
        logger.info("Performing nightly scheduled task...")
        logger.trace(
            "resetEveryNight = {}, fullSyncIntervalInDays = {}",
            resetEveryNight,
            scheduledFullSyncIntervalInDays
        )

        if (resetEveryNight.get()) {

            scope.launch {
                hardReset()
            }
            return
        }

        if (scheduledFullSyncIntervalInDays.get() != 0) {
            val lastScheduled = lastScheduledFullSyncAt.get()
            val intervalDays = scheduledFullSyncIntervalInDays.get()
            val today = LocalDate.now(ZoneId.of("Europe/Oslo"))

            val daysSinceLast =
                lastScheduled?.let { ChronoUnit.DAYS.between(today, it) }?.toInt()

            if (daysSinceLast == null || daysSinceLast >= intervalDays) {
                submit(FullSyncCommand(scheduled = true))
            } else {
                logger.trace(
                    "Scheduled full sync not performed. interval: {}, daysSinceLast: {}, today: {}",
                    intervalDays,
                    daysSinceLast,
                    today
                )
            }
        } else return
    }

    private suspend fun deltaLoop() {
        if (enableDeltaSync.get()) {
            deltaSyncLoopStartedAt.set(Instant.now())
            logger.info("Delta sync loop started with ${deltaSyncIntervalInMinutes.get()} minutes interval. " + nextScheduledDeltaSync())
            while (scope.isActive) {
                val interval = deltaSyncIntervalInMinutes.get()
                delay(interval.toLong() * 60_000L)
                if (engine.verifyResourceLimitNotReached()) {
                    submit(DeltaSyncCommand())
                }
            }
        } else {
            logger.info("AUTOMATIC DELTA_SYNC IS DISABLED")
        }
    }

    private suspend fun heartbeatLoop() {
        if (heartBeatActive.get()) {
            logger.debug("heartbeat loop started with ${props.fintProperties.heartbeatIntervalInMinutes} minutes interval.")
            while (scope.isActive) {
                delay(props.fintProperties.heartbeatIntervalInMinutes * 60_000L)
                lastHeartBeatAt.set(Instant.now())
                if (!offline.get()) {
                    adapter.giveHeartBeat()
                }
            }
        } else logger.warn("HEARTBEAT HAS BEEN DEACTIVATED")
    }

    // ------------------------
    //   Controller functions
    // ------------------------

    fun setDataset(domains: List<String>): String {
        var returnString: String

        if (domains == activeDomains.get()) {
            return "Updating dataset failed because dataset is already as specified."
        }
        val newDomains = domains.filter { !activeDomains.get().contains(it) }
        returnString = if (newDomains.isEmpty()) {
            "All specified domains already exist in instance."
        } else {
            val allCapabilities: MutableSet<AdapterCapability> =
                (engine.generateCapabilitiesForDomains(newDomains)
                        + registeredCapabilities) as MutableSet<AdapterCapability>
            val registered = adapter.register(allCapabilities)
            if (registered.registered) {
                logger.info("registration Successful with new domains: {}", domains)
                activeDomains.set(domains)
                "Dataset has been successfully updated. " +
                        "\n Dataset successfully registered to Provider." +
                        "\n If you want data from the new dataset, run a POST to /data/reset-data. "

            } else "Failed to register with $newDomains."
        }
        return returnString
    }

    fun resetDataset(): String {
        if (activeDomains.get() != props.startupDomains) {
            activeDomains.set(props.startupDomains)
            return "Dataset is already set as standard"
        } else return "Original dataset has been restored. run a POST to /data/reset-data to reset the generated data."
    }

    fun setResetDataNightly(bool: Boolean) {
        if (bool) {
            logger.info("NIGHTLY DATA RESET ENABLED")
        } else {
            logger.info("NIGHTLY DATA RESET DISABLED")
        }

        resetEveryNight.set(bool)
    }

    // --------------------------
    //   Configuration tweaking
    // --------------------------

    fun setAmountTierPolicy(newPolicy: AmountTierPolicy) {
        logger.info("AmountTierPolicy updated.")
        amountTierPolicy.set(newPolicy)
    }

    fun resetAmountTierPolicy() {
        logger.info("AmountTierPolicy has been reset to configurated.")
        amountTierPolicy.set(props.amountTierPolicy.toAmountTierPolicy())
    }

    fun setMaxGeneratedResources(int: Int) {
        logger.info("Max generated resources has been set to $int")
        engine.setMaxResources(int)
    }

    fun resetMaxGeneratedResources() {
        logger.info("Max generated resources has been reset to configurated.")
        engine.resetMaxResources()
    }

    fun setFullSyncInterval(interval: Int) {
        logger.info("Full sync interval in days has been set to $interval")
        scheduledFullSyncIntervalInDays.set(interval)
        engine.setFullSyncInterval(interval)
    }

    fun setSeed(newSeed: String?) {
        logger.info("Seed has been set to : '$newSeed'")
        engine.setSeed(newSeed)
    }

    fun resetSeed() {
        logger.info("Seed has been reset to configurated.")
        engine.setSeed(null)
    }

    // ---------------------
    //   Delta setup stuff
    // ---------------------

    fun setEnableDeltaSync() {
        enableDeltaSync.set(true)

        if (deltaLoopJob?.isActive != true)
            deltaLoopJob = scope.launch {
                deltaLoop()
            }
        logger.info("DELTA_SYNC HAS NOW BEEN ENABLED WITH ${deltaSyncIntervalInMinutes.get()} MINUTE INTERVAL.")
    }

    fun setDisableDeltaSync() {
        logger.info("AUTOMATIC DELTA_SYNC HAS NOW BEEN DISABLED.")
        enableDeltaSync.set(false)
        deltaLoopJob?.cancel()
    }

    fun setDeltaSyncInterval(intervalInMinutes: Int) {
        logger.info("DeltaSync Interval has now been set to $intervalInMinutes minutes.")
        deltaSyncIntervalInMinutes.set(intervalInMinutes)
    }

    fun resetDeltaSyncInterval() {
        logger.info("DeltaSync Interval has now been reset to configurated (${props.deltaConfig.deltaSyncIntervalInMinutes} minutes).")
        deltaSyncIntervalInMinutes.set(props.deltaConfig.deltaSyncIntervalInMinutes)
    }

    fun setDeltaSyncResources(
        resources: Map<String, DualInt?>,
        replace: Boolean = false
    ) {
        val deltaWithRange: MutableMap<String, IntRange> = mutableMapOf()
        for ((key, value) in resources) {
            if (value == null) {
                val defaultRange = engine.getMetadataFromKey(key)?.amountTier
                deltaWithRange[key] = deltaSyncConfig.get().amountTierPolicy.toAmountTierPolicy()
                    .getRange(defaultRange ?: AmountTier.UNKNOWN)
            } else {
                deltaWithRange[key] = value.toRange()
            }
        }

        val configResources = deltaWithRange.toDeltaResourceConfigList()

        if (!replace) {
            deltaSyncConfig.updateAndGet { current ->
                current.copy(
                    resources =
                        current.resources + configResources,
                )
            }
        } else {
            deltaSyncConfig.updateAndGet {
                it.copy(
                    resources = configResources,
                )
            }
        }
        logger.info("DeltaSync resources have been updated. ")
    }

    fun setDeltaAmountTierPolicy(newPolicy: AmountTierPolicy) {
        logger.info("DeltaSync AmountTierPolicy has been updated.")
        deltaSyncConfig.updateAndGet {
            it.copy(
                amountTierPolicy = newPolicy.toConfigAmountTier(),
            )
        }
    }

    // ----------
    //   Events
    // ----------

    private suspend fun eventCheckLoop() {
        if (eventCheckIntervalMinutes.get() >= 1) {
            logger.debug("Event check loop started with ${eventCheckIntervalMinutes.get()} minutes interval.")
            while (scope.isActive) {
                delay(eventCheckIntervalMinutes.toLong() * 60_000L)
                submit(EventFetchCommand(), false)
            }
        } else logger.warn("EVENT CHECKING IS DEACTIVATED")
    }

    fun checkForEvents(command: EventFetchCommand) {
        val events: List<RequestFintEvent> = adapter.getEvents()
        if (events.isEmpty()) {
            updateJobMessage(command.id, "Event check done, no events returned.", false)
        } else {
            for (event in events) {
                eventCache[event.corrId] = event
                submit(
                    EventHandlingCommand(
                        operationType = event.operationType,
                        eventCorrId = event.corrId,
                    )
                )
                logger.debug("EVENT: {} : {} added to queue.", event.operationType.name, event.corrId)
            }
        }
        updateJobMessage(command.id, "Event check done, ${events.size} events added to queue.", false)
    }

    fun setEventInterval(int: Int) {
        logger.info("Event check interval in minutes has been set to $int.")
        eventCheckIntervalMinutes.set(int)
    }

    fun resetEventInterval() {
        logger.info("Event check interval has been reset to configurated.")
        eventCheckIntervalMinutes.set(props.fintProperties.eventCheckIntervalInMinutes)
    }

    fun disableEventCheck() {
        logger.info("AUTOMATIC EVENT CHECK HAS BEEN DEACTIVATED.")
        eventCheckIntervalMinutes.set(0)
    }

    private fun executeEventRequest(command: EventHandlingCommand) {
        val request = eventCache[command.eventCorrId]
        if (request != null) {
            logger.info(
                "attempting to execute event request: {} : {}",
                command.operationType.name,
                command.eventCorrId
            )
            val execution = engine.executeEventRequest(request)
            adapter.postEvent(execution)
        } else {
            throw NullPointerException("executeEventRequest event[${command.eventCorrId}] not found in eventCache")
        }
    }

    // --------------------
    //   Job Status stuff
    // --------------------

    private fun updateJobMessage(id: String, message: String, log: Boolean = true) {
        currentJobs[id]?.message = message
        allJobs[id]?.message = message
        if (log) {
            logger.info("JOB UPDATE : $id: $message")
        }
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

    private fun markSuccess(command: RuntimeCommand) {
        updateStatus(command.id) {
            it.copy(
                state = JobState.SUCCESS,
                finishedAt = Instant.now(),
            )
        }
        val message: String = currentJobs[command.id]?.message ?: ""
        logger.info("JOB DONE: ${command.id}; $message")
        when (command) {
            is StartupSequence -> lastFullSyncAt.set(Instant.now())
            is FullSyncCommand -> lastFullSyncAt.set(Instant.now())
            is DeltaSyncCommand -> {
                lastDeltaSyncAt.set(Instant.now())
                lastScheduledDeltaSyncAt.set(Instant.now())
            }

            is CreateDataCommand -> lastDeltaSyncAt.set(Instant.now())
            is CreateSpecificDataCommand -> lastDeltaSyncAt.set(Instant.now())

            is EventHandlingCommand,
            is EventFetchCommand -> {
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

    // ----------------
    //   Status stuff
    // ----------------

    fun getRuntimeStatus(): RuntimeStatus =
        RuntimeStatus(
            registered = registered.get(),
            offline = offline.get(),
            resetEveryNight = resetEveryNight.get(),
            fullSyncIntervalInDays = scheduledFullSyncIntervalInDays.get(),
            domains = activeDomains.get(),
            deltaSyncEnabled = (enableDeltaSync.get()),
            deltaSyncIntervalMinutes = deltaSyncIntervalInMinutes.get(),
            heartbeatEnabled = heartBeatActive.get(),
            lastHeartBeatAt = lastHeartBeatAt.get(),
            eventCheckEnabled = (eventCheckIntervalMinutes.get() != 0),
            eventCheckIntervalMinutes = eventCheckIntervalMinutes.get(),
        )

    fun getRunningJob(): RuntimeJobStatus? =
        currentJobs.values.firstOrNull { it.state == JobState.RUNNING }

    fun getDeltaSetup(): DeltaConfig = deltaSyncConfig.get()
    
    fun getCurrentJobs(): List<RuntimeJobStatus> = currentJobs.values.sortedBy { it.requestedAt }

    fun getActiveDomains(): List<String> = activeDomains.get()

    fun getAllJobs(): List<RuntimeJobStatus> = allJobs.values.sortedByDescending { it.requestedAt }

    fun queueSize(): Int = currentJobs.values.count { it.state == JobState.QUEUED }

    fun getLastFullSync(): Instant? = lastFullSyncAt.get()
    fun getLastDeltaSync(): Instant? = lastDeltaSyncAt.get()

    fun nextScheduledDeltaSync(): String {
        if (!enableDeltaSync.get()) return "Scheduled DeltaSync is DISABLED"

        val lastRun = lastScheduledDeltaSyncAt.get()
            ?: deltaSyncLoopStartedAt.get() ?: return "no idea lol *shrug*"

        val nextRun = lastRun.plus(
            Duration.ofMinutes(
                deltaSyncIntervalInMinutes.toLong()
            )
        )
        val localTime = nextRun.atZone(ZoneId.systemDefault()).toLocalDateTime()

        return "Next Scheduled DeltaSync: " + localTime.truncatedTo(ChronoUnit.SECONDS).toString()
    }

}