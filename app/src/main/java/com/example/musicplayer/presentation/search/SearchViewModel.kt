package com.example.musicplayer.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.data.MusicRepository
import com.example.musicplayer.domain.models.Song
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val musicRepository: MusicRepository
) : ViewModel() {

    private val _songs = MutableStateFlow<List<Song>>(emptyList())
    val songs: StateFlow<List<Song>> = _songs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    /**
     * Implementation of debounce to avoid rate limiting and excessive API calls.
     * As per engineering best practices.
     */
    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _songs.value = emptyList()
            _isLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            // Debounce for 300ms for snappier experience while respecting rate limits
            delay(300)
            _isLoading.value = true
            try {
                val results = musicRepository.searchSongs(query)
                _songs.value = results
            } catch (e: Exception) {
                _songs.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
