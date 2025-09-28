package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octahedron.repository.ListeningHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId
import javax.inject.Inject


data class WeeklyChartUiState(
    val days: List<ListeningHistoryRepository.DailyStat> = emptyList(),
    val weekTotalMs: Long = 0L,
    val rowCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repo: ListeningHistoryRepository
): ViewModel() {
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
}