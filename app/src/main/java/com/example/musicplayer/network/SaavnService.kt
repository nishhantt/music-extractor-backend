package com.example.musicplayer.network

import android.util.Log
import com.example.musicplayer.BuildConfig
import com.example.musicplayer.domain.models.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder

class SaavnService(private val client: OkHttpClient) {

    /**
     * Brainstormed Fix: 
     * 1. Public APIs like saavn.dev block mobile apps via Cloudflare (Returning HTML instead of JSON).
     * 2. The solution is to proxy the request through YOUR OWN backend (Render).
     * 3. This implementation tries the backend proxy first, then falls back to a direct call with fixed headers.
     */
    suspend fun searchSongs(query: String): List<Song> {
        return withContext(Dispatchers.IO) {
            val cleanQuery = query.trim().replace("\n", "")
            if (cleanQuery.isEmpty()) return@withContext emptyList()

            val sanitizedQuery = URLEncoder.encode(cleanQuery, "UTF-8")
            
            // Try Backend Proxy First (Recommended Approach)
            val backendBase = BuildConfig.EXTRACTOR_BACKEND_URL.trimEnd('/')
            if (backendBase.isNotBlank() && backendBase.contains("http")) {
                val backendUrl = "$backendBase/saavn/search?query=$sanitizedQuery"
                Log.d("SaavnAPI", "Trying backend proxy: $backendUrl")
                val results = fetchAndParse(backendUrl, isDirect = false)
                if (results.isNotEmpty()) return@withContext results
            }

            // Fallback: Direct Call with Working API (saavn.sumit.co)
            val directUrl = "https://saavn.sumit.co/api/search/songs?query=$sanitizedQuery"
            Log.d("SaavnAPI", "Direct call to working API: $directUrl")
            fetchAndParse(directUrl, isDirect = true)
        }
    }

    private fun fetchAndParse(url: String, isDirect: Boolean): List<Song> {
        val requestBuilder = Request.Builder().url(url)
        
        if (isDirect) {
            requestBuilder.addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/121.0.0.0 Safari/537.36")
            requestBuilder.addHeader("Accept", "application/json")
        }

        return try {
            val response = client.newCall(requestBuilder.build()).execute()
            val body = response.body?.string() ?: return emptyList()

            if (body.trim().startsWith("<")) {
                Log.e("SaavnAPI", "Blocked by Cloudflare/Bot detection (HTML returned)")
                return emptyList()
            }

            val json = JSONObject(body)
            // Handle new structure: { success: true, data: { results: [...] } }
            val data = json.optJSONObject("data") ?: json
            val results = data.optJSONArray("results") ?: json.optJSONArray("data") ?: return emptyList()

            val songs = mutableListOf<Song>()
            for (i in 0 until results.length()) {
                val item = results.getJSONObject(i)

                // Safe extraction of 320kbps URL
                val downloadUrls = item.getJSONArray("downloadUrl")
                val audioUrl = if (downloadUrls.length() > 4) {
                    downloadUrls.getJSONObject(4).getString("url")
                } else {
                    downloadUrls.getJSONObject(downloadUrls.length() - 1).getString("url")
                }

                // Safe extraction of Image
                val images = item.getJSONArray("image")
                val image = if (images.length() > 2) {
                    images.getJSONObject(2).getString("url")
                } else {
                    images.getJSONObject(images.length() - 1).getString("url")
                }

                // Extract artist name from artists.primary[0].name
                var artist = "Unknown Artist"
                try {
                    val artistsObj = item.optJSONObject("artists")
                    if (artistsObj != null) {
                        val primaryArtists = artistsObj.optJSONArray("primary")
                        if (primaryArtists != null && primaryArtists.length() > 0) {
                            artist = primaryArtists.getJSONObject(0).getString("name")
                        }
                    } else {
                        // Fallback for older API versions
                        artist = item.optString("primaryArtists", "Unknown Artist")
                    }
                } catch (e: Exception) {
                    Log.w("SaavnAPI", "Could not parse artist for ${item.optString("name")}")
                }

                songs.add(
                    Song(
                        id = item.getString("id"),
                        title = item.getString("name"),
                        artist = artist,
                        image = image,
                        audioUrl = audioUrl
                    )
                )
            }
            songs
        } catch (e: Exception) {
            Log.e("SaavnAPI", "Request failed for $url", e)
            emptyList()
        }
    }
}
