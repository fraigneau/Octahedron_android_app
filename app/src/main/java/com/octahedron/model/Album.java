package com.octahedron.model;

import android.graphics.Bitmap;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
        (
                tableName = "album",
                indices = { @Index(value = {"name"}, unique = true) }
        )
public class Album {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "name")
    public String name;

    @ColumnInfo(name = "cover", typeAffinity = ColumnInfo.BLOB)
    public Bitmap cover;
}
