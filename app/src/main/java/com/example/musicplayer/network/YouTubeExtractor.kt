package com.example.musicplayer.network

import com.example.musicplayer.di.NetworkModule
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YouTubeExtractor @Inject constructor(
    private val client: OkHttpClient
) {
    /**
     * Now simply returns the backend proxy URL.
     * The backend handles all the heavy lifting (yt-dlp, proxying).
     */
    suspend fun extractStreamUrl(videoId: String): String {
        return "${NetworkModule.BACKEND_URL}/audio?videoId=$videoId"
    }
}
