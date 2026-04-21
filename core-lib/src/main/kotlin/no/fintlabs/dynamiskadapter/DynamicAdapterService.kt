package no.fintlabs.dynamiskadapter

import no.novari.fint.model.administrasjon.kompleksedatatyper.Kontostreng
import no.novari.fint.model.arkiv.noark.Avskrivning
import no.novari.fint.model.arkiv.noark.Dokumentbeskrivelse
import no.novari.fint.model.arkiv.noark.Dokumentobjekt
import no.novari.fint.model.arkiv.noark.Journalpost
import no.novari.fint.model.arkiv.noark.Korrespondansepart
import no.novari.fint.model.arkiv.noark.Merknad
import no.novari.fint.model.arkiv.noark.Part
import no.novari.fint.model.arkiv.noark.Skjerming
import no.novari.fint.model.felles.kompleksedatatyper.Adresse
import no.novari.fint.model.felles.kompleksedatatyper.Identifikator
import no.novari.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.novari.fint.model.felles.kompleksedatatyper.Matrikkelnummer
import no.novari.fint.model.felles.kompleksedatatyper.Periode
import no.novari.fint.model.felles.kompleksedatatyper.Personnavn
import no.novari.fint.model.okonomi.faktura.Fakturalinje
import no.novari.fint.model.okonomi.faktura.Fakturamottaker
import no.novari.fint.model.okonomi.regnskap.Bilag
import no.novari.fint.model.resource.FintResource
import no.novari.fint.model.resource.arkiv.noark.DokumentbeskrivelseResource
import no.novari.fint.model.resource.arkiv.noark.DokumentobjektResource
import no.novari.fint.model.resource.felles.kompleksedatatyper.AdresseResource
import no.novari.fint.model.resource.okonomi.faktura.FakturalinjeResource
import no.novari.fint.model.resource.okonomi.faktura.FakturamottakerResource
import no.novari.fint.model.utdanning.elev.Klasse
import no.novari.fint.model.utdanning.vurdering.Fravarsprosent
import java.lang.reflect.Field
import java.util.Date
import kotlin.random.Random

private enum class FaultType {
    NONE,
    MISSING,
    WRONG,
}

class DynamicAdapterService {
    private val blueprintCache = mutableMapOf<String, Map<String, () -> Any?>>()
    private val randomizer: CustomRandomizer = CustomRandomizer()

    fun create(
        resource: Class<out FintResource>,
        amount: Int,
        logging: Boolean = false,
        errorPercentage: Int = 0
    ): List<FintResource> {
        val resourceName = resource.simpleName
        var blueprint: Map<String, () -> Any?>

        if (blueprintCache.containsKey(resourceName)) {
            blueprint = blueprintCache[resourceName]!!
        } else {
            blueprint = generateBlueprint(logging, resource)
            blueprintCache[resourceName] = blueprint
        }
        logIfEnabled(logging, "📋${amount}x $resource")
        return List(amount) { createInstanceFromBlueprint(logging, resource, blueprint, errorPercentage) }
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

    private fun <T : FintResource> generateBlueprint(
        logging: Boolean,
        clazz: Class<T>,
    ): Map<String, () -> Any?> {
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
                                beskrivelse = randomizer.quote()
                                navn = randomizer.funnyName()
                                systemId = Identifikator().apply {
                                    identifikatorverdi = randomizer.advancedString(name)
                                }
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
                            logIfEnabled(logging, "⚠️ Type not specified: ${field.type}")
                            null
                        }
                    }
                }

            generators[field.name] = generator
        }

        return generators
    }

    private object SKIP_FIELD

    private fun <T : FintResource> createInstanceFromBlueprint(
        logging: Boolean,
        clazz: Class<T>,
        blueprint: Map<String, () -> Any?>,
        errorPercentage: Int
    ): FintResource {
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
                logIfEnabled(logging, "⚠️ Field not found: $fieldName in ${clazz.simpleName}")
                continue
            }

            val fault =
                if (errorPercentage > 0 && !isCriticalField(field)) rollFault(errorPercentage) else FaultType.NONE
            val value = applyFault(field, generator, fault, logging)
            if (value === SKIP_FIELD) {
                continue
            }

            try {
                field.set(
                    if (field.declaringClass.isInstance(targetRoot)) targetRoot else instance,
                    value
                )
            } catch (e: Exception) {
                logIfEnabled(logging, "⚠️ Could not set field '$fieldName' in ${clazz.simpleName}: ${e.message}")
            }
        }

        return instance as FintResource
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

    private fun isCriticalField(field: Field): Boolean =
        field.type == Identifikator::class.java ||
                field.name.equals("systemId", ignoreCase = true)

    private fun applyFault(
        field: Field,
        generator: () -> Any?,
        fault: FaultType,
        logging: Boolean,
    ): Any? {
        return when (fault) {
            FaultType.NONE -> generator()

            FaultType.MISSING -> {
                // Best default behavior: leave field unset if it's primitive or awkward.
                // Return a marker so caller can skip setting entirely.
                SKIP_FIELD
            }

            FaultType.WRONG -> {
                generateWrongValue(field.type, generator(), logging)
            }
        }
    }

    private fun rollFault(errorPercentage: Int): FaultType {
        if (errorPercentage <= 0) return FaultType.NONE

        val roll = Random.nextInt(100)
        if (roll >= errorPercentage) return FaultType.NONE

        return if (Random.nextBoolean()) FaultType.MISSING else FaultType.WRONG
    }

    private fun generateWrongValue(
        type: Class<*>,
        normalValue: Any?,
        logging: Boolean,
    ): Any? {
        return when (type) {
            Int::class.java, Integer::class.java -> -1
            Long::class.java, java.lang.Long::class.java -> -1L
            Boolean::class.java, java.lang.Boolean::class.java -> !(normalValue as? Boolean ?: false)
            String::class.java -> "INVALID_${Random.nextInt(1000, 9999)}"
            Date::class.java -> Date(0) // epoch, often "wrong enough"

            List::class.java -> emptyList<Any>()
            Map::class.java -> emptyMap<Any, Any>()

            Kontaktinformasjon::class.java -> {
                Kontaktinformasjon().apply {
                    epostadresse = "not-an-email"
                }
            }

            Periode::class.java -> {
                Periode().apply {
                    beskrivelse = "INVALID_PERIOD"
                    start = Date(0)
                    slutt = Date(0)
                }
            }

            else -> {
                logIfEnabled(logging, "⚠️ No wrong-value strategy for type $type, falling back to normal value")
                normalValue
            }
        }
    }


    private fun logIfEnabled(
        bool: Boolean,
        input: String,
    ) {
        if (bool) println(input)
    }
}
