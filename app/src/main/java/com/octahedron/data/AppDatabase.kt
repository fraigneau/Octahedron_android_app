package com.octahedron.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.octahedron.model.Artist
import com.octahedron.model.ListeningHistory
import com.octahedron.model.Track
import com.octahedron.model.TrackArtist
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao


@Database(
    entities = [
        Track::class,
        Artist::class,
        TrackArtist::class,
        ListeningHistory::class,
    ],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun trackArtistDao(): TrackArtistDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao
}