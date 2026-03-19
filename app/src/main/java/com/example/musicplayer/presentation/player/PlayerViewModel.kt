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
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: android.content.Context,
    private val exoPlayerManager: ExoPlayerManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<PlayerUiState>(PlayerUiState.Idle)
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _currentPosition = MutableStateFlow(0L)
    val currentPosition: StateFlow<Long> = _currentPosition.asStateFlow()

    private val _duration = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration.asStateFlow()

    private val _playlist = MutableStateFlow<List<Song>>(emptyList())
    val playlist: StateFlow<List<Song>> = _playlist.asStateFlow()

    private var currentIndex = -1

    private val player: Player = exoPlayerManager.asPlayer()

    init {
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlayingNow: Boolean) {
                _isPlaying.value = isPlayingNow
                if (isPlayingNow) {
                    _duration.value = player.duration.coerceAtLeast(0)
                }
            }

            override fun onPlaybackStateChanged(state: Int) {
                if (state == Player.STATE_READY) {
                    _duration.value = player.duration.coerceAtLeast(0)
                } else if (state == Player.STATE_ENDED) {
                    next()
                }
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Update current song state when transitioning (e.g. via Next/Prev)
                val currentSong = _playlist.value.getOrNull(player.currentMediaItemIndex)
                if (currentSong != null) {
                    currentIndex = player.currentMediaItemIndex
                    _uiState.value = PlayerUiState.Playing(currentSong)
                }
            }
        })

        viewModelScope.launch {
            while (isActive) {
                if (player.isPlaying) {
                    _currentPosition.value = player.currentPosition
                    // Occasionally duration is -1 until some data is buffered
                    if (_duration.value <= 0) {
                        _duration.value = player.duration.coerceAtLeast(0)
                    }
                }
                delay(1000)
            }
        }
    }

    fun playSong(song: Song, songs: List<Song> = emptyList()) {
        if (song.audioUrl.isBlank()) {
            Log.e("PlayerViewModel", "Audio URL is empty for song: ${song.title}")
            _uiState.value = PlayerUiState.Error("Invalid audio URL")
            return
        }

        try {
            // If a list was provided (e.g. from search results), use it as the playlist
            val listToUse = if (songs.isNotEmpty()) songs else listOf(song)
            _playlist.value = listToUse
            currentIndex = listToUse.indexOf(song).coerceAtLeast(0)

            Log.d("Player", "Playing song: ${song.title} from playlist of size ${listToUse.size}")
            
            // We use the ExoPlayer's internal playlist for Next/Prev support
            val mediaItems = listToUse.map { s ->
                MediaItem.Builder()
                    .setUri(s.audioUrl)
                    .setMediaMetadata(
                        androidx.media3.common.MediaMetadata.Builder()
                            .setTitle(s.title)
                            .setArtist(s.artist)
                            .setArtworkUri(android.net.Uri.parse(s.image))
                            .build()
                    )
                    .build()
            }
            
            // Start the foreground service
            val intent = android.content.Intent(context, com.example.musicplayer.player.MusicPlayerService::class.java)
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }

            player.setMediaItems(mediaItems, currentIndex, 0L)
            player.prepare()
            player.play()
            
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
        if (player.hasNextMediaItem()) {
            player.seekToNextMediaItem()
        } else {
            // Optional: Loop back to start if at the end
            if (_playlist.value.isNotEmpty()) {
                player.seekTo(0, 0)
                player.play()
            }
        }
    }

    fun previous() {
        if (player.hasPreviousMediaItem()) {
            player.seekToPreviousMediaItem()
        } else {
            player.seekTo(0)
        }
    }

    fun seekTo(ms: Long) {
        player.seekTo(ms)
        _currentPosition.value = ms
    }

    fun getDuration(): Long = _duration.value
}

sealed interface PlayerUiState {
    object Idle : PlayerUiState
    object Loading : PlayerUiState
    data class Playing(val song: Song) : PlayerUiState
    data class Error(val message: String) : PlayerUiState
}
