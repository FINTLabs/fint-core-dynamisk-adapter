package no.fintlabs.coreadapter.store

import no.fintlabs.coreadapter.data.ResourceKeyPair
import org.springframework.stereotype.Component
import java.util.concurrent.ConcurrentHashMap

data class ResourceRef(
    val key: ResourceKey,
    val id: String,
)

@Component
class RelationStore {
    private val graph = ConcurrentHashMap<ResourceKeyPair, ConcurrentHashMap<String, MutableSet<ResourceRef>>>()

    private fun nodeKey(ref: ResourceRef) = "${ref.key}|${ref.id}"

    fun relate(
        r1: ResourceRef,
        r2: ResourceRef,
    ) {
        val pairKey = ResourceKeyPair.of(r1.key, r2.key)
        val g = graph.computeIfAbsent(pairKey) { ConcurrentHashMap() }

        g.computeIfAbsent(nodeKey(r1)) { mutableSetOf() }.add(r2)
        g.computeIfAbsent(nodeKey(r2)) { mutableSetOf() }.add(r1)
    }

    fun getResourceRelation(
        ref: ResourceRef,
        otherType: ResourceKey,
    ): Set<ResourceRef> {
        val pairKey = ResourceKeyPair.of(ref.key, otherType)
        val g = graph[pairKey] ?: return emptySet()
        return g[nodeKey(ref)]?.toSet() ?: emptySet()
    }
}
