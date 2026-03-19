package com.example.musicplayer.network

import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeExtractor @Inject constructor(
    private val client: OkHttpClient
) {
    private val TAG = "YouTubeExtractor"

    suspend fun extractStreamUrl(videoId: String): String = withContext(Dispatchers.IO) {
        val pipedInstances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.ducks.party",
            "https://api.piped.vicr123.com",
            "https://pipedapi.moomoo.me"
        )

        for (instance in pipedInstances) {
            try {
                val url = "$instance/streams/$videoId"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val audioStreams = json.optJSONArray("audioStreams")
                    if (audioStreams != null && audioStreams.length() > 0) {
                        var bestUrl = ""
                        var bestScore = -1
                        for (i in 0 until audioStreams.length()) {
                            val stream = audioStreams.getJSONObject(i)
                            val bitrate = stream.optInt("bitrate", 128000)
                            val codec = stream.optString("codec", "").lowercase()
                            var currentScore = bitrate / 1000
                            if (codec.contains("mp4a")) currentScore += 50
                            if (currentScore > bestScore) {
                                bestScore = currentScore
                                bestUrl = stream.getString("url")
                            }
                        }
                        return@withContext bestUrl
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Piped failed: ${e.message}")
            }
        }

        // Secondary Layer: Invidious
        val invidiousInstances = listOf("https://iv.melmac.space", "https://invidious.namazso.eu")
        for (instance in invidiousInstances) {
            try {
                val url = "$instance/api/v1/videos/$videoId"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val adaptiveFormats = json.optJSONArray("adaptiveFormats")
                    if (adaptiveFormats != null) {
                        for (i in 0 until adaptiveFormats.length()) {
                            val format = adaptiveFormats.getJSONObject(i)
                            if (format.optString("type").contains("audio")) {
                                return@withContext format.getString("url")
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Invidious failed: ${e.message}")
            }
        }

        // Final fallback: Direct Proxy
        "https://youtube-to-mp3-proxy.terasp.net/api/stream?id=$videoId"
    }
}
