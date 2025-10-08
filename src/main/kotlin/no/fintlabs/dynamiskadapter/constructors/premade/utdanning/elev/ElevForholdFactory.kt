package no.fintlabs.dynamiskadapter.constructors.premade.utdanning.elev

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.elev.ElevforholdResource
import no.fintlabs.dynamiskadapter.util.createPersonNumber

fun elevForholdFactory(): ElevforholdResource =
    ElevforholdResource()
        .apply {
            systemId =
                Identifikator().apply {
                    identifikatorverdi = createPersonNumber().trim()
                }
        }
