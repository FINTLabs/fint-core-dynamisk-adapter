package no.fintlabs.coreadapter.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "fint.provider")
data class ProviderProperties(
    val baseUrl: String = "https://beta.felleskomponent.no/provider",
)
