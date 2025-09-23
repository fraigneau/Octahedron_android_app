package com.octahedron.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
        (
                tableName = "track",
                indices = { @Index(value = {"title"}) }
        )
public class Track {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "title")
    public String title;

    @ColumnInfo(name = "duration")
    public long duration;
}