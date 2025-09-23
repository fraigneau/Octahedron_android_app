package com.octahedron.repository

import androidx.room.util.copy
import androidx.room.withTransaction
import com.octahedron.data.AppDatabase
import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.data.relation.TrackWithArtists
import com.octahedron.model.Artist
import com.octahedron.model.ListeningHistory
import com.octahedron.model.Track
import com.octahedron.model.TrackArtist
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class TrackRepository @Inject constructor(
    private val db: AppDatabase,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackAlbumDao: TrackAlbumDao,
    private val trackArtistDao: TrackArtistDao,
    private val listeningHistoryDao: ListeningHistoryDao
) {
    suspend fun insertTrack(track: Track): Long {
        return trackDao.insertIgnore(track)
    }

    suspend fun getTrackByTitle(title: String): Track? {
        return trackDao.getByTitle(title)
    }

    suspend fun getTrackById(id: Long): Track? {
        return trackDao.getById(id)
    }

    suspend fun getTrackByIdWithArtists(id: Long): TrackWithArtists? {
        return trackDao.getByIdWithArtists(id)
    }

    fun getAllTracks(): Flow<List<Track>> {
        return trackDao.getAll()
    }

    fun searchTracksByTitle(query: String): Flow<List<Track>> {
        return trackDao.searchByTitle(query)
    }

    fun getTracksForArtist(artistId: Long): Flow<List<Track>> {
        return trackDao.getTracksForArtist(artistId)
    }

    suspend fun getOrCreate(title: String, duration: Long?): Long {
        val d = duration ?: 0L
        val existing = trackDao.getByTitleAndDuration(title, d)
            ?: trackDao.getByTitleCaseInsensitive(title) // fallback si duration inconnue
        return existing?.uid ?: trackDao.insertIgnore(Track().apply {
            this.title = title
            this.duration = d
        })
    }

    suspend fun linkArtist(trackId: Long, artistId: Long) {
        if (!trackArtistDao.exists(trackId, artistId)) {
            trackArtistDao.insertIgnore(TrackArtist().apply {
                trackId.also { this.trackId = it }
                artistId.also { this.artistId = it }
            })
        }
    }


    suspend fun updateTrack(track: Track) {
        trackDao.update(track)
    }

    suspend fun deleteTrack(track: Track) {
        trackDao.delete(track)
    }
}
