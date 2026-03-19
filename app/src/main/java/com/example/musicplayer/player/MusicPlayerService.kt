package com.example.musicplayer.player

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import androidx.lifecycle.LifecycleService
import androidx.media3.session.MediaSession
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import android.app.PendingIntent
import android.content.pm.ServiceInfo
import android.os.Build
import androidx.core.app.ServiceCompat

@AndroidEntryPoint
class MusicPlayerService : LifecycleService() {

    companion object {
        const val ACTION_TOGGLE_PLAY = "com.example.musicplayer.action.TOGGLE_PLAY"
        const val ACTION_NEXT = "com.example.musicplayer.action.NEXT"
        const val ACTION_PREV = "com.example.musicplayer.action.PREV"
        const val ACTION_STOP = "com.example.musicplayer.action.STOP"
    }

    @Inject
    lateinit var exoPlayerManager: ExoPlayerManager

    private val binder = LocalBinder()
    private var mediaSession: MediaSession? = null

    inner class LocalBinder : Binder() {
        fun getService(): MusicPlayerService = this@MusicPlayerService
    }

    override fun onBind(intent: Intent): IBinder? {
        super.onBind(intent)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val player = exoPlayerManager.asPlayer()
        
        if (mediaSession == null) {
            val mainActivityIntent = Intent(this, com.example.musicplayer.MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this, 0, mainActivityIntent, PendingIntent.FLAG_IMMUTABLE)
            mediaSession = MediaSession.Builder(this, player)
                .setSessionActivity(pendingIntent)
                .build()
        }

        val nm = MediaNotificationManager(this)
        val notification = nm.createNotification("Music Player")
        
        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        } else {
            0
        }
        
        ServiceCompat.startForeground(this, 1, notification, type)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        exoPlayerManager.release()
    }
}
