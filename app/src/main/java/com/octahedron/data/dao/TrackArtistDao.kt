package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.octahedron.model.Artist
import com.octahedron.model.TrackArtist

@Dao
interface TrackArtistDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(crossRef: TrackArtist)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(crossRefs: List<TrackArtist>)

    @Query("DELETE FROM track_artist WHERE track_uid = :trackId")
    suspend fun deleteForTrack(trackId: Long)

    @Query("DELETE FROM track_artist WHERE artist_uid = :artistId")
    suspend fun deleteForArtist(artistId: Long)

    @Query("""
       SELECT EXISTS
    (
         SELECT 1 FROM track_artist
         WHERE track_uid = :trackId AND artist_uid = :artistId
    )
    """)
    suspend fun exists(trackId: Long, artistId: Long): Boolean

    @Query("""
       SELECT a.* FROM artist a
       INNER JOIN track_artist ta ON ta.artist_uid = a.uid
       WHERE ta.track_uid = :trackId
       ORDER BY a.name COLLATE NOCASE
    """)
    suspend fun getArtistsForTrack(trackId: Long): List<Artist>



}