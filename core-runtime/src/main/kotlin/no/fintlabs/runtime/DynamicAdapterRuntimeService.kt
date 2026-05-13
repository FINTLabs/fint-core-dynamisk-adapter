package no.fintlabs.runtime

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.fintlabs.adapter.DynamicAdapterPublisher
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.engine.DynamicAdapterEngine
import no.fintlabs.runtime.config.DynaRuntimeConfig
import no.fintlabs.runtime.model.CreateDataCommand
import no.fintlabs.runtime.model.DeltaSyncCommand
import no.fintlabs.runtime.model.FullSyncCommand
import no.fintlabs.runtime.model.JobState
import no.fintlabs.runtime.model.RuntimeCommand
import no.fintlabs.runtime.model.RuntimeJobStatus
import no.fintlabs.runtime.model.StartupSequence
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class DynamicAdapterRuntimeService(
    val engine: DynamicAdapterEngine,
    val adapter: DynamicAdapterPublisher,
    val props: DynaRuntimeConfig,
) {
    val logger: Logger = LoggerFactory.getLogger(DynamicAdapterRuntimeService::class.java)

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val queue = Channel<RuntimeCommand>(capacity = Channel.UNLIMITED)
    private val currentJobs = ConcurrentHashMap<String, RuntimeJobStatus>()
    private val allJobs = ConcurrentHashMap<String, RuntimeJobStatus>()

    private val registeredCapabilities = mutableSetOf<AdapterCapability>()

    init {
        scope.launch {
            workerLoop()
        }
    }

    @PostConstruct
    fun startupSequence() {
        scope.launch {
            submit(StartupSequence(domains = props.startupDomains))
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
        // TODO: Expand the return of command submission
        return command.id
    }

    suspend fun workerLoop() {
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
        markRunning(command)
        when
                (command) {
            is StartupSequence -> handleStartup(command)
            is CreateDataCommand -> handleCreateData(command)
            is FullSyncCommand -> handleFullSync(command)
            is DeltaSyncCommand -> handleDeltaSync(command)
        }
    }

    private suspend fun handleStartup(command: StartupSequence) {
        val capabilities = engine.generateCapabilitiesForDomains(command.domains)
        if (capabilities.isNotEmpty()) {
            updateJobMessage(command.id, "Registering adapter with ${capabilities.size} capabilities")
            val registered = adapter.register(capabilities)
            if (registered) {
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

    private suspend fun generateAndDeployInitialDataset() {
        engine.executeInitialDataset(props.amountTierPolicy)
        val metadata = engine.getAllMetadata()
        val allData = engine.getAllGeneratedResources()
        adapter.performSync(metadata, allData, SyncType.FULL, props.fintProperties.maxPageSize)
    }

    @Scheduled(cron = "0 0 2 * * *", zone = "Europe/Oslo")
    private fun resetData() {
        if (props.resetEveryNight) {
            logger.info("Resetting all data...")
            engine.purgeAllStoredResources()

            submit(StartupSequence(domains = props.startupDomains))
        } else {
            return
        }
    }

    private suspend fun fullSyncLoop(days: Int) {
        while (scope.isActive) {
            delay(days * 24 * 60 * 60_000L)
            submit(FullSyncCommand())
        }
    }

    private suspend fun deltaLoop() {
        if (!props.enableDeltaSync) return
        else {
            // TODO: If resources exceed maxResources, stop
            // TODO: If props.deltaSetup.resources is empty, stop
            while (scope.isActive) {
                delay(props.deltaConfig.deltaSyncIntervalInMinutes * 60_000L)
                submit(DeltaSyncCommand())
            }
        }
    }

    private suspend fun heartbeatLoop(minutes: Int) {
        while (scope.isActive) {
            delay(minutes * 60_000L)
            adapter.giveHeartBeat()
        }
    }

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
}