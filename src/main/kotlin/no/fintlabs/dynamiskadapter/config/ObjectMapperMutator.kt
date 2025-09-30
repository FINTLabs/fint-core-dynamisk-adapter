package no.fintlabs.dynamiskadapter.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.util.ISO8601DateFormat
import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration

@Configuration
open class ObjectMapperMutator(
    private val objectMapper: ObjectMapper,
) {
    @PostConstruct
    fun customize() {
        objectMapper.dateFormat = ISO8601DateFormat()
    }
}
