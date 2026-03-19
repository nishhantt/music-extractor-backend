package com.example.musicplayer.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.domain.models.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun loadAlbum(albumId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = musicRepository.getAlbumSongs(albumId)
            _isLoading.value = false
        }
    }

    fun loadArtist(artistId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _songs.value = musicRepository.getArtistSongs(artistId)
            _isLoading.value = false
        }
    }
}
