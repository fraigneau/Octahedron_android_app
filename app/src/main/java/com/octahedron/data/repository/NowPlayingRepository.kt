package com.octahedron.data.repository

import android.graphics.Bitmap
import androidx.room.withTransaction
import com.octahedron.data.AppDatabase
import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.data.model.Album
import com.octahedron.data.model.Artist
import com.octahedron.data.model.ListeningHistory
import com.octahedron.data.model.Track
import com.octahedron.data.model.TrackAlbum
import com.octahedron.data.model.TrackArtist
import jakarta.inject.Inject
import jakarta.inject.Singleton

@Singleton
class NowPlayingRepository @Inject constructor(
    private val db: AppDatabase,
    private val artistDao: ArtistDao,
    private val trackDao: TrackDao,
    private val trackArtistDao: TrackArtistDao,
    private val listeningHistoryDao: ListeningHistoryDao,
    private val trackAlbumDao: TrackAlbumDao,
    private val albumDao: AlbumDao,
) {
    data class Normalized(
        val title: String,
        val album: String,
        val img: Bitmap,
        val durationMs: Long?,
        val primaryArtist: String,
        val featuring: List<String> = emptyList()
    )

    suspend fun persistPlay(n: Normalized): Pair<Long, Long> = db.withTransaction {
        val trackId = getOrCreateTrack(n.title, n.durationMs ?: 0L)
        val mainArtistId = getOrCreateArtist(n.primaryArtist)
        val albumId = getOrCreateAlbum(n.album, n.img)

        linkTrackArtist(trackId, mainArtistId, role = "main")

        n.featuring.forEach { featName ->
            val featId = getOrCreateArtist(featName)
            linkTrackArtist(trackId, featId, role = "feat")
        }

        listeningHistoryDao.insert(
            ListeningHistory().apply {
                trackId.also { this.trackId = it }
                listenedAt = System.currentTimeMillis()
            }
        )

        linkTrackAlbum(trackId, albumId)

        trackId to mainArtistId
    }

    private suspend fun getOrCreateArtist(name: String): Long {
        val existing = artistDao.getByName(name)
        if (existing != null) return existing.uid
        return artistDao.insertIgnore(Artist().apply { this.name = name })
    }

    private suspend fun getOrCreateTrack(title: String, duration: Long): Long {
        val existing = trackDao.getByTitle(title)
        if (existing != null) return existing.uid
        return trackDao.insertIgnore(
            Track().apply {
                this.title = title
                this.duration = duration
            }
        )    }

    private suspend fun getOrCreateAlbum(title: String, img: Bitmap): Long {
        val existing = albumDao.getByName(title)
        if (existing != null) return existing.uid
        return albumDao.insertIgnore(
            Album().apply {
                this.name = title
                this.cover = img
            }
        )
    }

    private suspend fun linkTrackArtist(trackId: Long, artistId: Long, role: String) {
        val cross = TrackArtist().apply {
            this.trackId = trackId
            this.artistId = artistId
            this.role = role
        }
        trackArtistDao.insertIgnore(cross)
    }

    private suspend fun linkTrackAlbum(trackId: Long, albumId: Long) {
        val cross = TrackAlbum().apply {
            this.trackId = trackId
            this.albumId = albumId
        }
        trackAlbumDao.upsert(cross)
    }
}