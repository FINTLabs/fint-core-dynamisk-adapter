package no.fintlabs.library

data class ResourceFactoryConfig(
    val seed: String = "",
    val minimumRandomPoolSize: Int = 20,
    val firstnameList: List<String> = emptyList(),
    val lastnameList: List<String> = emptyList(),
    val funnyNameList: List<String> = emptyList(),
    val quoteList: List<String> = emptyList(),
    val cityNameList: List<String> = emptyList(),
    val streetNameList: List<String> = emptyList(),
)
