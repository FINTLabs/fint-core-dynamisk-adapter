package no.fintlabs.dynamiskadapter.util

import com.google.gson.Gson

fun safeSerialize(obj: Any, gson: Gson): String =
    when (obj) {
        is Map<*, *> -> gson.toJson(obj)
        else -> {
            val allFields =
                obj.javaClass.declaredFields +
                        obj.javaClass.superclass
                            ?.declaredFields
                            .orEmpty()

            val unique =
                allFields
                    .filter { it.name != "writeable" }
                    .associate { field ->
                        field.isAccessible = true
                        field.name to field.get(obj)
                    }
            gson.toJson(unique)
        }
    }