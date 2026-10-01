package no.fintlabs.contract.util

import no.fintlabs.contract.models.ResourceIdentifiers

fun resourceToIdentifiers(key: String): ResourceIdentifiers {
    val parts = key.split("/")

    require(parts.size == 3) {
        "invalid resource key: $key"
    }

    return ResourceIdentifiers(
        domain = parts[0],
        component = parts[1],
        resource = parts[2]
    )
}