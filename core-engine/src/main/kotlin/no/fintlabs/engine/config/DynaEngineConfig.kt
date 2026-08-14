package no.fintlabs.engine.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "dyna.engine")
data class DynaEngineConfig(
    val seed: String = "",
    val minimumRandomPoolSize: Int = 20,
    val maxGeneratedResources: Int = 10000,
    val firstnameList: List<String> = emptyList(),
    val lastnameList: List<String> = emptyList(),
    val funnyNameList: List<String> = emptyList(),
    val quoteList: List<String> = emptyList(),
    val cityNameList: List<String> = emptyList(),
    val streetNameList: List<String> = emptyList(),
)
