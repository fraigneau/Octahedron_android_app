package com.octahedron.repository

import com.octahedron.data.dao.AlbumDao
import com.octahedron.model.Album
import jakarta.inject.Inject

class AlbumRepository @Inject constructor(
    private val albumDao: AlbumDao,
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