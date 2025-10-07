package no.fintlabs.dynamiskadapter.kafka

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.time.Duration
import java.util.Properties

object KafkaSingleton {
    private lateinit var producer: KafkaProducer<String, String>
    var bootstrapServers: String = "localhost:9092"
    val objectMapper = jacksonObjectMapper()

    fun init(bootstrapServers: String) {
        this.bootstrapServers = bootstrapServers
        producer =
            KafkaProducer<String, String>(
                Properties().apply {
                    put("bootstrap.servers", bootstrapServers)
                    put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                    put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
                },
            )
    }

    fun <T> publish(
        topic: String,
        data: List<T>,
    ) {
        for (resource in data) {
            val json = objectMapper.writeValueAsString(resource)
            producer.send(ProducerRecord(topic, "key", json))
            println("Published $resource")
        }

        println("Published to $topic")
    }

    inline fun <reified T> readAll(
        topic: String,
        groupId: String = "default-group",
    ): List<T>? {
        val consumer =
            KafkaConsumer<String, String>(
                Properties().apply {
                    put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers)
                    put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                    put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
                    put(ConsumerConfig.GROUP_ID_CONFIG, groupId)
                    put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
                },
            )

        consumer.subscribe(listOf(topic))
        val records = consumer.poll(Duration.ofMillis(200))
        consumer.close()

        if (records.isEmpty) return null

        return records.map { objectMapper.readValue(it.value(), T::class.java) }
    }
}
