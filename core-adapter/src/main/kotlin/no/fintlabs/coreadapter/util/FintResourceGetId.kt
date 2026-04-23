package no.fintlabs.coreadapter.util

import no.fintlabs.coreadapter.data.IdFieldType
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.resource.FintResource

fun FintResource.getId(
    idPrefix: String,
    idFieldType: IdFieldType,
): String =
    when (idFieldType) {
        IdFieldType.IDENTIFIKATOR_MAP ->
            identifikators.entries
                .firstOrNull { it.key.equals(idPrefix, ignoreCase = true) }
                ?.value
                ?.identifikatorverdi
                ?: error("No identifier found in identifikators for ${this.javaClass.simpleName} with prefix=$idPrefix")

        IdFieldType.DIRECT_FIELD -> {
            val field =
                generateSequence(this.javaClass as Class<*>?) { it.superclass }.firstNotNullOfOrNull { clazz ->
                    try {
                        clazz.getDeclaredField(idPrefix).apply { isAccessible = true }
                    } catch (_: NoSuchFieldException) {
                        null
                    }
                }
                    ?: error("No direct field '$idPrefix' found in ${this.javaClass.simpleName}")

            when (val value = field.get(this)) {
                is Identifikator ->
                    value.identifikatorverdi
                        ?: error("Identifikator field '$idPrefix' was null in ${this.javaClass.simpleName}")

                is String ->
                    value

                else ->
                    error("Unsupported direct ID field type '${value?.javaClass?.name}' in ${this.javaClass.simpleName}.$idPrefix")
            }
        }
    }