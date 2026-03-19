package com.example.musicplayer.network

import android.util.Log
import com.example.musicplayer.domain.models.Song
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeSearchService @Inject constructor(
    private val client: OkHttpClient,
    private val extractor: YouTubeExtractor
) {
    private val TAG = "YouTubeSearch"

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        val pipedInstances = listOf(
            "https://pipedapi.kavin.rocks",
            "https://pipedapi.ducks.party",
            "https://api.piped.vicr123.com"
        )

        for (instance in pipedInstances) {
            try {
                val url = "$instance/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&filter=videos"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val json = JSONObject(body)
                    val items = json.getJSONArray("items")
                    val songs = mutableListOf<Song>()

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        if (item.optString("type") != "video") continue

                        val id = item.getString("url").substringAfterLast("v=")
                        val title = item.getString("title")
                        val artist = item.getString("uploaderName")
                        val image = item.getString("thumbnail")
                        
                        songs.add(Song("yt_$id", title, artist, image, ""))
                    }
                    if (songs.isNotEmpty()) return@withContext songs
                }
            } catch (e: Exception) {
                Log.e(TAG, "Search failed on $instance: ${e.message}")
            }
        }

        // Secondary Fallback: Invidious
        val invidiousInstances = listOf("https://iv.melmac.space", "https://invidious.namazso.eu")
        for (instance in invidiousInstances) {
            try {
                val url = "$instance/api/v1/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}&type=video"
                val request = Request.Builder().url(url).build()
                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) return@use
                    val body = response.body?.string() ?: return@use
                    val items = org.json.JSONArray(body)
                    val songs = mutableListOf<Song>()

                    for (i in 0 until items.length()) {
                        val item = items.getJSONObject(i)
                        val id = item.getString("videoId")
                        val title = item.getString("title")
                        val artist = item.getJSONObject("author").getString("author")
                        val image = "https://img.youtube.com/vi/$id/maxresdefault.jpg"
                        
                        songs.add(Song("yt_$id", title, artist, image, ""))
                    }
                    if (songs.isNotEmpty()) return@withContext songs
                }
            } catch (e: Exception) {
                 Log.e(TAG, "Search failed on $instance: ${e.message}")
            }
        }

        return@withContext emptyList<Song>()
    }
}
