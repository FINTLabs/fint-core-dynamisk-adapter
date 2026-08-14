package no.fintlabs.contract.util

import no.novari.fint.model.FintRelation

fun FintRelation.toResourceKey(): String =
    packageName
        .substringAfter("model.")
        .replace(".", "/")
        .lowercase()
