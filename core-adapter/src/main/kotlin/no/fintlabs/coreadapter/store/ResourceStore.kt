package no.fintlabs.coreadapter.store

import no.fint.model.resource.FintResource
import no.fintlabs.coreadapter.data.StoredResource
import no.fintlabs.coreadapter.util.getFirstId
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

typealias ResourceKey = String

@Component
class ResourceStore {
    private val data = ConcurrentHashMap<ResourceKey, ConcurrentHashMap<String, StoredResource>>()

    private fun mapFor(key: ResourceKey): ConcurrentHashMap<String, StoredResource> = data.computeIfAbsent(key) { ConcurrentHashMap() }

    fun addAllResources(
        key: ResourceKey,
        resources: List<FintResource>,
    ) {
        val map = mapFor(key)
        resources.forEach { resource ->
            val id = resource.getFirstId()
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

    fun getResourceById(
        key: ResourceKey,
        id: String,
    ) = mapFor(key)[id]

    fun getRandomId(key: ResourceKey): String? {
        val ids = data[key]?.keys ?: return null
        if (ids.isEmpty()) return null
        return ids.elementAt(Random.nextInt(ids.size)) ?: return null
    }

    fun getAll(key: ResourceKey): List<StoredResource> = data[key]?.values?.toList() ?: emptyList()

    fun getAllResources(key: ResourceKey): List<FintResource?> = data[key]?.values?.toFintResources() ?: emptyList()

    fun List<FintResource>.toStoredResources(): List<StoredResource> {
        val toStore = mutableListOf<StoredResource>()
        for (resource in this) {
            toStore.add(
                StoredResource(
                    id = resource.getFirstId(),
                    resource = resource,
                ),
            )
        }
        return toStore
    }

    private fun MutableCollection<StoredResource>.toFintResources(): List<FintResource?> {
        if (this.isEmpty()) return emptyList()

        val result = mutableListOf<FintResource>()
        for (resource in this) {
            result.add(resource.resource)
        }
        return result
    }
}
