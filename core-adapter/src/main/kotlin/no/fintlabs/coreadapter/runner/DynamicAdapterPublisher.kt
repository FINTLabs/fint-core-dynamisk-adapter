package no.fintlabs.coreadapter.runner

import no.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.publish.SyncPageFactory
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val props: AdapterProperties,
    private val factory: SyncPageFactory,
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
        println("🔑 Adapter Registration :  $response")
    }

    private fun postSyncPage(
        resourceName: String,
        page: SyncPage,
    ) = webClient
        .post()
        .uri("${props.baseUrl}/provider/$resourceName")
        .bodyValue(page)
        .exchangeToMono { response ->
            response
                .bodyToMono(String::class.java)
                .defaultIfEmpty("")
                .map { body -> response.statusCode() to body }
        }

    fun fullSyncResource(
        resourceName: String,
        data: List<FintResource>,
    ) {
        val entries = factory.buildEntries(data)

        val meta =
            factory.buildMetadata(
                resourceName = resourceName,
                page = 0,
                pageSize = entries.size.toLong(),
                totalPages = 1,
                totalSize = entries.size.toLong(),
            )

        val page = factory.buildPage(SyncType.FULL, meta, entries)

        val (status, body) =
            postSyncPage(resourceName, page).block()
                ?: error("No response from provider")

        println("📤 FULL $resourceName => HTTP $status, body='${body.take(500)}'")
    }
}
