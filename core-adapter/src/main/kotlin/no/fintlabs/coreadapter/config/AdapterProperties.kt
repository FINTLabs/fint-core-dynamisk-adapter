package no.fintlabs.coreadapter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fint.adapter")
data class AdapterProperties(
    private val id: String,
    val orgId: String = "fintlabs.no",
    val clientId: String,
    val clientSecret: String,
    val scope: String,
    val username: String,
    val password: String,
    val idpUri: String,
    val heartbeatIntervalInMinutes: Int = 3,
    val capabilities: List<Capability> = emptyList(),
    val baseUrl: String = "https://beta.felleskomponent.no",
)

data class Capability(
    val domainName: String,
    val packageName: String,
    val resource: String,
)
