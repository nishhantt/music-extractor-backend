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

    private val _searchResults = MutableStateFlow(com.example.musicplayer.domain.models.SearchResult())
    val searchResults: StateFlow<com.example.musicplayer.domain.models.SearchResult> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun onQueryChanged(query: String) {
        searchJob?.cancel()
        if (query.isBlank()) {
            _searchResults.value = com.example.musicplayer.domain.models.SearchResult()
            _isLoading.value = false
            return
        }

        searchJob = viewModelScope.launch {
            delay(500)
            _isLoading.value = true
            try {
                _searchResults.value = musicRepository.globalSearch(query)
            } catch (e: Exception) {
                _searchResults.value = com.example.musicplayer.domain.models.SearchResult()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
