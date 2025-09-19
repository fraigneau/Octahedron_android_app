package com.octahedron.service

import android.content.Intent
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSession
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.content.ContextCompat

class PlayerNotificationListener: NotificationListenerService() {

    // TODO faire ne sorte que le notificationLisner recupere la notif a la creation du service
    companion object {
        private const val TAG = "PlayerNotificationListener"
    }

    private var bleService: BlePacketManager? = null
    private var bound = false
    private var lastTrackSignature: String? = null

    private val conn = object : android.content.ServiceConnection {
        override fun onServiceConnected(name: android.content.ComponentName?, service: IBinder?) {
            val b = service as? BlePacketManager.LocalBinder
            bleService = b?.getService()
            bound = bleService != null
            Log.i(TAG, "Bound to BlePacketManager: $bound")
        }
        override fun onServiceDisconnected(name: android.content.ComponentName?) {
            bound = false
            bleService = null
        }
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
        Log.i(TAG, "Notification listener started")

        val intent = Intent(this, BlePacketManager::class.java)
        ContextCompat.startForegroundService(this, intent)
        startService(intent)
        bindService(intent, conn, BIND_AUTO_CREATE)

        activeNotifications?.forEach { onNotificationPosted(it) }
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
                        Log.i(TAG, "Token retrieved successfully")
                        val controller = MediaController(this, token)
                        val metadata = controller.metadata
                        val title    = metadata?.getString(MediaMetadata.METADATA_KEY_TITLE) ?: ""
                        val artist   = metadata?.getString(MediaMetadata.METADATA_KEY_ARTIST) ?: ""
                        val album    = metadata?.getString(MediaMetadata.METADATA_KEY_ALBUM) ?: ""
                        val duration = metadata?.getLong(MediaMetadata.METADATA_KEY_DURATION) ?: 0L
                        val bmp = metadata?.getBitmap(MediaMetadata.METADATA_KEY_ALBUM_ART)?: return

                        val signature = "$title|$artist|$album|$duration"

                        if (signature == lastTrackSignature) {
                            Log.d(TAG, "Même morceau, on ignore la mise à jour de cover.")
                            return
                        }

                        lastTrackSignature = signature

                        if (bound && bleService != null) {
                            bleService!!.sendCover(bmp)
                        }

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