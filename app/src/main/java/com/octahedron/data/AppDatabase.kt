package com.octahedron.data

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.octahedron.data.dao.AlbumDao
import com.octahedron.model.Artist
import com.octahedron.model.ListeningHistory
import com.octahedron.model.Track
import com.octahedron.model.Album
import com.octahedron.model.TrackAlbum
import com.octahedron.model.TrackArtist
import com.octahedron.data.dao.ArtistDao
import com.octahedron.data.dao.ListeningHistoryDao
import com.octahedron.data.dao.TrackAlbumDao
import com.octahedron.data.dao.TrackArtistDao
import com.octahedron.data.dao.TrackDao
import com.octahedron.data.image.Converters


@TypeConverters(Converters::class)
@Database(
    entities = [
        Track::class,
        Artist::class,
        TrackArtist::class,
        Album::class,
        TrackAlbum::class,
        ListeningHistory::class,
    ],
    version = 3,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun artistDao(): ArtistDao
    abstract fun trackArtistDao(): TrackArtistDao
    abstract fun listeningHistoryDao(): ListeningHistoryDao
    abstract fun albumDao(): AlbumDao
    abstract fun trackAlbumDao(): TrackAlbumDao
}