package no.fintlabs.coreadapter.publish

import no.fint.model.resource.FintResource
import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.adapter.models.sync.SyncPageEntry
import no.fintlabs.adapter.models.sync.SyncPageMetadata
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.util.getFirstId
import org.springframework.stereotype.Component
import java.util.*

@Component
class SyncPageFactory(
    private val props: AdapterProperties,
) {
    fun buildEntries(resources: List<FintResource>): MutableList<SyncPageEntry> =
        resources
            .map { resource ->
                val id =
                    requireNotNull(resource.getFirstId()) {
                        "Missing identifier for ${resource.javaClass.simpleName}"
                    }
                SyncPageEntry.of(id, resource)
            }.toMutableList()

    fun buildMetadata(
        resourceName: String,
        page: Long,
        pageSize: Long,
        totalPages: Long,
        totalSize: Long,
        corrId: String = newCorrId(),
        time: Long = System.currentTimeMillis(),
    ): SyncPageMetadata =
        SyncPageMetadata
            .builder()
            .adapterId(props.adapterId)
            .orgId(props.orgId)
            .corrId(corrId)
            .uriRef("/$resourceName")
            .page(page)
            .pageSize(pageSize)
            .totalPages(totalPages)
            .totalSize(totalSize)
            .time(time)
            .build()

    fun buildPage(
        syncType: SyncType,
        metadata: SyncPageMetadata,
        entries: MutableList<SyncPageEntry>,
    ): SyncPage =
        SyncPage
            .builder()
            .syncType(syncType)
            .metadata(metadata)
            .resources(entries)
            .build()

    fun newCorrId(): String = UUID.randomUUID().toString().lowercase()
}
