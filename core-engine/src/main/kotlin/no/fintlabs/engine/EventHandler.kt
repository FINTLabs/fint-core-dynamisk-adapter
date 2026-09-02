package no.fintlabs.engine

import com.fasterxml.jackson.databind.ObjectMapper
import no.fintlabs.adapter.models.event.RequestFintEvent
import no.fintlabs.adapter.models.event.ResponseFintEvent
import no.fintlabs.adapter.models.sync.SyncPageEntry
import no.fintlabs.adapter.operation.OperationType
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.ResourceIdentifiers
import no.fintlabs.contract.util.getId
import no.fintlabs.engine.store.ResourceStore
import no.fintlabs.engine.store.TempDeltaSyncStore
import no.novari.fint.model.resource.FintResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventHandler(
    private val metadata: MetadataService,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val relations: RelationFactory,
) {
    private val logger = LoggerFactory.getLogger(EventHandler::class.java)
    private val objectMapper = ObjectMapper()

    fun handle(event: RequestFintEvent): ResponseFintEvent {
        val resourceKey = "${event.domainName}/${event.packageName}/${event.resourceName}"
        val resourceClass = metadata.getMetadataFor(
            ResourceIdentifiers(
                event.domainName,
                event.packageName,
                event.resourceName
            )
        )

        val incomingResource: FintResource? = objectMapper.readValue(
            event.value,
            resourceClass!!.resource.resourceClass
        )
        val resourceId: String = incomingResource!!.identifikators.firstNotNullOf { resourceClass.idPrefix }

        // Initializing values
        var actual: SyncPageEntry = SyncPageEntry()

        var conflictReason: String = ""
        var errorMessage: String = ""
        var rejectedReason: String = ""

        when (event.operationType) {
            OperationType.CREATE -> {
                storage.addAllResources(resourceClass, listOf<FintResource>(incomingResource!!))
                actual = resourceToSyncPageEntry(incomingResource, resourceClass)
            }

            OperationType.UPDATE -> {
                val res = storage.replaceResource(resourceKey, resourceId, incomingResource)

                if (res) {
                    // IF SUCCESSFUL, CREATE SYNC-PAGE-ENTRY OF THE BROUGHT RESOURCE
                    actual = resourceToSyncPageEntry(incomingResource, resourceClass)
                } else {
                    // TODO: Possibility of Errors and fails needs to be initialized before when loop, and passed on to the ResponseEvent.
                }
            }

            OperationType.DELETE -> {
                val deleted = storage.deleteResource(resourceKey, resourceId)
                if (!deleted) {
                    errorMessage = "Resource ${resourceClass.idPrefix} not found, therefore could not be deleted"
                }
            }

            OperationType.VALIDATE -> {

            }
        }

        return ResponseFintEvent
            .builder()
            .corrId(event.corrId)
            .operationType(event.operationType)
            .value(actual)
            .conflicted(conflictReason.isNotBlank())
            .conflictReason(conflictReason)
            .failed(errorMessage.isNotBlank())
            .errorMessage(errorMessage)
            .rejected(rejectedReason.isNotBlank())
            .rejectReason(rejectedReason)
            .build()
    }

    // TODO: Validate Events. Maybe something like the provider-gateway ResponseEventService.validateEvent()?
    fun validateEventRequest(event: RequestFintEvent) {
        if (event.value.isEmpty() || event.operationType != OperationType.VALIDATE) {
            throw IllegalStateException("Event request ${event.corrId} has no value")
        }
    }

    // TODO
    // This function should do whatever "operationType.validate" is supposed to do
    fun validateResource(external: FintResource): Boolean {
        return true
    }

    private fun resourceToSyncPageEntry(resource: FintResource, meta: ExpandedMetadata): SyncPageEntry {
        val id =
            requireNotNull(resource.getId(meta.idPrefix, meta.idFieldType)) {
                "Missing identifier for ${resource.javaClass.simpleName}"
            }
        return SyncPageEntry.of(id, resource)
    }
}