package com.example.musicplayer.data

import com.example.musicplayer.domain.models.Song
import com.example.musicplayer.network.SaavnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val saavnService: SaavnService
) {
    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        saavnService.searchSongs(query)
    }
}
