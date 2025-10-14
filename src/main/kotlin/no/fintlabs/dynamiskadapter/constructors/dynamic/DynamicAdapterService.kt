package no.fintlabs.dynamiskadapter.constructors.dynamic

import io.github.serpro69.kfaker.Faker
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
import kotlin.reflect.KClass
import kotlin.reflect.full.memberProperties

@Service
class DynamicAdapterService {
    private val faker = Faker()

    fun create(
        resource: ResourceEnum,
        amount: Int,
    ) {
    }

    private fun <T : Any> generateMockDataFromModel(clazz: KClass<T>): Map<String, Any> {
        val result = mutableMapOf<String, Any>()

        clazz.memberProperties.forEach { prop ->

            val name = prop.name.lowercase()
            val value =
                when (prop.returnType.classifier) {
                    // Basic Types
                    Int::class -> Random.nextInt()
                    Long::class -> Random.nextLong()
                    Boolean::class -> Random.nextBoolean()
                    String::class ->
                        when {
                            "beskrivelse" in name -> faker.starWars.quote()
                            "kommentar" in name -> faker.starWars.quote()
                            "nummer" in name -> createPersonNumber()
                            "kode" in name -> createPersonNumber()
                            "id" in name -> createPersonNumber()
                            else -> faker.name
                        }
                    // Advanced Classes

                    Date::class -> Date()

                    // Custom Complex Class Types
                    Identifikator::class ->
                        Identifikator().apply {
                            identifikatorverdi =
                                when {
                                    "navn" in name -> faker.funnyName.name()
                                    "nummer" in name -> createPersonNumber()
                                    "id" in name -> createPersonNumber()
                                    else -> createPersonNumber()
                                }
                        }
                    Personnavn::class ->
                        Personnavn().apply {
                            fornavn = faker.name.firstName()
                            etternavn = faker.name.lastName()
                            mellomnavn = faker.name.name()
                        }
                    Kontaktinformasjon::class ->
                        Kontaktinformasjon().apply {
                            epostadresse = faker.funnyName.name().trim() + "@hotmail.com"
                        }
                    Periode::class ->
                        Periode().apply {
                            beskrivelse = faker.starWars.quote()
                            start =
                                Date(
                                    System.currentTimeMillis() -
                                        Random.nextLong(0, 10L * 24 * 60 * 60 * 1000),
                                )
                        }
                    Fravarsprosent::class ->
                        Fravarsprosent().apply {
                            fravarstimer = 3
                            prosent = 10
                            undervisningstimer = 3
                        }
                    Adresse::class -> createAddress()
                    else -> {
                        println("DynamicAdapterService.kt - Type not specified: ${prop.returnType}")
                    }
                }
        }

        return result
    }
}
