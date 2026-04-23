package no.fintlabs.coreadapter.util

import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.metamodel.model.Resource
import java.lang.reflect.Field

fun Resource.generateIdPrefix(): String {
    val clazz = this.resourceClass
    val fields = getAllFields(clazz)
    val identifiers = fields.filter { it.type == Identifikator::class.java }
    val requiredIdentifiers = identifiers.filter { it.isRequired() }
    val allRequiredFields = fields.filter { it.isRequired() }

    val priorityList = listOf("systemId", "fodselsnummer", "feidenavn", "kode", "nummer", "navn")

    return priorityList.firstOrNull { preferred ->
        // Checks for REQUIRED IDENTIFIERS with preferred name
        requiredIdentifiers.any { it.name.contains(preferred, ignoreCase = true) }
    }?.lowercase()
        ?: priorityList.firstOrNull { preferred ->
            // Checks all REQUIRED Fields with preferred name
            allRequiredFields.any { it.name.contains(preferred, ignoreCase = true) }
        }?.lowercase() ?: fields.firstOrNull { fields ->
            fields.name.endsWith(
                "id",
                ignoreCase = true
            )
        }?.name?.lowercase()
        ?: (requiredIdentifiers + allRequiredFields)
            // Checks all required fields
            .firstOrNull()
            ?.name
            ?.lowercase()
        ?: "NO_REQUIRED_FIELDS_IN_${clazz.name}"
}

private fun getAllFields(clazz: Class<*>): List<Field> =
    generateSequence(clazz) { it.superclass }
        .takeWhile { it != Any::class.java }
        .flatMap { it.declaredFields.asSequence() }
        .onEach { it.isAccessible = true }
        .toList()

private fun Field.isRequired(): Boolean =
    this.isAnnotationPresent(javax.validation.constraints.NotNull::class.java) ||
            this.isAnnotationPresent(javax.validation.constraints.NotBlank::class.java)
