package no.fintlabs.runtime.model

import java.time.Instant
import java.util.UUID

sealed interface RuntimeCommand {
    val id: String
    val requestedAt: Instant
}

data class StartupSequence(
    override val id: String = "startup_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    val domains: List<String>,
) : RuntimeCommand

data class CreateDataCommand(
    override val id: String = "create" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    val component: String,
    val resource: String,
    val count: Int,
) : RuntimeCommand

data class FullSyncCommand(
    override val id: String = "fullSync_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
) : RuntimeCommand

data class DeltaSyncCommand(
    override val id: String = "deltaSync_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
) : RuntimeCommand