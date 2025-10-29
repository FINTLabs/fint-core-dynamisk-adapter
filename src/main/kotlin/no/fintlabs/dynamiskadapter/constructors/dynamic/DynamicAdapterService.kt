package no.fintlabs.dynamiskadapter.constructors.dynamic

import io.github.serpro69.kfaker.Faker
import jakarta.validation.constraints.NotNull
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

    private val blueprintCache = mutableMapOf<ResourceEnum, Map<String, () -> Any?>>()

    private fun getClass(enum: ResourceEnum): FintModelObject = enum.clazz.getDeclaredConstructor().newInstance()

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

        return List(amount) { createInstanceFromBlueprint(concreteClass, blueprint) }
    }

    private fun <T : Any> generateBlueprint(clazz: Class<T>): Map<String, () -> Any?> {
        val generators = mutableMapOf<String, () -> Any?>()

        clazz.declaredFields.forEach { field ->
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

                    Personnavn::class.java -> {
                        {
                            Personnavn().apply {
                                fornavn = faker.name.firstName()
                                etternavn = faker.name.lastName()
                                mellomnavn = faker.name.name()
                            }
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

    private fun <T : FintModelObject> createInstanceFromBlueprint(
        clazz: Class<T>,
        blueprint: Map<String, () -> Any?>,
    ): T {
        val instance = clazz.getDeclaredConstructor().newInstance()

        blueprint.forEach { (fieldName, generator) ->
            val field = clazz.getDeclaredField(fieldName)
            field.isAccessible = true

            field.set(instance, generator())
        }
        return instance
    }
}
