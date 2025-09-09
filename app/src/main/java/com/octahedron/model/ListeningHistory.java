package com.octahedron.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
        (
                tableName = "listening_history",
                foreignKeys = @ForeignKey(
                        entity = Track.class,
                        parentColumns = "uid",
                        childColumns = "track_uid",
                        onDelete = ForeignKey.CASCADE
                ),
                indices = {
                        @Index("track_uid"),
                        @Index("listened_at")
                }
        )
public class ListeningHistory {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "track_uid")
    public long trackId;

    @ColumnInfo(name = "listened_at")
    public long listenedAt;
}