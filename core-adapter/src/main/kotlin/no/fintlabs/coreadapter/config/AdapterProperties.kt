package no.fintlabs.coreadapter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fint.adapter")
data class AdapterProperties(
    val id: String,
    val orgId: String = "fintlabs-no",
    val username: String,
    val password: String,
    val baseUrl: String,
    val registrationId: String,
    val heartbeatInterval: Int,
    val capabilities: List<Capability> = emptyList(),
)

data class Capability(
    val domainName: String,
    val packageName: String,
    val resource: String,
)