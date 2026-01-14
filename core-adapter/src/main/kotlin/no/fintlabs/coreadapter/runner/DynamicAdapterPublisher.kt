package no.fintlabs.coreadapter.runner

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.coreadapter.config.AdapterProperties
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val props: AdapterProperties,
) {
    fun register(capabilities: MutableSet<AdapterCapability>) {
        val contract =
            AdapterContract
                .builder()
                .adapterId(props.adapterId)
                .orgId(props.orgId)
                .username(props.username)
                .capabilities(capabilities)
                .build()

        val response =
            webClient
                .post()
                .uri("https://beta.felleskomponent.no/provider/register")
                .bodyValue(contract)
                .retrieve()
                .toBodilessEntity()
                .block()

        println(response)
        // Return 200 === Det funker // veldig bra
    }

    // Register to Provider

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
