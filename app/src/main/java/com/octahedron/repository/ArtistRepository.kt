package com.octahedron.repository

import com.octahedron.data.dao.ArtistDao
import com.octahedron.model.Artist
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class ArtistRepository @Inject constructor(
    private val artistDao: ArtistDao,
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