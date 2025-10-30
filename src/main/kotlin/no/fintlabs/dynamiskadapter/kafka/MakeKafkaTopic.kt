package no.fintlabs.dynamiskadapter.kafka

fun makeKafkaTopic(
    org: String,
    domain: String,
    resource: String,
): String = "$org.$domain.$resource"
