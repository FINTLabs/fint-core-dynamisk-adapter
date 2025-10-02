package no.fintlabs.dynamiskadapter.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import no.fintlabs.dynamiskadapter.util.replaceDotWithDash
import no.fintlabs.kafka.entity.topic.EntityTopicNameParameters
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import java.util.Properties

class KafkaService {
    private val producer =
        KafkaProducer<String, String>(
            Properties().apply {
                put("bootstrap.servers", "localhost:9092")
                put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
            },
        )

    fun <T> publish(
        org: String,
        domain: String,
        resource: String,
        data: T,
    ) {
        val topic =
            EntityTopicNameParameters
                .builder()
                .orgId(replaceDotWithDash(org))
                .domainContext(replaceDotWithDash(domain))
                .resource(replaceDotWithDash(resource))
                .build()
                .toString()

        print("publishing data to topic $topic")

        val json = jacksonObjectMapper().writeValueAsString(data)
        producer.send(ProducerRecord(topic, resource, json))
    }

    fun <T> readAll(
        org: String,
        domain: String,
        resource: String,
        clazz: Class<T>,
        groupId: String = "default-group",
    ): List<T> {
        val topic =
            EntityTopicNameParameters
                .builder()
                .orgId(replaceDotWithDash(org))
                .domainContext(replaceDotWithDash(domain))
                .resource(replaceDotWithDash(resource))
                .build()
                .toString()

        val consumer =
            KafkaConsumer<String, String>(
                Properties().apply {
                    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092")
                    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                    put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                },
            )
        consumer.subscribe(listOf(topic))
        val records = consumer.poll(Duration.ofSeconds(1))
        consumer.close()
        return records.map { jacksonObjectMapper().readValue(it.value(), clazz) }
    }

    fun close() {
        producer.flush()
        producer.close()
    }
}
