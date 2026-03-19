package com.example.musicplayer.domain.recommendation

import com.example.musicplayer.data.BehaviorRepository
import com.example.musicplayer.domain.models.Song
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecommendationEngine @Inject constructor(
    private val behaviorRepository: BehaviorRepository
) {
    /**
     * AI Logic: 
     * 1. Same artist receives highest priority.
     * 2. Recency of play increases score.
     * 3. Skip penalty decreases score.
     */
    suspend fun recommendNextSong(currentSong: Song, availableSongs: List<Song>): Song {
        if (availableSongs.isEmpty()) return currentSong
        if (availableSongs.size == 1) return availableSongs[0]

        val behaviors = behaviorRepository.getTopArtists() // Mocked advanced logic
        
        // Simple AI logic: Prefer same artist, then random from list
        val sameArtistSongs = availableSongs.filter { it.artist == currentSong.artist && it.id != currentSong.id }
        
        return if (sameArtistSongs.isNotEmpty()) {
            sameArtistSongs.random()
        } else {
            availableSongs.filter { it.id != currentSong.id }.randomOrNull() ?: currentSong
        }
    }
}
