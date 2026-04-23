package no.fintlabs.coreadapter.util

import no.fintlabs.coreadapter.data.IdFieldType
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.metamodel.model.Resource
import java.lang.reflect.Field

data class IdMetadata(
    val prefix: String,
    val type: IdFieldType,
)

fun Resource.generateIdPrefix(): IdMetadata {
    val clazz = this.resourceClass
    val fields = getAllFields(clazz)

    val identifiers = fields.filter { it.type == Identifikator::class.java }
    val requiredIdentifiers = identifiers.filter { it.isRequired() }
    val allRequiredFields = fields.filter { it.isRequired() }

    val priorityList = listOf("systemId", "fodselsnummer", "feidenavn", "kode", "nummer", "navn")

    val chosenField =
        priorityList.firstNotNullOfOrNull { preferred ->
            requiredIdentifiers.firstOrNull { it.name.equals(preferred, true) }
        }
            ?: priorityList.firstNotNullOfOrNull { preferred ->
                allRequiredFields.firstOrNull { it.name.equals(preferred, true) }
            }
            ?: (requiredIdentifiers + allRequiredFields)
                .distinctBy { it.name }
                .firstOrNull()
            ?: error("No required fields in ${clazz.name}")

    val type =
        if (identifiers.any { it.name.equals(chosenField.name, true) }) {
            IdFieldType.IDENTIFIKATOR_MAP
        } else {
            IdFieldType.DIRECT_FIELD
        }

    return IdMetadata(
        prefix = chosenField.name.lowercase(),
        type = type
    )
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
