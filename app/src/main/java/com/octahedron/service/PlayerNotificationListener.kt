package com.octahedron.service

import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log

class PlayerNotificationListener: NotificationListenerService() {

    companion object {
        private const val TAG = "PlayerNotificationListener"

    }

    // TODO : Make this configurable from DataStore settings
    public var currentPlatform: Listened_Patforms? = Listened_Patforms.Spotify // just for testing

    enum class Listened_Patforms {
        Spotify,
        Deezer,
        YoutubeMusic,
    }

    data class TrackInfos(
        val title: String,
        val artist: String,
        val album: String,
        val duration: Long,
        val isPlaying: Boolean,
        val platform: Listened_Patforms,
    )

    override fun onListenerConnected() {

        Log.i(TAG, "Notification listener started" )
        activeNotifications?.forEach { snb ->
            Log.d(TAG, "Notification from: ${snb.packageName}" )
        }
    }

    override fun onListenerDisconnected() {
        Log.i(TAG, "Notification listener stopped" )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val pkg = sbn?.packageName
        val extras = sbn?.notification?.extras
        Log.i(TAG, "Notification posted: $pkg")

        when (pkg) {
            "com.spotify.music" -> {
                if (currentPlatform == Listened_Patforms.Spotify) {
                    val token: MediaSession.Token? = if (Build.VERSION.SDK_INT >= 33) {
                        extras?.getParcelable<MediaSession.Token>("android.mediaSession", MediaSession.Token::class.java)
                    } else {
                        @Suppress("DEPRECATION")
                        extras?.getParcelable<MediaSession.Token>("android.mediaSession")
                    }
                    if (token != null) {
                        val controller = MediaController(this, token)
                        val metadata = controller.metadata


                        // TODO: tranform in object TrackInfos
                        Log.d(TAG, "Metadata: title=${metadata?.getString(MediaMetadata.METADATA_KEY_TITLE)}")
                        Log.d(TAG, "Metadata: artist=${metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST)}")
                        Log.d(TAG, "Metadata: album=${metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM)}")
                        Log.d(TAG, "Metadata: duration=${metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION)}")
                        Log.d(TAG, "Cover: ${metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART) != null}")
                    }
                }
            }
            // TODO: analyse other platforms for metadata
            "com.google.android.apps.youtube.music" -> {
                if (currentPlatform == Listened_Patforms.YoutubeMusic) {
                    val title = extras?.getCharSequence("android.title") ?: "Unknown"
                    val artist = extras?.getCharSequence("android.text") ?: "Unknown"
                    Log.d(TAG, "YouTube Music - Title: $title, Artist: $artist")
                }
            }
            "deezer.android.app" -> {
                if (currentPlatform == Listened_Patforms.Deezer) {
                    val title = extras?.getCharSequence("android.title") ?: "Unknown"
                    val artist = extras?.getCharSequence("android.text") ?: "Unknown"
                    Log.d(TAG, "Deezer - Title: $title, Artist: $artist")
                }
            }
        }
    }
}