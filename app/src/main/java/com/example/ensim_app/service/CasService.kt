package com.example.ensim_app.service

import android.util.Log
import android.util.Xml
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.xmlpull.v1.XmlPullParser
import java.io.IOException
import java.io.StringReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class CasIdentity(
    val username: String,
    val attributes: Map<String, String>,
) {
    val displayName: String? get() = attributes["displayName"]
    val mail: String? get() = attributes["mail"]
    val givenName: String? get() = attributes["givenName"]
    val surname: String? get() = attributes["Sn"] ?: attributes["sn"] ?: attributes["surname"]

    val bestLabel: String
        get() = displayName
            ?: listOfNotNull(givenName, surname).joinToString(" ").ifBlank { null }
            ?: username
}

object CasService {

    const val CAS_BASE = "https://cas.univ-lemans.fr/cas"
    const val SERVICE_URL = "https://planning.univ-lemans.fr/direct/myplanning.jsp"

    fun loginUrl(): String {
        val encoded = URLEncoder.encode(SERVICE_URL, "UTF-8")
        return "$CAS_BASE/login?service=$encoded"
    }

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build()
    }

    suspend fun validateTicket(ticket: String): CasIdentity = withContext(Dispatchers.IO) {
        val encodedService = URLEncoder.encode(SERVICE_URL, "UTF-8")
        val url = "$CAS_BASE/serviceValidate?service=$encodedService&ticket=$ticket"
        val request = Request.Builder().url(url).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("CAS validate HTTP ${response.code}")
            }
            val body = response.body?.string() ?: throw IOException("CAS validate: empty body")
            Log.d("ENSIM-CAS", "validateTicket XML:\n$body")
            parseCasResponse(body) ?: throw IOException("CAS authentication failed")
        }
    }

    private fun parseCasResponse(xml: String): CasIdentity? {
        val parser = Xml.newPullParser()
        parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
        parser.setInput(StringReader(xml))

        var event = parser.eventType
        var inSuccess = false
        var inAttributes = false
        var username: String? = null
        val attributes = mutableMapOf<String, String>()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    val name = parser.name.removeCasPrefix()
                    when {
                        name == "authenticationSuccess" -> inSuccess = true
                        name == "authenticationFailure" -> return null
                        name == "user" && inSuccess -> username = parser.nextText().trim()
                        name == "attributes" && inSuccess -> inAttributes = true
                        inAttributes -> attributes[name] = parser.nextText().trim()
                    }
                }
                XmlPullParser.END_TAG -> {
                    val name = parser.name.removeCasPrefix()
                    if (name == "attributes") inAttributes = false
                }
            }
            event = parser.next()
        }
        return username?.let { CasIdentity(username = it, attributes = attributes) }
    }

    private fun String.removeCasPrefix(): String = substringAfter(":", this)
}
