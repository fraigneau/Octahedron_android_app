package com.octahedron.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.octahedron.data.bus.NowPlayingBus
import com.octahedron.data.bus.NowPlayingBus.NowPlaying
import com.octahedron.model.Artist
import com.octahedron.model.ListeningHistory
import com.octahedron.model.Track
import com.octahedron.model.TrackArtist
import com.octahedron.repository.ArtistRepository
import com.octahedron.repository.TrackRepository
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

@AndroidEntryPoint
class DataService : Service() {

    companion object {
        private const val TAG = "DataService"
        private const val CHANNEL_ID = "data_service_channel"
        private const val NOTIF_ID = 1
    }

    @Inject lateinit var trackRepository: TrackRepository
    @Inject lateinit var artistRepository: ArtistRepository
    @Inject lateinit var trackArtistDao: TrackArtistDao
    @Inject lateinit var listeningHistoryDao: ListeningHistoryDao

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DataService created")
        createNotificationChannel()
        startForeground(NOTIF_ID, buildNotification("Initialisation"))
        scope.launch {
            try {
                NowPlayingBus.flow
                    .map { it to stablekey(it)}
                    .distinctUntilChanged { a, b -> a.second == b.second }
                    .debounce (30_000)
                    .map { it.first }
                    .collect { processNowPlaying(it) }

            } catch (t: Throwable) {
                Log.e(TAG, "Erreur collecte NowPlaying", t)
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.i(TAG, "onStartCommand startId=$startId flags=$flags")
        return START_STICKY
    }

    override fun onDestroy() {
        Log.i(TAG, "onDestroy")
        job.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private suspend fun processNowPlaying(now: NowPlaying) {
        val normalized = normalize(now)
        if (normalized.title.isEmpty() || normalized.primaryArtist.isEmpty()) {
            Log.w(TAG, "Donnée invalide: $normalized")
            return
        }

        val artistId = ensureArtist(normalized.primaryArtist)
        val trackId  = ensureTrack(normalized)
        ensureTrackArtistLink(trackId, artistId)
        recordListening(trackId)

        Log.d(TAG, "Persisté: trackId=$trackId artistId=$artistId ($normalized)")
    }

    private data class NormalizedNowPlaying(
        val title: String,
        val album: String,
        val durationMs: Long,
        val primaryArtist: String
    )

    private fun normalize(src: NowPlaying): NormalizedNowPlaying =
        NormalizedNowPlaying(
            title = src.title.trim(),
            album = src.album.trim(),
            durationMs = src.durationMs,
            primaryArtist = splitArtists(src.artist).firstOrNull() ?: src.artist.trim()
        )

    private fun splitArtists(raw: String): List<String> =
        raw.split("&", ",", "feat.", "Feat.", "FT.", "ft.")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun toArtist(name: String): Artist = Artist().apply { this.name = name }

    private fun toTrack(n: NormalizedNowPlaying): Track = Track().apply {
        title = n.title
        albumName = n.album
        duration = n.durationMs
    }

    private fun toTrackArtist(trackId: Long, artistId: Long): TrackArtist =
        TrackArtist().apply {
            this.trackId = trackId
            this.artistId = artistId
        }

    private fun toListeningHistory(trackId: Long, ts: Long): ListeningHistory =
        ListeningHistory().apply {
            this.trackId = trackId
            this.listenedAt = ts
        }

    private suspend fun ensureArtist(name: String): Long {
        val existing = artistRepository.getArtistByName(name)
        return existing?.uid ?: artistRepository.insertArtist(toArtist(name))
    }

    private suspend fun ensureTrack(n: NormalizedNowPlaying): Long {
        val existing = trackRepository.getTrackByTitle(n.title)
        return existing?.uid ?: trackRepository.insertTrack(toTrack(n))
    }

    private suspend fun ensureTrackArtistLink(trackId: Long, artistId: Long) {
        trackArtistDao.insertIgnore(toTrackArtist(trackId, artistId))
    }

    private suspend fun recordListening(trackId: Long) {
        listeningHistoryDao.insert(toListeningHistory(trackId, System.currentTimeMillis()))
    }

    private fun stablekey(src: NowPlaying): String =" ${src.title.trim()}|${src.artist.trim()}|${src.album.trim()}|${src.durationMs}"

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val nm = getSystemService<NotificationManager>() ?: return
            val ch = NotificationChannel(
                CHANNEL_ID,
                "Data Service",
                NotificationManager.IMPORTANCE_MIN
            )
            nm.createNotificationChannel(ch)
        }
    }

    private fun buildNotification(text: String): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Data service")
            .setContentText(text)
            .setSmallIcon(android.R.drawable.stat_notify_sync)
            .setOngoing(true)
            .build()
}
