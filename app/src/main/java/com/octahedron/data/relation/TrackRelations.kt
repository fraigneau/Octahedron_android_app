package com.octahedron.data.relation

import androidx.room.*
import com.octahedron.model.*

data class TrackWithArtists(
    @Embedded val track: Track,
    @Relation(
        parentColumn = "uid",
        entity = Artist::class,
        entityColumn = "uid",
        associateBy = Junction(
            value = TrackArtist::class,
            parentColumn = "track_uid",
            entityColumn = "artist_uid"
        )
    )
    val artists: List<Artist>
)

data class ArtistWithTracks(
    @Embedded val artist: Artist,
    @Relation(
        parentColumn = "uid",
        entity = Track::class,
        entityColumn = "uid",
        associateBy = Junction(
            value = TrackArtist::class,
            parentColumn = "artist_uid",
            entityColumn = "track_uid"
        )
    )
    val tracks: List<Track>
)

data class ListeningWithTrackAndArtists(
    @Embedded val history: ListeningHistory,
    @Relation(
        parentColumn = "track_uid",
        entityColumn = "uid"
    )
    val track: Track,
    @Relation(
        parentColumn = "track_uid",
        entity = Artist::class,
        entityColumn = "uid",
        associateBy = Junction(
            value = TrackArtist::class,
            parentColumn = "track_uid",
            entityColumn = "artist_uid"
        )
    )
    val artists: List<Artist>
)

data class ListeningWithTrackAndArtistsAndAlbum(
    @Embedded val history: ListeningHistory,
    @Relation(
        parentColumn = "track_uid",
        entityColumn = "uid"
    )
    val track: Track,
    @Relation(
        parentColumn = "track_uid",
        entity = Artist::class,
        entityColumn = "uid",
        associateBy = Junction(
            value = TrackArtist::class,
            parentColumn = "track_uid",
            entityColumn = "artist_uid"
        )
    )
    val artists: List<Artist>,
    @Relation(
        parentColumn = "album_uid",
        entityColumn = "uid"
    )
    val album: Album?
)