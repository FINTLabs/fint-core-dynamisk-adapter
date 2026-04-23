package no.fintlabs.coreadapter.store

import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.novari.fint.model.resource.FintResource
import no.fintlabs.coreadapter.data.StoredResource
import no.fintlabs.coreadapter.util.getId
import org.springframework.stereotype.Component
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.forEach

@Component
class TempDeltaSyncStore {
    private val data = ConcurrentHashMap<ResourceKey, ConcurrentHashMap<String, StoredResource>>()

    private fun mapFor(key: ResourceKey): ConcurrentHashMap<String, StoredResource> =
        data.computeIfAbsent(key) { ConcurrentHashMap() }

    fun purge(log: Boolean?) {
        if (log == true) {
            println("Latest DeltaSyncDataset has been PURGED from temporary storage")
            println(Instant.now())
        }
        data.clear()
    }

    fun addAllResources(
        key: ResourceKey,
        meta: ExpandedMetadata,
        resources: List<FintResource>,
    ) {
        val map = mapFor(key)
        resources.forEach { resource ->
            val id = resource.getId(meta.idPrefix, meta.idFieldType)
            map[id] = StoredResource(id, resource)
        }
    }

    fun updateResource(
        key: ResourceKey,
        id: String,
        updater: (FintResource) -> Unit,
    ) {
        val map = mapFor(key)
        val stored = map[id] ?: return

        updater(stored.resource)
        map[id] = stored
    }

    fun getIdsFor(key: ResourceKey): List<String> = data[key]?.keys?.toList() ?: emptyList()

    fun getAll(key: ResourceKey): List<StoredResource> = data[key]?.values?.toList() ?: emptyList()

    fun getAllResources(key: ResourceKey): List<FintResource> = data[key]?.values?.toFintResources() ?: emptyList()

    private fun Collection<StoredResource>.toFintResources(): List<FintResource> {
        if (isEmpty()) return emptyList()
        return map { it.resource }
    }
}
