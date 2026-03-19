package com.example.musicplayer.domain.models

data class SearchResult(
    val topResult: Song? = null,
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList()
)

data class Album(
    val id: String,
    val title: String,
    val artist: String,
    val image: String,
    val url: String = "",
    val year: String = ""
)

data class Artist(
    val id: String,
    val name: String,
    val image: String,
    val url: String = "",
    val description: String = ""
)
