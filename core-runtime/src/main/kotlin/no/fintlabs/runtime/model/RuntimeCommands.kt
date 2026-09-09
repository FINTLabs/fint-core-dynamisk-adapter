package no.fintlabs.runtime.model

import no.fintlabs.adapter.operation.OperationType
import no.fintlabs.contract.models.ResourceIdentifiers
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
    override val id: String = "create_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    val resources: Map<ResourceIdentifiers, Int>,
) : RuntimeCommand

data class CreateSpecificDataCommand(
    override val id: String = "create_specific_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    val resource: ResourceIdentifiers,
    val fieldName: String,
    val fieldValue: String,
    val amount: Int = 1,
) : RuntimeCommand

data class FullSyncCommand(
    override val id: String = "fullSync_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
) : RuntimeCommand

data class DeltaSyncCommand(
    override val id: String = "deltaSync_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
) : RuntimeCommand

data class EventHandlingCommand(
    override val id: String = "eventHandling_${operationType.name}" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
    val operationType: OperationType,
    val eventCorrId: String,
) : RuntimeCommand

data class EventFetchCommand(
    override val id: String = "event_fetch_" + UUID.randomUUID().toString(),
    override val requestedAt: Instant = Instant.now(),
) : RuntimeCommand
