package no.fintlabs.dynamiskadapter.constructors.utdanning.vurdering

import io.github.serpro69.kfaker.Faker
import no.fint.model.resource.utdanning.vurdering.FravarsregistreringResource
import no.fintlabs.dynamiskadapter.kafka.KafkaSingleton
import no.fintlabs.dynamiskadapter.util.makeKafkaTopic

class noStudentsException(
    message: String,
) : Exception(message)

fun fravarsRegistreringFactory(
    count: Int,
    org: String,
    domain: String,
): List<FravarsregistreringResource?> {
    val faker = Faker()

    val elevFravarList = KafkaSingleton.readAll(makeKafkaTopic(org, domain, "utdanning-elev-fravær"))
    if (elevFravarList.isNotEmpty()) {
    } else {
        throw noStudentsException("You can not create FravarsRegistreringer without Elever.")
    }
}
