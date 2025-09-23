package com.octahedron.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.octahedron.data.relation.ListeningWithTrackAndArtists
import com.octahedron.model.ListeningHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface ListeningHistoryDao {

    @Insert
    suspend fun insert(event: ListeningHistory): Long

    @Transaction
    @Query("SELECT * FROM listening_history ORDER BY listened_at DESC LIMIT :limit")
    fun getRecentWithLimit(limit: Int): Flow<List<ListeningWithTrackAndArtists>>

    @Transaction
    @Query("SELECT * FROM listening_history WHERE track_uid = :trackId ORDER BY listened_at DESC")
    fun getAllForTrack(trackId: Long): Flow<List<ListeningWithTrackAndArtists>>

    @Transaction
    @Query("""
        SELECT * FROM listening_history 
        WHERE listened_at BETWEEN :fromTs AND :toTs
        ORDER BY listened_at DESC
    """)
    fun getBetween(fromTs: Long, toTs: Long): Flow<List<ListeningWithTrackAndArtists>>

    @Query("""
        SELECT COUNT(*) FROM listening_history 
        WHERE listened_at BETWEEN :fromTs AND :toTs
    """)
    fun countBetween(fromTs: Long, toTs: Long): Flow<Int>

    @Query("DELETE FROM listening_history WHERE uid = :id")
    suspend fun deleteById(id: Long)

    data class CountPerId(val id: Long, val count: Int)

    @Query("""
      SELECT track_uid AS id, COUNT(*) AS count
      FROM listening_history
      WHERE listened_at BETWEEN :fromTs AND :toTs
      GROUP BY track_uid
      ORDER BY count DESC
      LIMIT :limit
    """)
    suspend fun topTracksBetween(fromTs: Long, toTs: Long, limit: Int): List<CountPerId>

    @Query("""
      SELECT ta.artist_uid AS id, COUNT(*) AS count
      FROM listening_history lh
      JOIN track_artist ta ON ta.track_uid = lh.track_uid
      WHERE lh.listened_at BETWEEN :fromTs AND :toTs
      GROUP BY ta.artist_uid
      ORDER BY count DESC
      LIMIT :limit
    """)
    suspend fun topArtistsBetween(fromTs: Long, toTs: Long, limit: Int): List<CountPerId>

    @Query("DELETE FROM listening_history WHERE listened_at < :olderThan")
    suspend fun purgeOlderThan(olderThan: Long)

}