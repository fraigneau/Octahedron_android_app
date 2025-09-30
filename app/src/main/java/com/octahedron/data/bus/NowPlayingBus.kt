package com.octahedron.data.bus

import android.graphics.Bitmap
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

object NowPlayingBus {
    data class NowPlaying(
        val title: String? = null,
        val artist: String? = null,
        val album: String? = null,
        val durationMs: Long? = null,
        val bitmap: Bitmap? = null
    )
    private val _flow = MutableSharedFlow<NowPlaying>(
        replay = 1,
        extraBufferCapacity = 1
    )
    val flow = _flow.asSharedFlow()

    fun emit(value: NowPlaying) {
        _flow.tryEmit(value)
    }

    private val _state = MutableStateFlow(NowPlaying())
    val state: StateFlow<NowPlaying> = _state.asStateFlow()
}