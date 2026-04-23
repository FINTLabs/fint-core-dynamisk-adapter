package no.fintlabs.coreadapter.util

import no.novari.fint.model.resource.FintResource
import no.novari.metamodel.model.Resource

// TODO: Double check the name AND multiplicity of the field
// TODO: getId needs to use prefix

fun FintResource.getId(prefix: String): String =
    listOf(prefix)
        .firstNotNullOfOrNull { key ->
            identifikators.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value
                ?.identifikatorverdi
        }
        ?: "NO_IDENTIFIERS_FOUND_WITH_PREFIX"