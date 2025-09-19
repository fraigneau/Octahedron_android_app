package com.octahedron.data.image


import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import androidx.core.graphics.scale


object ImageTools {
    private const val TAG = "ImageTools"
    private const val TARGET = 240
    private const val BYTES_PER_PIXEL_RGB565 = 2
    private const val BIN_SIZE = TARGET * TARGET * BYTES_PER_PIXEL_RGB565

    @WorkerThread
    fun resizeTo240(src: Bitmap): Bitmap {
        val dstW = TARGET
        val dstH = TARGET

        val scale = maxOf(
            dstW.toFloat() / src.width.toFloat(),
            dstH.toFloat() / src.height.toFloat()
        )
        val scaledW = (src.width * scale).toInt().coerceAtLeast(1)
        val scaledH = (src.height * scale).toInt().coerceAtLeast(1)

        val scaled = src.scale(scaledW, scaledH)
        val left = ((scaledW - dstW) / 2).coerceAtLeast(0)
        val top  = ((scaledH - dstH) / 2).coerceAtLeast(0)

        val cropped = Bitmap.createBitmap(
            scaled,
            left,
            top,
            dstW.coerceAtMost(scaled.width - left),
            dstH.coerceAtMost(scaled.height - top)
        )

        if (scaled !== src) scaled.recycle()
        return cropped
    }

    @WorkerThread
    fun toRgb565Bytes(bitmap240: Bitmap): ByteArray {
        require(bitmap240.width == TARGET && bitmap240.height == TARGET) {
            "toRgb565Bytes nécessite un bitmap ${TARGET}x${TARGET}"
        }

        val pixels = IntArray(TARGET * TARGET)
        bitmap240.getPixels(pixels, 0, TARGET, 0, 0, TARGET, TARGET)

        val out = ByteArray(BIN_SIZE)
        var i = 0
        for (px in pixels) {
            val r = (px ushr 16) and 0xFF
            val g = (px ushr 8) and 0xFF
            val b = (px) and 0xFF

            val r5 = (r ushr 3) and 0x1F
            val g6 = (g ushr 2) and 0x3F
            val b5 = (b ushr 3) and 0x1F

            val rgb565 = (r5 shl 11) or (g6 shl 5) or b5
            out[i++] = (rgb565).toByte()        // LSB
            out[i++] = (rgb565 ushr 8).toByte() // MSB
        }
        return out
    }

    @WorkerThread
    fun toRgb565BytesAuto(src: Bitmap): ByteArray {
        if (src.width == TARGET && src.height == TARGET) {
            return toRgb565Bytes(src)
        }
        val resized = resizeTo240(src)
        return try {
            toRgb565Bytes(resized)
        } finally {
            resized.recycle()
        }
    }
}
