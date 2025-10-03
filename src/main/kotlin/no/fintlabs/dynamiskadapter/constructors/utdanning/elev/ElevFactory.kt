package no.fintlabs.dynamiskadapter.constructors.utdanning.elev

import io.github.serpro69.kfaker.Faker
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.Link
import no.fint.model.resource.felles.PersonResource
import no.fint.model.resource.utdanning.elev.ElevResource
import no.fint.model.resource.utdanning.elev.ElevforholdResource
import no.fintlabs.dynamiskadapter.constructors.felles.createPerson
import no.fintlabs.dynamiskadapter.kafka.KafkaSingleton
import no.fintlabs.dynamiskadapter.util.createPersonNumber
import no.fintlabs.dynamiskadapter.util.makeKafkaTopic

fun elevFactory(
    count: Int,
    org: String,
    domain: String,
): List<ElevResource> {
    val faker = Faker()

    val elevList = mutableListOf<ElevResource>()
    val personList = mutableListOf<PersonResource>()
    val elevforholdList = mutableListOf<ElevforholdResource>()

    for (i in 0 until count) {
        val thePerson = createPerson()
        val theElevForhold = createElevForhold()

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

        personList.add(thePerson)
        elevforholdList.add(theElevForhold)
        elevList.add(elev)
    }
    KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-elev-person"), personList)
    KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-elev-elevforhold"), elevforholdList)
    KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-elev"), elevList)

    return elevList
}
