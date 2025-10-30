package no.fintlabs.dynamiskadapter.util.general

import io.github.serpro69.kfaker.Faker
import no.fint.model.resource.felles.kompleksedatatyper.AdresseResource

fun createAddress(): AdresseResource {
    val faker = Faker()
    return AdresseResource().apply {
        adresselinje = List<String>(size = 1) { faker.address.streetName() }
        postnummer = "666"
        poststed = faker.address.city()
    }
}
