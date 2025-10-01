package no.fintlabs.dynamiskadapter.constructors.utdanning

import io.github.serpro69.kfaker.Faker
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.ElevResource
import no.fintlabs.dynamiskadapter.util.createPersonNumber

fun elevFactory(Count: Int): List<ElevResource> {
    val faker = Faker()
    val thePerson = createPerson()

    val elevList = mutableListOf<ElevResource>()
    for (i in 0 until Count) {
        val username =
            Identifikator().apply {
                identifikatorverdi = faker.funnyName.name()
            }

        val studentNumber =
            Identifikator().apply {
                identifikatorverdi = createPersonNumber()
            }

        val elev: ElevResource =
            ElevResource().apply {
                addPerson(Link.with("fodselsnummer/${thePerson.fodselsnummer.identifikatorverdi}"))

                brukernavn = username
                elevnummer = studentNumber
                feidenavn = username
                gjest = false
                hybeladresse = thePerson.bostedsadresse
                kontaktinformasjon = thePerson.kontaktinformasjon
                systemId = studentNumber
            }
        thePerson.apply {
            addElev(Link.with("systemId/${elev.systemId.identifikatorverdi}"))
        }

        elevList.add(elev)
    }
    return elevList
}
