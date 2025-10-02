package no.fintlabs.dynamiskadapter.util

fun makeKafkaTopic(
    org: String,
    domain: String,
    resource: String,
): String = "$org.$domain.$resource"
