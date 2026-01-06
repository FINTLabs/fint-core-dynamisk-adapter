package no.fintlabs.coreadapter.config

import no.fintlabs.adapter.models.AdapterCapability
import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fint.provider")
data class ProviderProperties(
    val baseUrl: String = "https://beta.felleskomponent.no/provider",
    val orgId: String = "fintlabs.no",
    val capabilities: List<AdapterCapability> = listOf(),
)
