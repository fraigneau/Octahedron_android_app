package com.octahedron.repository

import com.octahedron.data.AppDatabase
import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.model.Album
import com.octahedron.model.TrackAlbum
import jakarta.inject.Inject

class AlbumRepository @Inject constructor(
    private val db: AppDatabase,
    private val trackDao: TrackDao,
    private val artistDao: ArtistDao,
    private val albumDao: AlbumDao,
    private val trackAlbumDao: TrackAlbumDao,
    private val trackArtistDao: TrackArtistDao,
    private val listeningHistoryDao: ListeningHistoryDao
) {
    suspend fun insertAlbum(album: com.octahedron.model.Album): Long {
        return albumDao.insertIgnore(album)
    }

    suspend fun getAlbumById(id: Long): com.octahedron.model.Album? {
        return albumDao.getById(id)
    }

    suspend fun getOrCreate(name: String): Long {
        val existing = albumDao.getByName(name)
        return existing?.uid ?: albumDao.insertIgnore(Album().apply { this.name = name })
    }

    suspend fun updateAlbum(album: com.octahedron.model.Album) {
        albumDao.update(album)
    }

    suspend fun deleteAlbum(album: com.octahedron.model.Album) {
        albumDao.delete(album)
    }
}