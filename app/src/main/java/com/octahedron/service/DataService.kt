package com.octahedron.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.getSystemService
import com.octahedron.data.bus.NowPlayingBus
import com.octahedron.data.bus.NowPlayingBus.NowPlaying
import com.octahedron.repository.NowPlayingRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DataService : Service() {

    companion object {
        private const val TAG = "DataService"
        private const val DEBOUNCE_MS = 30_000L
    }

    @Inject lateinit var nowPlayingRepository: NowPlayingRepository

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DataService created")

        scope.launch {
            try {
                NowPlayingBus.flow
                    .map { it to stableKey(it) }
                    .distinctUntilChanged { a, b -> a.second == b.second }
                    .debounce(DEBOUNCE_MS)
                    .map { it.first }
                    .collectLatest { processNowPlaying(it) }
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

    // ---------- Pipeline métier ----------

    private suspend fun processNowPlaying(now: NowPlaying) {
        val n = normalize(now)

        if (n.title.isEmpty() || n.primaryArtist.isEmpty()) {
            Log.w(TAG, "Donnée invalide: $n")
            return
        }

        try {
            val (trackId, artistId) = nowPlayingRepository.persistPlay(
                NowPlayingRepository.Normalized(
                    title = n.title,
                    album = n.album,
                    img = n.img,
                    durationMs = n.durationMs.takeIf { it > 0 },
                    primaryArtist = n.primaryArtist,
                    featuring = splitArtists(now.artist).drop(1)
                )
            )
            Log.d(TAG, "Persisté: trackId=$trackId artistId=$artistId ($n)")
        } catch (t: Throwable) {
            Log.e(TAG, "Echec persistance NowPlaying: $n", t)
        }
    }

    // ---------- Normalisation ----------

    private data class NormalizedNowPlaying(
        val title: String,
        val album: String,
        val img: Bitmap,
        val durationMs: Long,
        val primaryArtist: String,
        val featuring: List<String> = emptyList()
    )

    private fun normalize(src: NowPlaying): NormalizedNowPlaying =
        NormalizedNowPlaying(
            title = src.title.orEmpty().trim(),
            album = src.album.orEmpty().trim(),
            img = src.bitmap!!,
            durationMs = src.durationMs!!,
            primaryArtist = splitArtists(src.artist).firstOrNull()
                ?: src.artist.orEmpty().trim(),
            featuring = splitArtists(src.artist).drop(1)
        )

    // TODO: regarder les separations possibles
    private fun splitArtists(raw: String?): List<String> =
        (raw ?: "")
            .split("&", ",", "feat.", "Feat.", "FT.", "ft.")
            .map { it.trim() }
            .filter { it.isNotEmpty() }

    private fun stableKey(src: NowPlaying): String =
        "${src.title.orEmpty().trim()}|${src.artist.orEmpty().trim()}|${src.album.orEmpty().trim()}|${src.durationMs}"

}
