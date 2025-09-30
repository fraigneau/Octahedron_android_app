package com.octahedron.data.repository

import com.octahedron.data.dao.AlbumDao
import com.octahedron.data.model.Album
import jakarta.inject.Inject

class AlbumRepository @Inject constructor(
    private val albumDao: AlbumDao,
) {
    suspend fun insertAlbum(album: Album): Long {
        return albumDao.insertIgnore(album)
    }

    suspend fun getAlbumById(id: Long): Album? {
        return albumDao.getById(id)
    }

    suspend fun getOrCreate(name: String): Long {
        val existing = albumDao.getByName(name)
        return existing?.uid ?: albumDao.insertIgnore(Album().apply { this.name = name })
    }

    suspend fun updateAlbum(album: Album) {
        albumDao.update(album)
    }

    suspend fun deleteAlbum(album: Album) {
        albumDao.delete(album)
    }
}