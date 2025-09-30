package com.octahedron.data.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

@Entity
        (
                tableName = "artist",
                indices = { @Index(value = {"name"}, unique = true) }
        )
public class Artist {

    @PrimaryKey(autoGenerate = true)
    public long uid;

    @ColumnInfo(name = "name", collate = ColumnInfo.NOCASE)
    public String name;

}
