package no.fintlabs.dynamiskadapter.constructors.utdanning.elev

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.ElevforholdResource
import no.fintlabs.dynamiskadapter.util.createPersonNumber

fun createElevForhold(): ElevforholdResource =
    ElevforholdResource()
        .apply {
            systemId =
                Identifikator().apply {
                    identifikatorverdi = createPersonNumber().trim()
                }
        }
