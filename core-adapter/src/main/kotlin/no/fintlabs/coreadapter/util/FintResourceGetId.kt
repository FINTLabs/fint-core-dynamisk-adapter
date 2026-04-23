package no.fintlabs.coreadapter.util

import no.novari.fint.model.resource.FintResource
import no.novari.metamodel.model.Resource

// TODO: Double check the name AND multiplicity of the field

fun FintResource.getId(): String =
    listOf("systemId", "fodselsnummer", "feidenavn")
        .firstNotNullOfOrNull { key ->
            identifikators.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
                ?.value
                ?.identifikatorverdi
        }
        ?: "NO_IDENTIFIERS_FOUND"

fun Resource.getIdPrefix(): String =
    listOf("systemId", "fodselsnummer", "feidenavn")
        .firstNotNullOfOrNull { key ->
            idFields
                .firstOrNull { it.equals(key, ignoreCase = true) }
            key.lowercase()
        }
        ?: "NO_IDENTIFIERS_FOUND"

fun FintResource.getIdPrefix(): String =
    listOf("systemId", "fodselsnummer", "feidenavn")
        .firstNotNullOfOrNull { key ->
            identifikators.entries
                .firstOrNull { it.key.equals(key, ignoreCase = true) }
            key.lowercase()
        }
        ?: "NO_IDENTIFIERS_FOUND"