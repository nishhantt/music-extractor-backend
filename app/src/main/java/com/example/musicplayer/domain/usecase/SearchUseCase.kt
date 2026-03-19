package com.example.musicplayer.domain.usecase

import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.domain.models.Song
import javax.inject.Inject

class SearchUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    suspend fun execute(query: String): List<Song> {
        return repository.searchSongs(query)
    }
}
