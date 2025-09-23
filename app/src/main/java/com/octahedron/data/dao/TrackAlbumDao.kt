package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import com.octahedron.model.Album
import com.octahedron.model.Artist
import com.octahedron.model.TrackAlbum

@Dao
interface TrackAlbumDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(crossRef: TrackAlbum)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(crossRefs: List<TrackAlbum>)

    @Upsert
    suspend fun upsert(crossRef: TrackAlbum)

    @Query("DELETE FROM track_album WHERE track_uid = :trackId")
    suspend fun deleteForTrack(trackId: Long)

    @Query("DELETE FROM track_album WHERE album_uid = :albumId")
    suspend fun deleteForAlbum(albumId: Long)

    @Query("""
       SELECT EXISTS(
         SELECT 1 FROM track_album
         WHERE track_uid = :trackId AND album_uid = :albumId
       )
    """)
    suspend fun exists(trackId: Long, albumId: Long): Boolean

    @Query("""
       SELECT al.* FROM album al
       INNER JOIN track_album ta ON ta.album_uid = al.uid
       WHERE ta.track_uid = :trackId
       ORDER BY al.name COLLATE NOCASE
    """)
    suspend fun getAlbumsForTrack(trackId: Long): List<Album>



}