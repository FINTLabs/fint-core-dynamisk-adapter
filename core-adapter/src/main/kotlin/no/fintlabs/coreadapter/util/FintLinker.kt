package no.fintlabs.coreadapter.util

import no.fint.model.resource.FintResource
import no.fint.model.resource.Link

fun FintResource.putLink(
    rel: String,
    targetId: String,
) {
    val linksField =
        generateSequence(this.javaClass as Class<*>?) { it.superclass }
            .mapNotNull { c -> c.declaredFields.firstOrNull { it.name == "links" }?.apply { isAccessible = true } }
            .firstOrNull() ?: error("No 'links' field found on ${this.javaClass.name}")

    @Suppress("UNCHECKED_CAST")
    val map =
        (linksField.get(this) as? MutableMap<String, MutableList<Link>>)
            ?: HashMap<String, MutableList<Link>>().also { linksField.set(this, it) }

    map.getOrPut(rel) { mutableListOf() }.add(Link.with(targetId))
}
