package no.fintlabs.coreadapter.data

import no.fintlabs.coreadapter.store.ResourceKey

data class ResourceKeyPair(
    val a: ResourceKey,
    val b: ResourceKey,
) {
    companion object {
        fun of(
            k1: ResourceKey,
            k2: ResourceKey,
        ): ResourceKeyPair = if (k1 <= k2) ResourceKeyPair(k1, k2) else ResourceKeyPair(k2, k1)
    }
}
