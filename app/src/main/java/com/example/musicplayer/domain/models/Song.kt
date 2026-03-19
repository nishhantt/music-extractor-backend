package com.example.musicplayer.domain.models

data class Song(
    val id: String,
    val title: String,
    val artist: String,
    val image: String,
    val audioUrl: String
)

/**
 * Migration helper to bridge old PlayableMedia with new Song model if needed
 */
fun Song.toPlayableMedia() = PlayableMedia(
    uriString = audioUrl,
    mimeType = "audio/mpeg",
    title = title,
    thumbnailUrl = image
)
