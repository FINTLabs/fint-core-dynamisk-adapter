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
import no.novari.fint.model.resource.FintResource
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component

@Component
class EventHandler(
    private val metadata: MetadataService,
    private val storage: ResourceStore,
) {
    private val logger = LoggerFactory.getLogger(EventHandler::class.java)
    private val objectMapper = ObjectMapper()

    fun handle(event: RequestFintEvent): ResponseFintEvent {
        var errorMessage: String = ""
        var conflictReason: String = ""
        var rejectedReason: String = ""
        var valueNotEmpty: Boolean = true
        var actual: SyncPageEntry = SyncPageEntry()

        if (event.value.isEmpty() || event.operationType != OperationType.VALIDATE) {
            errorMessage = "\"Event request \${event.corrId} has no value \\n One could say it's... worthless.\""
        } else {
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

            when (event.operationType) {
                OperationType.CREATE -> {
                    val existing = storage.getById(resourceKey, resourceId)
                    if (existing == null) {
                        storage.addAllResources(resourceClass, listOf<FintResource>(incomingResource))
                        actual = resourceToSyncPageEntry(incomingResource, resourceClass)
                    } else {
                        conflictReason = "A resource with this ID already exists, therefore could not be created."
                    }
                }

                OperationType.UPDATE -> {
                    val updated = storage.replaceResource(resourceKey, resourceId, incomingResource)

                    if (updated) {
                        actual = resourceToSyncPageEntry(incomingResource, resourceClass)
                    } else {
                        errorMessage = "Resource not found, therefore could not be updated."
                    }
                }

                OperationType.DELETE -> {
                    val deleted = storage.deleteResource(resourceKey, resourceId)
                    if (!deleted) {
                        errorMessage = "Resource ${resourceClass.idPrefix} not found, therefore could not be deleted"
                    }
                }

                OperationType.VALIDATE -> {
                    valueNotEmpty = false
                }
            }
        }

        return ResponseFintEvent
            .builder()
            .corrId(event.corrId)
            .operationType(event.operationType)
            .value(if (valueNotEmpty) actual else null)
            .conflicted(conflictReason.isNotBlank())
            .conflictReason(conflictReason)
            .failed(errorMessage.isNotBlank())
            .errorMessage(errorMessage)
            .rejected(rejectedReason.isNotBlank())
            .rejectReason(rejectedReason)
            .build()
    }

    private fun resourceToSyncPageEntry(resource: FintResource, meta: ExpandedMetadata): SyncPageEntry {
        val id =
            requireNotNull(resource.getId(meta.idPrefix, meta.idFieldType)) {
                "Missing identifier for ${resource.javaClass.simpleName}"
            }
        return SyncPageEntry.of(id, resource)
    }
}