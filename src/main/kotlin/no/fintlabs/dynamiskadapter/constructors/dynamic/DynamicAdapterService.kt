package no.fintlabs.dynamiskadapter.constructors.dynamic

import io.github.serpro69.kfaker.Faker
import no.fint.model.FintModelObject
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.utdanning.vurdering.Fravarsprosent
import no.fintlabs.dynamiskadapter.util.createAddress
import no.fintlabs.dynamiskadapter.util.createPersonNumber
import org.springframework.stereotype.Service
import java.util.Date
import kotlin.random.Random

@Service
class DynamicAdapterService {
    private val faker = Faker()

    fun getClass(enum: ResourceEnum): FintModelObject = enum.clazz.getDeclaredConstructor().newInstance()

    fun create(
        resource: ResourceEnum,
        amount: Int,
    ) {
    }

    private fun <T : Any> generateMockDataFromModel(clazz: Class<T>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        clazz.declaredFields.forEach { prop ->
            val name = prop.name.lowercase()

            val value =
                when (prop.type) {
                    // Basic Types
                    Int::class.java -> Random.nextInt()
                    Long::class.java -> Random.nextLong()
                    Boolean::class.java -> Random.nextBoolean()
                    String::class.java ->
                        when {
                            "beskrivelse" in name -> faker.starWars.quote()
                            "kommentar" in name -> faker.starWars.quote()
                            "nummer" in name -> createPersonNumber()
                            "kode" in name -> createPersonNumber()
                            "id" in name -> createPersonNumber()
                            else -> faker.name
                        }
                    // Advanced Classes

                    Date::class.java -> Date()

                    // Custom Complex Class Types
                    Identifikator::class.java ->
                        Identifikator().apply {
                            identifikatorverdi =
                                when {
                                    "navn" in name -> faker.funnyName.name()
                                    "nummer" in name -> createPersonNumber()
                                    "id" in name -> createPersonNumber()
                                    else -> createPersonNumber()
                                }
                        }
                    Personnavn::class.java ->
                        Personnavn().apply {
                            fornavn = faker.name.firstName()
                            etternavn = faker.name.lastName()
                            mellomnavn = faker.name.name()
                        }
                    Kontaktinformasjon::class.java ->
                        Kontaktinformasjon().apply {
                            epostadresse = faker.funnyName.name().trim() + "@hotmail.com"
                        }
                    Periode::class.java ->
                        Periode().apply {
                            beskrivelse = faker.starWars.quote()
                            start =
                                Date(
                                    System.currentTimeMillis() -
                                        Random.nextLong(0, 10L * 24 * 60 * 60 * 1000),
                                )
                        }
                    Fravarsprosent::class.java ->
                        Fravarsprosent().apply {
                            fravarstimer = 3
                            prosent = 10
                            undervisningstimer = 3
                        }
                    Adresse::class.java -> createAddress()
                    else -> {
                        println("DynamicAdapterService.kt - Type not specified: ${prop.type}")
                    }
                }
            result[prop.name] = value
        }
        return result
    }
}
