package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octahedron.data.bus.EspConnectionBus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class ConnectionViewModel () : ViewModel() {

    private val _ui = MutableStateFlow(EspConnectionBus.Esp32ConnectionUi())
    val ui: StateFlow<EspConnectionBus.Esp32ConnectionUi> = _ui

    init {
        viewModelScope.launch {
            EspConnectionBus.flow.collect { state ->
                _ui.value = state
            }
        }
    }

}