package com.octahedron.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;

@Entity
        (
                tableName = "track_album",
                primaryKeys = {"track_uid", "album_uid"},
                foreignKeys = {
                        @ForeignKey(entity = Track.class,  parentColumns = "uid", childColumns = "track_uid",  onDelete = ForeignKey.CASCADE),
                        @ForeignKey(entity = Album.class, parentColumns = "uid", childColumns = "album_uid", onDelete = ForeignKey.CASCADE)
                },
                indices = { @Index("album_uid"), @Index("track_uid") }
        )
public class TrackAlbum {

    @ColumnInfo(name = "track_uid")
    public long trackId;

    @ColumnInfo(name = "album_uid")
    public long albumId;

}
