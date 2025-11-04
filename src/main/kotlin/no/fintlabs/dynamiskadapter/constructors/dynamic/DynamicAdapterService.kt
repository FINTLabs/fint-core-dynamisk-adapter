package no.fintlabs.dynamiskadapter.constructors.dynamic

import io.github.serpro69.kfaker.Faker
import no.fint.model.FintModelObject
import no.fint.model.felles.kompleksedatatyper.Adresse
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.utdanning.vurdering.Fravarsprosent
import no.fintlabs.dynamiskadapter.util.general.createAddress
import no.fintlabs.dynamiskadapter.util.general.createPersonNumber
import org.springframework.stereotype.Service
import java.lang.reflect.Field
import java.util.Date
import kotlin.random.Random

@Service
class DynamicAdapterService {
    private val faker = Faker()

    private val blueprintCache = mutableMapOf<ResourceEnum, Map<String, () -> Any?>>()

    fun create(
        resource: ResourceEnum,
        amount: Int,
    ): List<FintModelObject> {
        val resourceClass = resource.clazz
        var blueprint: Map<String, () -> Any?>

        @Suppress("UNCHECKED_CAST")
        val concreteClass = resourceClass as Class<FintModelObject>

        if (blueprintCache.containsKey(resource)) {
            blueprint = blueprintCache[resource]!!
        } else {
            blueprint = generateBlueprint(resourceClass)
            blueprintCache[resource] = blueprint
        }
        println("CREATE::: Blueprint : $blueprint")
        return List(amount) { createInstanceFromBlueprint(concreteClass, blueprint) }
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
                                { faker.starWars.quote() }
                            }
                            "nummer" in name || "kode" in name || "id" in name -> {
                                { createPersonNumber() }
                            }
                            else -> {
                                { faker.name.firstName() }
                            }
                        }

                    Date::class.java -> {
                        { Date() }
                    }

                    Identifikator::class.java -> {
                        {
                            Identifikator().apply {
                                identifikatorverdi =
                                    if ("navn" in name) {
                                        faker.funnyName.name()
                                    } else {
                                        createPersonNumber()
                                    }
                            }
                        }
                    }

                    Kontaktinformasjon::class.java -> {
                        {
                            Kontaktinformasjon().apply {
                                epostadresse = faker.funnyName.name().trim() + "@hotmail.com"
                            }
                        }
                    }

                    Personnavn::class.java -> {
                        {
                            Personnavn().apply {
                                fornavn = faker.name.firstName()
                                etternavn = faker.name.lastName()
                                mellomnavn = faker.name.name()
                            }
                        }
                    }

                    Periode::class.java -> {
                        {
                            Periode().apply {
                                beskrivelse = faker.starWars.quote()
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
                        { createAddress() }
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

    private fun <T : FintModelObject> createInstanceFromBlueprint(
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
