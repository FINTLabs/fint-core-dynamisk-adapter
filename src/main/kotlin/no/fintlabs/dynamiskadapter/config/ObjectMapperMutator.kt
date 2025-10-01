package no.fintlabs.dynamiskadapter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
open class ObjectMapperMutator(
    private val objectMapper: ObjectMapper,
) {
    @PostConstruct
    fun customize() {
        // Register JavaTimeModule to handle java.time types
        objectMapper.registerModule(JavaTimeModule())

        // Disable writing dates as timestamps, use ISO-8601 instead
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
    }
}
