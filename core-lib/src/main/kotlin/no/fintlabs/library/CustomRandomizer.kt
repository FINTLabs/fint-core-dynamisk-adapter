package no.fintlabs.library

import no.novari.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import java.util.UUID
import kotlin.random.Random

class CustomRandomizer(
    private var random: Random,
    config: ResourceFactoryConfig,
) {
    private val firstnameList =
        config.firstnameList.withDefaults(DEFAULT_FIRSTNAMES, config.minimumRandomPoolSize)

    private val lastnameList =
        config.firstnameList.withDefaults(DEFAULT_LASTNAMES, config.minimumRandomPoolSize)

    private val funnyNameList =
        config.firstnameList.withDefaults(DEFAULT_FUNNYNAMES, config.minimumRandomPoolSize)

    private val quoteList =
        config.firstnameList.withDefaults(DEFAULT_QUOTES, 5)

    private val cityNameList =
        config.cityNameList.withDefaults(DEFAULT_CITYNAMES, 2)

    private val streetNameList =
        config.streetNameList.withDefaults(DEFAULT_STREETNAMES, 2)

    fun updateRandom(newRandom: Random) {
        random = newRandom
    }

    fun firstname(): String = firstnameList.random(random)

    fun lastname(): String = lastnameList.random(random)

    fun fullName(): String = firstname() + " " + lastname()

    fun funnyName(): String = funnyNameList.random(random)

    fun quote(): String = quoteList.random(random)

    fun uniqueFunnyName(): String = funnyNameList.random(random) + personNumber()

    fun shortNumber(): Int = (1..42).random(random)

    fun uniqueId(): String = UUID.nameUUIDFromBytes(random.nextLong().toString().toByteArray()).toString()

    fun personNumber(): String =
        (1..11)
            .map { (0..9).random(random) }
            .joinToString("")

    fun createAddress(): AdresseResource =
        AdresseResource().apply {
            adresselinje = List<String>(size = 1) { streetNameList.random() + " ," + shortNumber().toString() }
            postnummer = "237"
            poststed = cityNameList.random(random)
        }

    fun advancedString(name: String): String =
        when {
            "beskrivelse" in name || "kommentar" in name -> {
                quote()
            }

            "tittel" in name || "brukernavn" in name || "feidenavn" in name -> {
                uniqueFunnyName()
            }

            "nummer" in name -> {
                personNumber()
            }

            "kode" in name || "id" in name -> {
                uniqueId()
            }

            "bilde" in name -> {
                "https://bildeURL/${uniqueId()}"
            }

            else -> {
                fullName()
            }
        }

    private fun List<String>.withDefaults(
        defaults: List<String>,
        minimumSize: Int = 20,
    ): List<String> {
        return if (this.size >= minimumSize) {
            this
        } else {
            (this + defaults)
                .distinct()
        }
    }
}
