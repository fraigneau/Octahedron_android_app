package com.octahedron.data.image

import android.graphics.Bitmap
import android.graphics.BitmapFactory.decodeByteArray
import androidx.room.TypeConverter
import java.io.ByteArrayOutputStream

class Converters {

    @TypeConverter
    fun fromBitmap(bmp: Bitmap?): ByteArray? =
        bmp?.let {
            ByteArrayOutputStream().apply {
                it.compress(Bitmap.CompressFormat.JPEG, 100, this)
            }.toByteArray()
        }

    @TypeConverter
    fun toBitmap(bytes: ByteArray?): Bitmap? =
        bytes?.let {  decodeByteArray(it, 0, it.size) }
}