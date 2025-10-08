package no.fintlabs.dynamiskadapter.constructors.premade.felles

import io.github.serpro69.kfaker.Faker
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Kontaktinformasjon
import no.fint.model.felles.kompleksedatatyper.Personnavn
import no.fint.model.resource.felles.PersonResource
import no.fintlabs.dynamiskadapter.util.createAddress
import no.fintlabs.dynamiskadapter.util.createPersonNumber
import java.util.Date

fun personFactory(): PersonResource {
    val faker = Faker()

    val address = createAddress()

    val birthNumber =
        Identifikator().apply {
            identifikatorverdi = createPersonNumber()
        }

    val name =
        Personnavn().apply {
            fornavn = faker.name.firstName()
            etternavn = faker.name.lastName()
            mellomnavn = faker.funnyName.name()
        }

    val contactInfo =
        Kontaktinformasjon().apply {
            epostadresse = faker.funnyName.name() + "@hotmail.com"
        }

    return PersonResource().apply {
        bilde = "bilde url"
        bostedsadresse = address
        fodselsdato = Date()
        fodselsnummer = birthNumber
        navn = name
        kontaktinformasjon = contactInfo
        postadresse = address
    }
}
