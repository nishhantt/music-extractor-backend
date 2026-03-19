package com.example.musicplayer.domain.usecase

import com.example.musicplayer.player.ExoPlayerManager
import javax.inject.Inject

class ControlPlaybackUseCases @Inject constructor(
    private val exoPlayerManager: ExoPlayerManager
) {
    fun play() = exoPlayerManager.play()
    fun pause() = exoPlayerManager.pause()
    // Simplified for this refactor
    fun next() { /* Handle next */ }
    fun previous() { /* Handle previous */ }
    fun seekTo(ms: Long) = exoPlayerManager.seekTo(ms)
}
