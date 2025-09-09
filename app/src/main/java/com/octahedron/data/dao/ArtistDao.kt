package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.octahedron.data.relation.ArtistWithTracks
import com.octahedron.model.Artist

@Dao
interface ArtistDao {

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(artist: Artist): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertIgnore(artist: Artist): Long

    @Update
    suspend fun update(artist: Artist)

    @Delete
    suspend fun delete(artist: Artist)

    @Query("SELECT * FROM artist WHERE uid = :id")
    suspend fun getById(id: Long): Artist?

    @Query("SELECT * FROM artist WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Artist?

    @Query("SELECT * FROM artist ORDER BY name COLLATE NOCASE")
    suspend fun getAll(): List<Artist>

    @Transaction
    @Query("SELECT * FROM artist WHERE uid = :id")
    suspend fun getWithTracks(id: Long): ArtistWithTracks?
}