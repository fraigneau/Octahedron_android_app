package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.octahedron.data.relation.TrackWithArtists
import com.octahedron.model.Track
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {

    @Insert(onConflict = OnConflictStrategy.Companion.ABORT)
    suspend fun insert(track: Track): Long

    @Insert(onConflict = OnConflictStrategy.Companion.IGNORE)
    suspend fun insertIgnore(track: Track): Long

    @Update
    suspend fun update(track: Track)

    @Delete
    suspend fun delete(track: Track)

    @Query("SELECT * FROM track WHERE uid = :id")
    suspend fun getById(id: Long): Track?

    @Query("SELECT * FROM track WHERE title = :title")
    suspend fun getByTitle(title: String): Track?

    @Transaction
    @Query("SELECT * FROM track WhERE uid = :id")
    suspend fun getByIdWithArtists(id: Long): TrackWithArtists?

    @Transaction
    @Query("""
        SELECT t.* FROM track t
        INNER JOIN track_artist ta ON ta.track_uid = t.uid
        WHERE ta.artist_uid = :artistId
        ORDER BY t.title COLLATE NOCASE
    """)
    fun getTracksForArtist(artistId: Long): Flow<List<Track>>

    @Query("SELECT * FROM track ORDER BY title COLLATE NOCASE")
    fun getAll(): Flow<List<Track>>

    @Query("SELECT * FROM track WHERE title LIKE '%' || :query || '%' ORDER BY title COLLATE NOCASE")
    fun searchByTitle(query: String): Flow<List<Track>>

}