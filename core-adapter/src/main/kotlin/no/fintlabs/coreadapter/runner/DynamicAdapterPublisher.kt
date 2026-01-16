package no.fintlabs.coreadapter.runner

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.coreadapter.config.AdapterProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val props: AdapterProperties,
) {
    val registered: Boolean = false

    fun register(capabilities: MutableSet<AdapterCapability>) {
        val contract =
            AdapterContract
                .builder()
                .adapterId(props.adapterId)
                .orgId(props.orgId)
                .username(props.username)
                .heartbeatIntervalInMinutes(props.heartbeatIntervalInMinutes)
                .capabilities(capabilities)
                .build()

        val response =
            webClient
                .post()
                .uri("https://beta.felleskomponent.no/provider/register")
                .body(Mono.just(contract), AdapterContract::class.java)
                .retrieve()
                .toBodilessEntity()
                .subscribe()

        println(response.toString())
        // Return 200 === Det funker // veldig bra
    }

    // resourceList.name = utdanning/vurdering/elevfravar
//    fun publishResource(capability: AdapterCapability, data: List<FintResource>) {
//        webClient.post()
//            .uri("https://beta.felleskomponent.no/provider/${resourceList[].name}")
//            .bodyValue(SyncPage.builder().)
//            .retrieve()
//            .toBodilessEntity()

    // Return 200 === Det funker // veldig bra
//     }
}
