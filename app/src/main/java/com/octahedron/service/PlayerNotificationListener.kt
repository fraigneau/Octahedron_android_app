package com.octahedron.service

import android.content.ComponentName
import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContextCompat

class PlayerNotificationListener: NotificationListenerService() {

    companion object {
        private const val TAG = "PlayerNotificationListener"
        private const val PKG_SPOTIFY = "com.spotify.music"
        private const val PKG_YOUTUBE_MUSIC = "com.google.android.apps.youtube.music"
        private const val PKG_DEEZER = "deezer.android.app"
    }
    private var lastTrackSignature: String? = null

    enum class MusicPlatform { Spotify, Deezer, YoutubeMusic, }
    // TODO : Make this configurable from DataStore settings
    public var currentPlatform: MusicPlatform? = MusicPlatform.Spotify // just for testing


    override fun onListenerConnected() {
        Log.i(TAG, "Notification listener started")

        val intent = Intent(this, BlePacketManager::class.java)
        ContextCompat.startForegroundService(this, intent)
        startService(intent)

        activeNotifications?.forEach { onNotificationPosted(it) }
    }

    override fun onListenerDisconnected() {
        Log.i(TAG, "Notification listener stopped" )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

        val msm = getSystemService(MediaSessionManager::class.java)
        val component = ComponentName(this, PlayerNotificationListener::class.java)
        val controllers = msm.getActiveSessions(component)

        controllers.forEach { controller ->
            val pkg = controller.packageName
            when (pkg) {
                PKG_SPOTIFY -> {
                    if (currentPlatform == MusicPlatform.Spotify) {
                        controllerMetadata(controller, pkg)
                    }
                }
                PKG_DEEZER -> {
                    if (currentPlatform == MusicPlatform.YoutubeMusic) {
                        controllerMetadata(controller, pkg)
                    }
                }
                PKG_YOUTUBE_MUSIC -> {
                    if (currentPlatform == MusicPlatform.Deezer) {
                        controllerMetadata(controller, pkg)
                    }
                }
            }
        }
    }

    private fun controllerMetadata(controller: MediaController, pkg: String) {
        val md = controller.metadata ?: return
        val title    = md.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
        val artist   = md.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
        val album    = md.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
        val duration = md.getLong(MediaMetadata.METADATA_KEY_DURATION)
        val bmp      = md.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) ?: md.getBitmap(MediaMetadata.METADATA_KEY_ART)

        val signature = "$title|$artist|$album|$duration"
        if (signature != lastTrackSignature) {
            lastTrackSignature = signature
            // TODO envoyer la pochette et les meta au service ble
            Log.d(TAG, "From session: $title - $artist ($album), from : $pkg" )
        }
    }
}