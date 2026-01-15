package no.fintlabs.coreadapter.config

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@ConfigurationProperties(prefix = "fint.adapter")
data class AdapterProperties(
    private val id: String,
    val orgId: String = "fintlabs-no",
    val clientId: String,
    val clientSecret: String,
    val scope: String,
    val username: String,
    val password: String,
    val baseUrl: String,
    val idpUri: String,
    val heartbeatInterval: Int,
    val capabilities: List<Capability> = emptyList(),
) {
    @OptIn(ExperimentalUuidApi::class)
    val adapterId: String = "$id/$${Uuid.random()}"
}

data class Capability(
    val domainName: String,
    val packageName: String,
    val resource: String,
)
