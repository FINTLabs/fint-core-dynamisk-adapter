package no.fintlabs.adapter

import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.adapter.config.DynaAdapterProperties
import no.fintlabs.contract.data.ExpandedMetadata
import no.fintlabs.contract.models.HeartBeatRequest
import no.novari.fint.model.resource.FintResource
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val factory: SyncPageFactory,
    private val props: DynaAdapterProperties,
) {
    fun register(capabilities: MutableSet<AdapterCapability>): Boolean {
        println("Registering to provider...")

        val missingCapabilities = false

        val adapterCapabilities: MutableSet<AdapterCapability> =
            if (missingCapabilities) {
                val list = capabilities.toMutableList()
                list.removeAt(3)
                list.removeAt(2)
                list.toMutableSet()
            } else capabilities

        val contract =
            AdapterContract
                .builder()
                .adapterId(props.adapterId)
                .orgId(props.orgId)
                .username(props.username)
                .heartbeatIntervalInMinutes(props.heartbeatIntervalInMinutes)
                .capabilities(adapterCapabilities)
                .time(0L)
                .build()

        val response =
            webClient
                .post()
                .uri("${props.baseUrl}/provider/register")
                .bodyValue(contract)
                .exchangeToMono { response ->
                    response
                        .bodyToMono<String>()
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

    fun performSync(
        metadataList: MutableList<ExpandedMetadata>,
        syncType: SyncType,
        maxPageSize: Int,
    ) {
        for (metadata in metadataList) {
            val data =
                if (syncType == SyncType.DELTA) {
                    deltaStorage.getAllResources(metadata.key)
                } else {
                    storage.getAllResources(metadata.key)
                }
            if (data.isNotEmpty()) {
                publish(metadata.key, metadata, syncType, maxPageSize, data)
            } else {
                println("FAKE_Sync: $syncType, ${metadata.key}, ${data.size} entries")
            }
            if (syncType == SyncType.DELTA) {
                storage.addAllResources(metadata.key, metadata, data)
                println("${metadata.key} added to FULL STORAGE from DELTA STORAGE")
            }
        }
        if (syncType == SyncType.DELTA) {
            deltaStorage.purge()
        }
    }

    private fun publish(
        resourceName: String,
        metadata: ExpandedMetadata,
        syncType: SyncType,
        maxPageSize: Int,
        data: List<FintResource>,
    ) {
        if (data.isEmpty()) {
            println("📤 Publish ${syncType.name} :: No data for $resourceName")
        }

        val chunks: List<List<FintResource>> = data.chunked(maxPageSize)
        val totalPages = chunks.size
        val totalSize = data.size.toLong()
        val corrId = UUID.randomUUID().toString()

        chunks.forEachIndexed { i, chunk ->
            val entries = factory.buildEntries(chunk, metadata)

            val meta =
                factory.buildMetadata(
                    resourceName = resourceName,
                    page = i.toLong(),
                    pageSize = entries.size.toLong(),
                    totalPages = totalPages.toLong(),
                    totalSize = totalSize,
                    corrId = corrId,
                    adapterId = props.adapterId,
                    orgId = props.orgId,
                )

            val page = factory.buildPage(syncType, meta, entries)

            val (status) =
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
                "📤 ${syncType.name}: HTTP $status, $resourceName page ${i + 1}/$totalPages (${entries.size} entries) ",
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
                .bodyToMono<String>()
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
                .bodyToMono<String>()
                .defaultIfEmpty("")
                .map { body -> response.statusCode() to body }
        }
}