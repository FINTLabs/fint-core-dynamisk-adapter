package no.fintlabs.coreadapter.store

import no.fint.model.resource.FintResource
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap
import kotlin.random.Random

typealias ResourceKey = String // Key should always be "component/resource"

@Component
class ResourceStore {
    private val data = ConcurrentHashMap<ResourceKey, MutableList<FintResource>>()

    private fun listFor(key: ResourceKey): MutableList<FintResource> = data.computeIfAbsent(key) { mutableListOf() }

    fun lookFor(name: String): List<FintResource>? = data[name]

    fun addAll(
        key: ResourceKey,
        resources: List<FintResource>,
    ) = listFor(key).addAll(resources)

    fun updateAll(
        key: ResourceKey,
        resources: List<FintResource>,
    ) {
        data[key] = resources.toMutableList()
    }

    fun addSingular(
        key: ResourceKey,
        resource: FintResource,
    ) = listFor(key).add(resource)

    fun getAll(key: ResourceKey): MutableList<FintResource> = data[key] ?: mutableListOf()

    fun getRandom(key: ResourceKey): FintResource? {
        val list = data[key] ?: return null
        return list[Random.nextInt(list.size)]
    }
}
