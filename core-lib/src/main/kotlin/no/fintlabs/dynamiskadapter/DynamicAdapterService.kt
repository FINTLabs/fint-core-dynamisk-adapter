package no.fintlabs.dynamiskadapter

import no.fint.model.administrasjon.kompleksedatatyper.Kontostreng
import no.fint.model.arkiv.noark.Avskrivning
import no.fint.model.arkiv.noark.Dokumentbeskrivelse
import no.fint.model.arkiv.noark.Dokumentobjekt
import no.fint.model.arkiv.noark.Journalpost
import no.fint.model.arkiv.noark.Klasse
import no.fint.model.arkiv.noark.Korrespondansepart
import no.fint.model.arkiv.noark.Merknad
import no.fint.model.arkiv.noark.Part
import no.fint.model.arkiv.noark.Skjerming
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Matrikkelnummer
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.okonomi.faktura.Fakturalinje
import no.fint.model.okonomi.faktura.Fakturamottaker
import no.fint.model.okonomi.regnskap.Bilag
import no.fint.model.resource.FintResource
import no.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.fint.model.resource.arkiv.noark.DokumentobjektResource
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import no.fint.model.resource.okonomi.faktura.FakturalinjeResource
import no.fint.model.resource.okonomi.faktura.FakturamottakerResource
import no.fint.model.utdanning.vurdering.Fravarsprosent
import java.lang.reflect.Field
import java.util.Date
import kotlin.random.Random

class DynamicAdapterService {
    private val blueprintCache = mutableMapOf<String, Map<String, () -> Any?>>()
    private val randomizer: CustomRandomizer = CustomRandomizer()

    fun create(
        resource: Class<out FintResource>,
        amount: Int,
    ): List<FintResource> {
        val resourceName = resource.simpleName
        var blueprint: Map<String, () -> Any?>

        if (blueprintCache.containsKey(resourceName)) {
            blueprint = blueprintCache[resourceName]!!
        } else {
            blueprint = generateBlueprint(resource)
            blueprintCache[resourceName] = blueprint
        }
        println("📋 DynamicAdapterService: creating ${amount}x $resourceName")
        return List(amount) { createInstanceFromBlueprint(resource, blueprint) }
    }

    private fun getAllUniqueFields(clazz: Class<*>): List<Field> {
        val fields = mutableListOf<Field>()
        val names = mutableSetOf<String>()

        var current: Class<*>? = clazz
        while (current != null && current != Any::class.java) {
            current.declaredFields
                .filter { names.add(it.name) }
                .forEach { fields.add(it) }

            current = current.superclass
        }

        return fields
    }

    private fun <T : Any> generateBlueprint(clazz: Class<T>): Map<String, () -> Any?> {
        val generators = mutableMapOf<String, () -> Any?>()

        getAllUniqueFields(clazz).forEach { field ->
            val name = field.name.lowercase()

            val generator: () -> Any? =
                when (field.type) {
                    // Basic Java classes

                    Int::class.java, Integer::class.java -> {
                        { Random.nextInt() }
                    }

                    Long::class.java, java.lang.Long::class.java -> {
                        { Random.nextLong() }
                    }

                    Boolean::class.java, java.lang.Boolean::class.java -> {
                        { Random.nextBoolean() }
                    }

                    String::class.java -> {
                        {
                            randomizer.advancedString(name)
                        }
                    }

                    List::class.java -> {
                        { mutableListOf<String>() }
                    }

                    Map::class.java -> {
                        { mutableMapOf<Any?, Any?>() }
                    }

                    Date::class.java -> {
                        { Date() }
                    }

                    // Complex Fint Datatypes

                    Adresse::class.java -> {
                        { randomizer.createAddress() }
                    }

                    AdresseResource::class.java -> {
                        { AdresseResource() }
                    }

                    Avskrivning::class.java -> {
                        {
                            Avskrivning().apply {
                                avskrevetAv = randomizer.fullName()
                                avskrivningsdato = Date()
                                avskrivningsmate = randomizer.quote()
                            }
                        }
                    }

                    Bilag::class.java -> {
                        {
                            Bilag().apply {
                                bilagsdato =
                                    Date(
                                        System.currentTimeMillis() -
                                            Random.nextLong(0, 10L * 24 * 60 * 60 * 1000),
                                    )
                            }
                        }
                    }

                    Dokumentbeskrivelse::class.java -> {
                        {
                            Dokumentbeskrivelse().apply {
                                tittel = randomizer.funnyName()
                            }
                        }
                    }

                    DokumentbeskrivelseResource::class.java -> {
                        {
                            DokumentbeskrivelseResource().apply {
                                tittel = randomizer.fullName()
                            }
                        }
                    }

                    Dokumentobjekt::class.java -> {
                        {
                            Dokumentobjekt()
                        }
                    }

                    DokumentobjektResource::class.java -> {
                        {
                            DokumentobjektResource()
                        }
                    }

                    Fakturalinje::class.java -> {
                        {
                            Fakturalinje().apply {
                                antall = 3.14F
                                pris = 100
                            }
                        }
                    }

                    FakturalinjeResource::class.java -> {
                        {
                            FakturalinjeResource().apply {
                                antall = 3.14F
                                pris = 100
                            }
                        }
                    }

                    Fakturamottaker::class.java -> {
                        {
                            Fakturamottaker()
                        }
                    }

                    FakturamottakerResource::class.java -> {
                        {
                            FakturamottakerResource()
                        }
                    }

                    Fravarsprosent::class.java -> {
                        {
                            Fravarsprosent().apply {
                                fravarstimer = randomizer.shortNumber()
                                prosent = randomizer.shortNumber()
                                undervisningstimer = randomizer.shortNumber()
                            }
                        }
                    }

                    Identifikator::class.java -> {
                        {
                            Identifikator().apply {
                                identifikatorverdi = randomizer.advancedString(name)
                            }
                        }
                    }

                    Journalpost::class.java -> {
                        {
                            Journalpost().apply {
                                tittel = randomizer.funnyName()
                            }
                        }
                    }

                    Klasse::class.java -> {
                        {
                            Klasse().apply {
                                klasseId = randomizer.personNumber()
                                tittel = randomizer.funnyName()
                            }
                        }
                    }

                    Kontaktinformasjon::class.java -> {
                        {
                            Kontaktinformasjon().apply {
                                epostadresse = randomizer.funnyName() + "@hotmail.com"
                            }
                        }
                    }

                    Kontostreng::class.java -> {
                        {
                            Kontostreng()
                        }
                    }

                    Korrespondansepart::class.java -> {
                        {
                            Korrespondansepart()
                        }
                    }

                    Matrikkelnummer::class.java -> {
                        {
                            Matrikkelnummer()
                        }
                    }

                    Merknad::class.java -> {
                        {
                            Merknad().apply {
                                merknadsdato = Date()
                                merknadstekst = randomizer.quote()
                            }
                        }
                    }

                    Part::class.java -> {
                        {
                            Part().apply {
                                partNavn = randomizer.funnyName()
                            }
                        }
                    }

                    Periode::class.java -> {
                        {
                            Periode().apply {
                                beskrivelse = randomizer.quote()
                                start =
                                    Date(
                                        System.currentTimeMillis() -
                                            Random.nextLong(0, 10L * 24 * 60 * 60 * 1000),
                                    )
                            }
                        }
                    }

                    Personnavn::class.java -> {
                        {
                            Personnavn().apply {
                                fornavn = randomizer.firstname()
                                etternavn = randomizer.lastname()
                                mellomnavn = ""
                            }
                        }
                    }

                    Skjerming::class.java -> {
                        {
                            Skjerming()
                        }
                    }

                    else -> {
                        {
                            println("⚠️ Type not specified: ${field.type}")
                            null
                        }
                    }
                }

            generators[field.name] = generator
        }

        return generators
    }

    private fun <T : FintResource> createInstanceFromBlueprint(
        clazz: Class<T>,
        blueprint: Map<String, () -> Any?>,
    ): T {
        val instance = clazz.getDeclaredConstructor().newInstance()

        val superField =
            try {
                clazz.getDeclaredField("super").apply { isAccessible = true }
            } catch (_: NoSuchFieldException) {
                null
            }

        val targetRoot: Any =
            if (superField != null) {
                val current = superField.get(instance)
                if (current != null) {
                    current
                } else {
                    val superType = superField.type
                    val created = superType.getDeclaredConstructor().newInstance()
                    superField.set(instance, created)
                    created
                }
            } else {
                instance
            }

        for ((fieldName, generator) in blueprint) {
            if (fieldName == "writeable") continue

            val field =
                findDeclaredFieldRecursive(targetRoot.javaClass, fieldName)
                    ?: findDeclaredFieldRecursive(clazz, fieldName)

            if (field == null) {
                println("⚠️ Field not found: $fieldName in ${clazz.simpleName}")
                continue
            }

            val value = generator()
            try {
                field.set(if (field.declaringClass.isInstance(targetRoot)) targetRoot else instance, value)
            } catch (e: Exception) {
                println("⚠️ Could not set field '$fieldName' in ${clazz.simpleName}: ${e.message}")
            }
        }

        return instance
    }

    private fun findDeclaredFieldRecursive(
        clazz: Class<*>,
        name: String,
    ): Field? =
        generateSequence(clazz as Class<*>?) { it.superclass }
            .mapNotNull { c ->
                try {
                    c.getDeclaredField(name).apply { isAccessible = true }
                } catch (_: NoSuchFieldException) {
                    null
                }
            }.firstOrNull()
}
