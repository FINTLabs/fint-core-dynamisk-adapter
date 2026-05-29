package no.fintlabs.contract.util

import no.fintlabs.adapter.models.AdapterCapability

fun AdapterCapability.getKey(): String = "${this.domainName}/${this.component}/${this.resourceName}".lowercase()

fun MutableSet<AdapterCapability>.getKeys(): List<String> = this.map { it.getKey() }