package com.raqeeb.app.data

import com.raqeeb.app.domain.model.AdminBroadcast
import com.raqeeb.app.domain.model.IslamicReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL

object SupabaseSyncClient {
    const val SUPABASE_URL = "https://nirnxfisfdmlkhszkirb.supabase.co"
    const val SUPABASE_KEY = "sb_publishable_bQd9POM_FPi9QnR3LtBaow_cv1xWtDt"

    private fun openGetConnection(endpoint: String): HttpURLConnection {
        val url = URL("$SUPABASE_URL/rest/v1/$endpoint")
        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("apikey", SUPABASE_KEY)
            setRequestProperty("Accept", "application/json")
            connectTimeout = 8000
            readTimeout = 8000
        }
    }

    suspend fun fetchLatestAdminMessage(): AdminBroadcast? = withContext(Dispatchers.IO) {
        try {
            val conn = openGetConnection("admin_messages?select=id,title_ar,body_ar,created_at&order=id.desc&limit=1")
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().readText()
                val arr = JSONArray(body)
                if (arr.length() > 0) {
                    val obj = arr.getJSONObject(0)
                    return@withContext AdminBroadcast(
                        id = obj.optLong("id", 0L),
                        title = obj.optString("title_ar", "رسالة من إدارة رقيب 🌿"),
                        body = obj.optString("body_ar", ""),
                        timestamp = obj.optString("created_at", "").take(10)
                    )
                }
            }
            null
        } catch (_: Exception) {
            null
        }
    }

    suspend fun fetchPublishedReminders(): List<IslamicReminder> = withContext(Dispatchers.IO) {
        try {
            val conn = openGetConnection("islamic_reminders?select=text_ar,text_en,source_ar,source_en&is_published=eq.true&order=id.desc&limit=20")
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().readText()
                val arr = JSONArray(body)
                val list = mutableListOf<IslamicReminder>()
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    list.add(
                        IslamicReminder(
                            textAr = obj.optString("text_ar"),
                            textEn = obj.optString("text_en").ifBlank { obj.optString("text_ar") },
                            sourceAr = obj.optString("source_ar"),
                            sourceEn = obj.optString("source_en").ifBlank { obj.optString("source_ar") }
                        )
                    )
                }
                return@withContext list
            }
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    suspend fun fetchRemoteBlockedDomains(): List<String> = withContext(Dispatchers.IO) {
        try {
            val conn = openGetConnection("blocked_domains?select=domain&is_active=eq.true&limit=500")
            if (conn.responseCode in 200..299) {
                val body = conn.inputStream.bufferedReader().readText()
                val arr = JSONArray(body)
                val domains = mutableListOf<String>()
                for (i in 0 until arr.length()) {
                    val d = arr.getJSONObject(i).optString("domain").trim().lowercase()
                    if (d.isNotBlank()) domains.add(d)
                }
                return@withContext domains
            }
            emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }
}
