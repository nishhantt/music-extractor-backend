package com.example.musicplayer.data

import com.example.musicplayer.data.local.BehaviorDao
import com.example.musicplayer.data.local.BehaviorEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BehaviorRepository @Inject constructor(
    private val behaviorDao: BehaviorDao
) {
    suspend fun trackPlay(songId: String, artist: String, album: String) {
        behaviorDao.insertBehavior(BehaviorEntity(songId = songId, action = "PLAY", artistName = artist, albumName = album))
    }

    suspend fun trackSkip(songId: String) {
        behaviorDao.insertBehavior(BehaviorEntity(songId = songId, action = "SKIP"))
    }

    suspend fun trackSearch(query: String) {
        behaviorDao.insertBehavior(BehaviorEntity(songId = "N/A", action = "SEARCH", artistName = query))
    }

    fun getAllBehaviors() = behaviorDao.getAllBehaviors()
    
    suspend fun getTopArtists() = behaviorDao.getTopArtists()
    suspend fun getMostPlayedSongs() = behaviorDao.getMostPlayedSongs()
}
