package no.fintlabs.dynamiskadapter.util

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonParser

fun parseFintResources(rawJson: String, gson: Gson): String {
    val root: JsonElement = JsonParser.parseString(rawJson)

    val jsonArray = when {
        root.isJsonArray -> root.asJsonArray
        root.isJsonObject -> JsonArray().apply { add(root) }
        else -> throw IllegalArgumentException("Unexpected JSON structure: $rawJson")
    }
    
    val list = jsonArray.map { jsonElement ->
        gson.fromJson(jsonElement, Any::class.java)
    }
    return formatFintResources(list, gson)
}

private fun formatFintResources(objects: List<Any>, gson: Gson): String {
    return objects.joinToString("\n") { obj ->
        safeSerialize(obj, gson)
    }
}
