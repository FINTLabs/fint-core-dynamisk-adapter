package no.fintlabs.coreadapter.store

import no.novari.fint.model.resource.FintResource
import no.fintlabs.coreadapter.data.StoredResource
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random
import no.fintlabs.coreadapter.util.getId

typealias ResourceKey = String
// ResourceKey = domain/component/resource

@Component
class ResourceStore {
    private val data = ConcurrentHashMap<ResourceKey, ConcurrentHashMap<String, StoredResource>>()

    private fun mapFor(key: ResourceKey): ConcurrentHashMap<String, StoredResource> =
        data.computeIfAbsent(key) { ConcurrentHashMap() }

    fun addAllResources(
        key: ResourceKey,
        resources: List<FintResource>,
    ) {
        val map = mapFor(key)
        resources.forEach { resource ->
            val id: String = resource.getId()
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

    fun getResourceById(
        key: ResourceKey,
        id: String,
    ) = mapFor(key)[id]

    fun getRandomId(key: ResourceKey): String? {
        val ids = data[key]?.keys ?: return null
        if (ids.isEmpty()) return null
        return ids.elementAt(Random.nextInt(ids.size)) ?: return null
    }

    fun countResources(key: ResourceKey): Int = data[key]?.keys?.size ?: 0

    fun getAll(key: ResourceKey): List<StoredResource> = data[key]?.values?.toList() ?: emptyList()

    fun getAllResources(key: ResourceKey): List<FintResource> = data[key]?.values?.toFintResources() ?: emptyList()

    fun List<FintResource>.toStoredResources(): List<StoredResource> {
        val toStore = mutableListOf<StoredResource>()
        for (resource in this) {
            toStore.add(
                StoredResource(
                    id = resource.getId(),
                    resource = resource,
                ),
            )
        }
        return toStore
    }

    private fun Collection<StoredResource>.toFintResources(): List<FintResource> {
        if (isEmpty()) return emptyList()
        return map { it.resource }
    }
}
