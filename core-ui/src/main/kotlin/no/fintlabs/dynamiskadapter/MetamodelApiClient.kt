package no.fintlabs.dynamiskadapter

import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

// For core-UI to communicate with core-API
class MetamodelApiClient(
    private val baseUrl: String = "http://localhost:8182/api",
) : MetamodelApi {
    private val client = HttpClient.newHttpClient()

    override suspend fun ping(): String {
        val response = client.send(
            HttpRequest.newBuilder(URI("$baseUrl/ping")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        )
        return response.body()
    }


    override suspend fun getAllComponents(): List<String> {
        val response = client.send(
            HttpRequest.newBuilder(URI("$baseUrl/getAllComponents")).GET().build(),
            HttpResponse.BodyHandlers.ofString()
        )
        return responseToStringList(response.body())
    }

    override suspend fun getResources(component: String): List<String> {
        val response = client.send(
            HttpRequest.newBuilder(URI("$baseUrl/getResources?component=${component}"))
                .GET()
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )

        return responseToStringList(response.body())
    }

    override suspend fun createResources(
        component: String,
        resource: String,
        count: Int
    ): String {
        val uri = URI("$baseUrl/create?component=${component}&resource=${resource}&count=${count}")

        val response = client.send(
            HttpRequest.newBuilder(uri)
                .POST(HttpRequest.BodyPublishers.noBody())
                .build(),
            HttpResponse.BodyHandlers.ofString()
        )
        return response.body()
    }

    private fun responseToStringList(json: String): List<String> =
        json
            .removePrefix("[")
            .removeSuffix("]")
            .split(",")
            .map { it.trim().removeSurrounding("\"") }
            .filter { it.isNotEmpty() }
}