package no.fintlabs.dynamiskadapter.kafka

import no.fintlabs.dynamiskadapter.util.replaceDotWithDash
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters

class KafkaService

fun initiateKafkaTopic(
    org: String,
    domain: String,
    resource: String,
) {
    EntityTopicNameParameters
        .builder()
        .orgId(replaceDotWithDash(org))
        .domainContext(replaceDotWithDash(domain))
        .resource(replaceDotWithDash(resource))
        .build()
}
