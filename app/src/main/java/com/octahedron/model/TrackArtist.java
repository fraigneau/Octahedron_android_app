package com.octahedron.model;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity
        (
                tableName = "track_artist",
                primaryKeys = {"track_uid", "artist_uid"},
                foreignKeys = {
                        @ForeignKey(entity = Track.class,  parentColumns = "uid", childColumns = "track_uid",  onDelete = ForeignKey.CASCADE),
                        @ForeignKey(entity = Artist.class, parentColumns = "uid", childColumns = "artist_uid", onDelete = ForeignKey.CASCADE)
                },
                indices = { @Index("artist_uid"), @Index("track_uid") }
        )
public class TrackArtist {
    @ColumnInfo(name = "track_uid")
    public long trackId;
    @ColumnInfo(name = "artist_uid")
    public long artistId;
    @ColumnInfo(name = "role", defaultValue = "'main'")
    public String role;
}
