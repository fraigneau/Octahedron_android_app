package com.octahedron.data.bus

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.Instant

object EspConnectionBus {
    enum class ConnectionStatus { Connected, Connecting, Disconnected }
    data class Esp32ConnectionUi(
        val status: ConnectionStatus = ConnectionStatus.Disconnected,
        val lastPingMs: Long? = null,
        val lastPingAt: Instant? = null,
        val lastImageAt: Instant? = null,
        val lastImageName : String? = null,
        val errorMessage: String? = null
    )
    sealed class PatchN<out T> {
        data class Set<T>(val value: T) : PatchN<T>()
        data object Keep : PatchN<Nothing>()
        data object Clear : PatchN<Nothing>()
    }

    fun <T> set(v: T) = PatchN.Set(v)
    val keep = PatchN.Keep
    val clear = PatchN.Clear

    private val _state = MutableStateFlow(Esp32ConnectionUi())
    val flow: StateFlow<Esp32ConnectionUi> = _state.asStateFlow()

    fun update(
        status: PatchN<ConnectionStatus> = keep,
        lastPingMs: PatchN<Long> = keep,
        lastPingAt: PatchN<Instant> = keep,
        lastImageAt: PatchN<Instant> = keep,
        lastImageName: PatchN<String> = keep,
        errorMessage: PatchN<String> = keep
    ) {
        _state.update { s ->
            s.copy(
                status = when (status) {
                    is PatchN.Set -> status.value
                    PatchN.Keep -> s.status
                    PatchN.Clear -> s.status // status non-nullable → ignore Clear
                },
                lastPingMs = when (lastPingMs) {
                    is PatchN.Set -> lastPingMs.value
                    PatchN.Keep -> s.lastPingMs
                    PatchN.Clear -> null
                },
                lastPingAt = when (lastPingAt) {
                    is PatchN.Set -> lastPingAt.value
                    PatchN.Keep -> s.lastPingAt
                    PatchN.Clear -> null
                },
                lastImageAt = when (lastImageAt) {
                    is PatchN.Set -> lastImageAt.value
                    PatchN.Keep -> s.lastImageAt
                    PatchN.Clear -> null
                },
                lastImageName = when (lastImageName) {
                    is PatchN.Set -> lastImageName.value
                    PatchN.Keep -> s.lastImageName
                    PatchN.Clear -> null
                },
                errorMessage = when (errorMessage) {
                    is PatchN.Set -> errorMessage.value
                    PatchN.Keep -> s.errorMessage
                    PatchN.Clear -> null
                }
            )
        }
    }
}