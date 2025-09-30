package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octahedron.data.bus.EspConnectionBus
import com.octahedron.data.bus.NowPlayingBus
import com.octahedron.data.repository.ListeningHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    repo: ListeningHistoryRepository
): ViewModel() {

    companion object {
        data class WeeklyChartUiState(
            val days: List<ListeningHistoryRepository.DailyStat> = emptyList(),
            val weekTotalMs: Long = 0L,
            val rowCount: Int = 0
        )
    }
    private val zone = ZoneId.systemDefault()

    val uiState: StateFlow<WeeklyChartUiState> =
        repo.dailyTotalsThisWeek(zone)
            .map { days ->
                WeeklyChartUiState(
                    days = days,
                    weekTotalMs = days.sumOf { it.totalPlayTimeMs },
                    rowCount = days.sumOf { it.playCount }
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = WeeklyChartUiState()
            )
    private val _ui = MutableStateFlow(EspConnectionBus.Esp32ConnectionUi())
    val ui: StateFlow<EspConnectionBus.Esp32ConnectionUi> = _ui

    init {
        viewModelScope.launch {
            EspConnectionBus.flow.collect { state ->
                _ui.value = state
            }
        }
    }

    private val _lastPlayed = MutableStateFlow(NowPlayingBus.NowPlaying())
    val lastPlayed: StateFlow<NowPlayingBus.NowPlaying> = _lastPlayed

    init {
        viewModelScope.launch {
            NowPlayingBus.flow.collect { np ->
                _lastPlayed.value = np
            }
        }
    }
}