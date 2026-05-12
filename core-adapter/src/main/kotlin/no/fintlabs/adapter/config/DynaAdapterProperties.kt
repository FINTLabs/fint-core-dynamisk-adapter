package no.fintlabs.adapter.config

import no.fintlabs.contract.models.ResourceIdentifiers
import org.springframework.boot.context.properties.ConfigurationProperties
import java.util.UUID

@ConfigurationProperties(prefix = "dyna.adapter")
data class DynaAdapterProperties(
    private val id: String,
    val orgId: String = "fintlabs.no",
    val clientId: String,
    val clientSecret: String,
    val scope: String,
    val username: String,
    val password: String,
    val idpUri: String,
    val heartbeatIntervalInMinutes: Int = 3,
    val capabilities: List<ResourceIdentifiers> = emptyList(),
    val baseUrl: String = "https://beta.felleskomponent.no",
) {
    val adapterId = "$id/${UUID.randomUUID()}"
}
