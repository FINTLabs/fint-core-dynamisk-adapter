package no.fintlabs.dynamiskadapter

interface MetamodelApi {
    suspend fun ping(): String
    suspend fun getAllComponents(): List<String>
    suspend fun getResources(component: String): List<String>
    suspend fun createResources(component: String, resource: String, count: Int): String
}