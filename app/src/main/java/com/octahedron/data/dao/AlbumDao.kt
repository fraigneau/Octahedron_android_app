package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.octahedron.model.Album
import kotlinx.coroutines.flow.Flow

@Dao
interface AlbumDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(album: Album): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(album: Album): Long

    @Update
    suspend fun update(album: Album)

    @Delete
    suspend fun delete(album: Album)

    @Query("SELECT * FROM album WHERE uid = :id")
    suspend fun getById(id: Long): Album?

    @Query("SELECT * FROM album WHERE name = :name COLLATE NOCASE LIMIT 1")
    suspend fun getByName(name: String): Album?

    @Query("SELECT * FROM album ORDER BY name COLLATE NOCASE")
    fun getAllFlow(): Flow<List<Album>>


}