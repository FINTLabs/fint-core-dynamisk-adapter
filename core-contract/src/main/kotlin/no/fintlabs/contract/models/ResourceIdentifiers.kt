package no.fintlabs.contract.models

data class ResourceIdentifiers(
    val domain: String,
    val component: String,
    val resource: String,
) {
    fun toKey(): String = "$domain/$component/$resource"
}

fun Set<ResourceIdentifiers>.getKeys() = map { it.toKey() }