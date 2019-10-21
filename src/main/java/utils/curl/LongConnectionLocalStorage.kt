package utils.curl

import com.google.gson.GsonBuilder
import utils.localstorage.LocalStorage
import java.net.URL

class LongConnectionLocalStorage(val fileName: String) {
    val localStorage = LocalStorage(fileName = fileName)

    fun save(longConnection: LongConnection) {
        val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
        val json = gson.toJson(longConnection)
        localStorage.setItem(URL(longConnection.url).host,json)
    }

    fun load(url: String): LongConnection? {
        return try {
            val gson = GsonBuilder().excludeFieldsWithoutExposeAnnotation().create()
            val json = localStorage.getItem(URL(url).host)
            gson.fromJson(json, LongConnection::class.java)
        } catch (e: Exception) {
            null
        }
    }
}