package no.fintlabs.coreadapter.util

import no.novari.fint.model.resource.FintResource

fun FintResource.getFirstId(): String =
    identifikators.firstNotNullOf { it.value }.identifikatorverdi
        ?: "MISSING_ID FOR ${this.javaClass.simpleName}"
