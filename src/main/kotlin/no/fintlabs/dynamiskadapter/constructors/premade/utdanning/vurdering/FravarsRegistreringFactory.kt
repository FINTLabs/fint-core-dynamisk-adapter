package no.fintlabs.dynamiskadapter.constructors.premade.utdanning.vurdering

import io.github.serpro69.kfaker.Faker
import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.felles.kompleksedatatyper.Periode
import no.fint.model.resource.Link
import no.fint.model.resource.utdanning.elev.ElevforholdResource
import no.fint.model.resource.utdanning.vurdering.ElevfravarResource
import no.fint.model.resource.utdanning.vurdering.FravarsregistreringResource
import no.fintlabs.dynamiskadapter.kafka.KafkaSingleton
import no.fintlabs.dynamiskadapter.kafka.makeKafkaTopic
import no.fintlabs.dynamiskadapter.util.general.createPersonNumber

class NoStudentsException(
    message: String,
) : Exception(message)

fun fravarsRegistreringFactory(
    count: Int,
    org: String,
    domain: String,
): List<FravarsregistreringResource?> {
    val faker = Faker()

    val elevFravarsRegistreringList = mutableListOf<FravarsregistreringResource>()
    val elevFravarList = mutableListOf<ElevfravarResource>()
    val newElevForholdList = mutableListOf<ElevforholdResource>()

    val elevForholdList = KafkaSingleton.readAll<ElevforholdResource>(makeKafkaTopic(org, domain, "utdanning-elev-elevforhold"))

    if (!elevForholdList.isNullOrEmpty()) {
        @Suppress("UNUSED_VARIABLE")
        for (i in 0 until count) {
            val fravarsRegistrering: FravarsregistreringResource =
                FravarsregistreringResource().apply {
                    foresPaVitnemal = true
                    kommentar = faker.starWars.quote()
                    periode =
                        Periode().apply {
                            beskrivelse = faker.starWars.quote()
                        }
                    systemId =
                        Identifikator().apply {
                            identifikatorverdi = createPersonNumber()
                        }
                }
            val randomElevForhold: ElevforholdResource = elevForholdList.random()

            val elevfravar: ElevfravarResource =
                elevFravarFactory().apply {
                    systemId =
                        Identifikator().apply {
                            identifikatorverdi = createPersonNumber()
                        }
                    addFravarsregistrering(Link.with("systemId/${fravarsRegistrering.systemId.identifikatorverdi}"))
                    addElevforhold(Link.with("systemId/${randomElevForhold.systemId.identifikatorverdi}"))
                }
            randomElevForhold.apply { addElevfravar(Link.with("systemId/${elevfravar.systemId.identifikatorverdi}")) }
            fravarsRegistrering.apply { addElevfravar(Link.with("systemId/${elevfravar.systemId.identifikatorverdi}")) }

            elevFravarList.add(elevfravar)
            newElevForholdList.add(randomElevForhold)
            elevFravarsRegistreringList.add(fravarsRegistrering)
        }

        KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-elev-elevforhold"), newElevForholdList)
        KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-vurdering-elevfravar"), elevFravarList)
        KafkaSingleton.publish(makeKafkaTopic(org, domain, "utdanning-vurdering-fravarsregistrering"), elevFravarsRegistreringList)

        return elevFravarsRegistreringList
    } else {
        throw NoStudentsException("You can not create FravarsRegistreringer without Elever.")
    }
}
