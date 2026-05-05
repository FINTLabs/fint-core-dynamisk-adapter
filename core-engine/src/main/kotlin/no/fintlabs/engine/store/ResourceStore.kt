package no.fintlabs.engine.store

import no.fintlabs.contract.data.ExpandedMetadata
import no.novari.fint.model.resource.FintResource
import no.fintlabs.contract.StoredResource
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import no.fintlabs.contract.util.getId

typealias ResourceKey = String
// ResourceKey = domain/component/resource

@Component
class ResourceStore {
    private val data = ConcurrentHashMap<ResourceKey, ConcurrentHashMap<String, StoredResource>>()

    private fun mapFor(key: ResourceKey): ConcurrentHashMap<String, StoredResource> =
        data.computeIfAbsent(key) { ConcurrentHashMap() }

    fun addAllResources(
        meta: ExpandedMetadata,
        resources: List<FintResource>,
    ) {
        val key = meta.key
        val map = mapFor(key)
        resources.forEach { resource ->
            val id: String = resource.getId(meta.idPrefix, meta.idFieldType)
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

    fun countResources(key: ResourceKey): Int = data[key]?.keys?.size ?: 0

    fun getAll(key: ResourceKey): List<StoredResource> = data[key]?.values?.toList() ?: emptyList()

    fun getAllResources(key: ResourceKey): List<FintResource> = data[key]?.values?.toFintResources() ?: emptyList()


    private fun Collection<StoredResource>.toFintResources(): List<FintResource> {
        if (isEmpty()) return emptyList()
        return map { it.resource }
    }
}
