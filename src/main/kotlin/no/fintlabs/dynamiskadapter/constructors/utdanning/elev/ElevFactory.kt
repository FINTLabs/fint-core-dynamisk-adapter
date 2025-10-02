package no.fintlabs.dynamiskadapter.constructors.utdanning.elev

import io.github.serpro69.kfaker.Faker
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.ElevResource
import no.fint.model.resource.utdanning.elev.ElevforholdResource
import no.fintlabs.dynamiskadapter.constructors.felles.createPerson
import no.fintlabs.dynamiskadapter.util.createPersonNumber

fun elevFactory(
    count: Int,
    org: String,
    domain: String,
): List<ElevResource> {
    val faker = Faker()

    val elevList = mutableListOf<ElevResource>()
    for (i in 0 until count) {
        val thePerson = createPerson()
        val theElevForhold = createElevForhold()

        val username =
            Identifikator().apply {
                identifikatorverdi = faker.funnyName.name()
            }

        val studentNumber =
            Identifikator().apply {
                identifikatorverdi = createPersonNumber().trim()
            }

        val elev: ElevResource =
            ElevResource().apply {
                addPerson(Link.with("fodselsnummer/${thePerson.fodselsnummer.identifikatorverdi}"))
                addElevforhold(Link.with("systemId/${theElevForhold.systemId.identifikatorverdi}"))

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
        theElevForhold.apply {
            addElev(Link.with("systemId/${elev.systemId.identifikatorverdi}"))
        }

        elevList.add(elev)
    }
    return elevList
}

fun createElevForhold(): ElevforholdResource =
    ElevforholdResource()
        .apply {
            systemId =
                Identifikator().apply {
                    identifikatorverdi = createPersonNumber().trim()
                }
        }
