package no.fintlabs.dynamiskadapter

import no.fint.model.felles.kompleksedatatyper.*
import no.fint.model.resource.FintResource
import no.fint.model.utdanning.vurdering.Fravarsprosent
import java.lang.reflect.Field
import java.util.*
import kotlin.random.Random

class DynamicAdapterService {
    private val blueprintCache = mutableMapOf<String, Map<String, () -> Any?>>()
    private val randomizer: CustomRandomizer = CustomRandomizer()

    fun create(
        resource: Class<out FintResource>,
        className: String,
        amount: Int,
    ): List<FintResource> {
        var blueprint: Map<String, () -> Any?>

        if (blueprintCache.containsKey(className)) {
            blueprint = blueprintCache[className]!!
        } else {
            blueprint = generateBlueprint(resource)
            blueprintCache[className] = blueprint
            println("📋 DynamicAdapterService.create generated blueprint: $className")
        }
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
                    Int::class.java, Integer::class.java -> {
                        { Random.nextInt() }
                    }

                    Long::class.java, java.lang.Long::class.java -> {
                        { Random.nextLong() }
                    }

                    Boolean::class.java, java.lang.Boolean::class.java -> {
                        { Random.nextBoolean() }
                    }

                    String::class.java ->
                        when {
                            "beskrivelse" in name || "kommentar" in name -> {
                                { randomizer.quote() }
                            }

                            "nummer" in name || "kode" in name || "id" in name -> {
                                { randomizer.personNumber() }
                            }

                            else -> {
                                { randomizer.fullName() }
                            }
                        }

                    Date::class.java -> {
                        { Date() }
                    }

                    List::class.java -> {
                        { emptyList<String>() }
                    }

                    Map::class.java -> {
                        { emptyMap<Any?, Any?>() }
                    }

                    Identifikator::class.java -> {
                        {
                            Identifikator().apply {
                                identifikatorverdi =
                                    if ("navn" in name) {
                                        randomizer.fullName()
                                    } else {
                                        randomizer.personNumber()
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

                    Personnavn::class.java -> {
                        {
                            Personnavn().apply {
                                fornavn = randomizer.firstname()
                                etternavn = randomizer.lastname()
                                mellomnavn = ""
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

                    Fravarsprosent::class.java -> {
                        {
                            Fravarsprosent().apply {
                                fravarstimer = 3
                                prosent = 10
                                undervisningstimer = 3
                            }
                        }
                    }

                    Adresse::class.java -> {
                        { randomizer.createAddress() }
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

        for ((fieldName, generator) in blueprint) {
            if (fieldName == "writeable") continue

            // Try to find field in class hierarchy
            val field =
                generateSequence(clazz as Class<*>?) { it.superclass }
                    .mapNotNull { c ->
                        try {
                            c.getDeclaredField(fieldName).apply { isAccessible = true }
                        } catch (_: NoSuchFieldException) {
                            null
                        }
                    }.firstOrNull()

            if (field == null) {
                println("⚠️ Field not found: $fieldName in ${clazz.simpleName}")
                continue
            }

            val value = generator()
            try {
                field.set(instance, value)
            } catch (e: Exception) {
                println("⚠️ Could not set field '$fieldName' in ${clazz.simpleName}: ${e.message}")
            }
        }

        return instance
    }
}