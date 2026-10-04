package com.tanzirdev.modmase

import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object Api {
    const val DB = "https://modmase-default-rtdb.firebaseio.com"
    const val GITHUB_LATEST = "https://api.github.com/repos/TanzirEren/MODMASE/releases/latest"

    fun getSync(url: String): String? {
        return try {
            val c = URL(url).openConnection() as HttpURLConnection
            c.connectTimeout = 10000
            c.readTimeout = 20000
            c.requestMethod = "GET"
            c.setRequestProperty("Accept", "application/json")
            if (c.responseCode in 200..299) {
                c.inputStream.bufferedReader().use { it.readText() }
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun get(path: String, query: String = ""): String? =
        withContext(Dispatchers.IO) { getSync("$DB/$path.json$query") }

    /** method: "PUT" or "POST" */
    suspend fun send(method: String, path: String, body: String): String? =
        withContext(Dispatchers.IO) {
            try {
                val c = URL("$DB/$path.json").openConnection() as HttpURLConnection
                c.connectTimeout = 10000
                c.readTimeout = 20000
                c.requestMethod = method
                c.doOutput = true
                c.setRequestProperty("Content-Type", "application/json")
                c.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
                if (c.responseCode in 200..299) {
                    c.inputStream.bufferedReader().use { it.readText() }
                } else {
                    null
                }
            } catch (e: Exception) {
                null
            }
        }
}
