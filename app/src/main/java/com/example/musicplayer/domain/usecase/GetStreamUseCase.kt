package com.example.musicplayer.domain.usecase

import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.domain.models.Song
import javax.inject.Inject

class GetStreamUseCase @Inject constructor(
    private val repository: MusicRepository
) {
    // This usecase is no longer needed for JioSaavn architecture as SaavnService returns the stream URL directly.
    // Deprecating it or repurposing it if needed.
}
