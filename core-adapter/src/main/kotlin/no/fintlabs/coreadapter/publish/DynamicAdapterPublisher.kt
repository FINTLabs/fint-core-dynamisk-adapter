package no.fintlabs.coreadapter.publish

import no.novari.fint.model.resource.FintResource
import no.fintlabs.adapter.models.AdapterCapability
import no.fintlabs.adapter.models.AdapterContract
import no.fintlabs.adapter.models.sync.SyncPage
import no.fintlabs.adapter.models.sync.SyncType
import no.fintlabs.coreadapter.config.AdapterProperties
import no.fintlabs.coreadapter.data.DynamicAdapterProperties
import no.fintlabs.coreadapter.data.ExpandedMetadata
import no.fintlabs.coreadapter.data.models.HeartBeatRequest
import no.fintlabs.coreadapter.store.ResourceStore
import no.fintlabs.coreadapter.store.TempDeltaSyncStore
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.UUID

@Component
class DynamicAdapterPublisher(
    private val webClient: WebClient,
    private val storage: ResourceStore,
    private val deltaStorage: TempDeltaSyncStore,
    private val factory: SyncPageFactory,
    private val props: AdapterProperties,
    private val dynaProps: DynamicAdapterProperties,
) {
    fun register(capabilities: MutableSet<AdapterCapability>): Boolean {
        println("Registering to provider...")
        if (!dynaProps.localLogicTest) {
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
        println("Booting Adapter in OFFLINE MODE")
        return true
    }

    fun giveHeartBeat() {
        if (!dynaProps.localLogicTest) {
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
    }

    fun performSync(
        metadataList: MutableList<ExpandedMetadata>,
        syncType: SyncType,
    ) {
        for (metadata in metadataList) {
            val data =
                if (syncType == SyncType.DELTA) {
                    deltaStorage.getAllResources(metadata.key)
                } else {
                    storage.getAllResources(metadata.key)
                }
            if (data.isNotEmpty()) {
                if (!dynaProps.localLogicTest) {
                    publish(metadata.key, syncType, data)
                } else {
                    logIfEnabled("FAKE_Sync: $syncType, ${metadata.key}, ${data.size} entries")
                }
                if (syncType == SyncType.DELTA) {
                    storage.addAllResources(metadata.key, data)
                    logIfEnabled("${metadata.key} added to FULL STORAGE from DELTA STORAGE")
                }
            } else {
                println("No data found in $syncType STORAGE for ${metadata.key}")
            }
        }
        if (syncType == SyncType.DELTA) {
            deltaStorage.purge(dynaProps.consoleLogging)
        }
    }

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
        val corrId = UUID.randomUUID().toString()

        chunks.forEachIndexed { i, chunk ->
            val entries = factory.buildEntries(chunk)

            val meta =
                factory.buildMetadata(
                    resourceName = resourceName,
                    page = i.toLong(),
                    pageSize = entries.size.toLong(),
                    totalPages = totalPages.toLong(),
                    totalSize = totalSize,
                    corrId = corrId,
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

    private fun logIfEnabled(log: String) {
        if (dynaProps.consoleLogging) println(log)
    }
}
