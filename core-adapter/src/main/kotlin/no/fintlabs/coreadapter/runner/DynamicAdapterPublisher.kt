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
                .heartbeatIntervalInMinutes(props.heartbeatIntervalInMinutes)
                .capabilities(capabilities)
                .time(0L)
                .build()

        val response =
            webClient
                .post()
                .uri("${props.baseUrl}/provider/register")
                .bodyValue(contract)
                .exchangeToMono { response ->
                    response
                        .bodyToMono(String::class.java)
                        .defaultIfEmpty("empty")
                        .map { body ->
                            response.statusCode().value() to body
                        }
                }.block()
        // Return 200 === Det funker // veldig bra
        println(response)
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
