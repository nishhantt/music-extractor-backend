package com.example.musicplayer.network

import android.util.Log
import com.example.musicplayer.domain.models.Song
import com.example.musicplayer.di.NetworkModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeSearchService @Inject constructor(
    private val client: OkHttpClient
) {
    private val TAG = "YouTubeSearch"

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        try {
            val url = "${NetworkModule.BACKEND_URL}/search?q=${java.net.URLEncoder.encode(query, "UTF-8")}"
            val request = Request.Builder().url(url).build()
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext emptyList()
                val body = response.body?.string() ?: return@withContext emptyList()
                val json = JSONObject(body)
                val items = json.getJSONArray("songs")
                val songs = mutableListOf<Song>()

                for (i in 0 until items.length()) {
                    val item = items.getJSONObject(i)
                    songs.add(Song(
                        id = item.getString("id"),
                        title = item.getString("title"),
                        artist = item.getString("artist"),
                        image = item.getString("image"),
                        audioUrl = "" // Extractor will return the stream url
                    ))
                }
                songs
            }
        } catch (e: Exception) {
            Log.e(TAG, "Search failed: ${e.message}")
            emptyList()
        }
    }
}
