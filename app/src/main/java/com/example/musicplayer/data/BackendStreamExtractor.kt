package com.example.musicplayer.data

import com.example.musicplayer.domain.models.PlayableMedia
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DEPRECATED: This class is no longer used as the app has migrated to local extraction.
 */
@Singleton
class BackendStreamExtractor @Inject constructor() : StreamExtractor {
    override suspend fun getPlayableMedia(videoId: String): PlayableMedia {
        throw UnsupportedOperationException("BackendStreamExtractor is deprecated")
    }
}
