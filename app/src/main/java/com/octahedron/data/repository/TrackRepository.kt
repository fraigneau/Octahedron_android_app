package com.octahedron.data.repository

import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.data.relation.TrackWithArtists
import com.octahedron.data.model.Track
import com.octahedron.data.model.TrackArtist
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class TrackRepository @Inject constructor(
    private val trackDao: TrackDao,
    private val trackArtistDao: TrackArtistDao,
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
