package no.fintlabs.dynamiskadapter.kafka

import org.testcontainers.kafka.KafkaContainer
import org.testcontainers.utility.DockerImageName

object KafkaBootstrap {
    val kafka =
        KafkaContainer(
            DockerImageName
                .parse("apache/kafka:4.1.0"),
        )

    fun start(): String {
        kafka.start()
        println("Kafka started on ${kafka.bootstrapServers}")
        return kafka.bootstrapServers
    }

    fun stop() {
        kafka.stop()
    }
}
