package com.example.musicplayer.player

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.datasource.HttpDataSource
import com.example.musicplayer.domain.models.Song
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExoPlayerManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val httpDataSourceFactory: HttpDataSource.Factory by lazy {
        DefaultHttpDataSource.Factory()
            .setUserAgent("Skibidi/0.1 (Android)")
            .setAllowCrossProtocolRedirects(true)
    }

    private val player: ExoPlayer by lazy {
        ExoPlayer.Builder(context)
            .setMediaSourceFactory(
                androidx.media3.exoplayer.source.DefaultMediaSourceFactory(context)
                    .setDataSourceFactory(httpDataSourceFactory)
            )
            .build()
    }

    fun asPlayer(): ExoPlayer = player

    fun play() { player.play() }
    fun pause() { player.pause() }
    fun seekTo(positionMs: Long) { player.seekTo(positionMs) }
    fun release() { player.release() }
}
