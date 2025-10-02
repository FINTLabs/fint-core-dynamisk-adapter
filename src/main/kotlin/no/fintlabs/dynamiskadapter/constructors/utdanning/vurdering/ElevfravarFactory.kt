package no.fintlabs.dynamiskadapter.constructors.utdanning.vurdering

import no.fint.model.felles.kompleksedatatyper.Identifikator
import no.fint.model.resource.utdanning.vurdering.ElevfravarResource
import no.fintlabs.dynamiskadapter.util.createPersonNumber

fun elevFravarFactory(): ElevfravarResource =
    ElevfravarResource().apply {
        systemId =
            Identifikator().apply {
                identifikatorverdi = createPersonNumber().trim()
            }
    }
