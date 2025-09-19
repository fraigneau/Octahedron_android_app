package com.octahedron.data.bus

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object NowPlayingBus {
    data class NowPlaying(
        val title: String,
        val artist: String,
        val album: String,
        val durationMs: Long,
        val bitmap: Bitmap?
    )
    private val _flow = MutableSharedFlow<NowPlaying>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val flow = _flow.asSharedFlow()

    fun emit(value: NowPlaying) {
        _flow.tryEmit(value)
    }

    fun lastOrNull(): NowPlaying? = _flow.replayCache.firstOrNull()
}