package com.example.musicplayer.data

import com.example.musicplayer.domain.models.Song
import com.example.musicplayer.network.SaavnService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MusicRepository @Inject constructor(
    private val saavnService: SaavnService,
    private val localMusicRepository: LocalMusicRepository
) {
    suspend fun globalSearch(query: String): com.example.musicplayer.domain.models.SearchResult = withContext(Dispatchers.IO) {
        if (query == "local_files") {
            val localSongs = localMusicRepository.getLocalSongs()
            return@withContext com.example.musicplayer.domain.models.SearchResult(
                songs = localSongs,
                topResult = localSongs.firstOrNull()
            )
        }

        saavnService.globalSearch(query)
    }

    suspend fun searchSongs(query: String): List<Song> = withContext(Dispatchers.IO) {
        saavnService.searchSongs(query)
    }

    suspend fun getAlbumSongs(albumId: String) = saavnService.getAlbumDetails(albumId)
    suspend fun getArtistSongs(artistId: String) = saavnService.getArtistSongs(artistId)
    suspend fun getSongDetails(songId: String): Song? {
        return if (songId.startsWith("local_")) {
            null // Local songs already have path in audioUrl
        } else {
            saavnService.getSongDetails(songId)
        }
    }
}
