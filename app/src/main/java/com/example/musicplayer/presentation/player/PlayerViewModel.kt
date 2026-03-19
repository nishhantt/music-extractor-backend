package com.example.musicplayer.presentation.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.example.musicplayer.domain.models.Song
import com.example.musicplayer.player.ExoPlayerManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val exoPlayerManager: ExoPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val player: Player = exoPlayerManager.asPlayer()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                _isPlaying.value = isPlayingNow
            }
        })

        viewModelScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                }
                delay(1000)
            }
        }
    }

    fun playSong(song: Song) {
        if (song.audioUrl.isBlank()) {
            Log.e("PlayerViewModel", "Audio URL is empty for song: ${song.title}")
            _uiState.value = PlayerUiState.Error("Invalid audio URL")
            return
        }

        try {
            Log.d("Player", "Playing Saavn stream: ${song.audioUrl}")
            exoPlayerManager.playSong(song)
            _uiState.value = PlayerUiState.Playing(song)
        } catch (e: Exception) {
            Log.e("PlayerViewModel", "Playback error", e)
            _uiState.value = PlayerUiState.Error("Playback failed")
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) exoPlayerManager.pause() else exoPlayerManager.play()
    }

    fun next() {
        // Implement queue logic if needed
    }

    fun previous() {
        // Implement queue logic if needed
    }

    fun seekTo(ms: Long) {
        exoPlayerManager.seekTo(ms)
    }

    fun getDuration(): Long = player.duration.coerceAtLeast(0)
}

sealed interface PlayerUiState {
    object Idle : PlayerUiState
    object Loading : PlayerUiState
    data class Playing(val song: Song) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}
