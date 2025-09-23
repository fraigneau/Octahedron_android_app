package com.octahedron.repository

import com.octahedron.data.AppDatabase
import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.model.Artist
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ArtistRepository @Inject constructor(
    private val db: AppDatabase,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackAlbumDao: TrackAlbumDao,
    private val trackArtistDao: TrackArtistDao,
    private val listeningHistoryDao: ListeningHistoryDao
) {

    suspend fun insertArtist(artist: Artist): Long {
        return artistDao.insertIgnore(artist)
    }

    suspend fun insertFeatArtist(name: String): Long {
        val featName = "feat. $name"
        val existing = artistDao.getByName(featName)
        return existing?.uid ?: artistDao.insertIgnore(Artist().apply { this.name = featName })
    }

    suspend fun getArtistByName(name: String): Artist? {
        return artistDao.getByName(name)
    }

    suspend fun getAllArtists(): List<Artist> {
        return artistDao.getAll()
    }

    suspend fun getArtistById(id: Long): Artist? {
        return artistDao.getById(id)
    }

    suspend fun getOrCreate(name: String): Long =
        artistDao.getByName(name) ?.uid
            ?: artistDao.insertIgnore(Artist().apply { this.name = name })

    fun allArtistsFlow(): Flow<List<Artist>> = artistDao.getAllFlow()

    suspend fun updateArtist(artist: Artist) {
        artistDao.update(artist)
    }

    suspend fun deleteArtist(artist: Artist) {
        artistDao.delete(artist)
    }
}