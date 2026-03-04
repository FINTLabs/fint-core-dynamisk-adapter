package no.fintlabs.coreadapter.publish

import no.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.data.models.HeartBeatRequest
import no.fintlabs.coreadapter.store.ResourceStore
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Instant

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val storage: ResourceStore,
    private val factory: SyncPageFactory,
    private val props: AdapterProperties,
    private val dynaProps: DynamicAdapterProperties,
) {
    fun register(capabilities: MutableSet<AdapterCapability>): Boolean {
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
        return response!!.first == 200
    }

    fun giveHeartBeat() {
        val requestBody =
            HeartBeatRequest(
                props.adapterId,
                props.username,
                props.orgId,
                time = Instant.now().epochSecond,
            )
        val response =
            webClient
                .post()
                .uri("${props.baseUrl}/provider/heartbeat")
                .bodyValue(requestBody)
                .exchangeToMono { response -> Mono.just(response.statusCode().value()) }
                .block()

        println("🫀 HeartBeat => HTTP $response")
    }

    fun performFullSync(metadataList: MutableList<ExpandedMetadata>) {
        for (metadata in metadataList) {
            val data = storage.getAllResources(metadata.key)
            if (data.isNotEmpty()) {
                publish(metadata.key, SyncType.FULL, data)
            } else {
                println("No data found in LOCAL_STORAGE for ${metadata.key}")
            }
        }
    }

    fun deltaSyncResource(
        resourceName: String,
        data: List<FintResource>,
    ) = publish(resourceName, SyncType.DELTA, data)

    private fun publish(
        resourceName: String,
        syncType: SyncType,
        data: List<FintResource>,
    ) {
        if (data.isEmpty()) {
            println("📤 Publish ${syncType.name} :: No data for $resourceName")
        }

        val chunks: List<List<FintResource>> = data.chunked(dynaProps.maxPageSize)
        val totalPages = chunks.size
        val totalSize = data.size.toLong()

        chunks.forEachIndexed { i, chunk ->
            val entries = factory.buildEntries(chunk)

            val meta =
                factory.buildMetadata(
                    resourceName = resourceName,
                    page = i.toLong(),
                    pageSize = entries.size.toLong(),
                    totalPages = totalPages.toLong(),
                    totalSize = totalSize,
                )

            val page = factory.buildPage(syncType, meta, entries)

            val (status, body) =
                when (syncType) {
                    SyncType.FULL -> {
                        sendFullSyncPage(resourceName, page).block()
                            ?: error("No response from provider")
                    }

                    SyncType.DELTA -> {
                        sendDeltaSyncPage(resourceName, page).block()
                            ?: error("No response from provider")
                    }

                    SyncType.DELETE -> {
                        error("SyncType.DELETE not implemented.")
                    }
                }

            println(
                "📤 ${syncType.name} $resourceName page ${i + 1}/$totalPages (${entries.size} entries) " +
                    "=> HTTP $status, body='${body.take(500)}'",
            )
        }
    }

    private fun sendFullSyncPage(
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

    private fun sendDeltaSyncPage(
        resourceName: String,
        page: SyncPage,
    ) = webClient
        .patch()
        .uri("${props.baseUrl}/provider/$resourceName")
        .bodyValue(page)
        .exchangeToMono { response ->
            response
                .bodyToMono(String::class.java)
                .defaultIfEmpty("")
                .map { body -> response.statusCode() to body }
        }
}
