package no.fintlabs.coreadapter.util

import no.fint.model.resource.FintResource

fun FintResource.getFirstId(): String? = identifikators.firstNotNullOf { it.value }.identifikatorverdi
