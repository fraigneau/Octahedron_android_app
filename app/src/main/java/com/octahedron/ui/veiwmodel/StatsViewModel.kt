package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.octahedron.repository.ListeningHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import java.time.ZoneId

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repo: ListeningHistoryRepository
) : ViewModel() {
    enum class Period { TODAY, WEEK, MONTH, YEAR, ALL_TIME }

    data class UiState(
        val isLoading: Boolean = true,
        val stats: ListeningHistoryRepository.PeriodStats? = null,
        val error: String? = null,
        val period: Period = Period.ALL_TIME,
        val zoneId: ZoneId = ZoneId.systemDefault()
    )

    private val _period = MutableStateFlow(Period.ALL_TIME)
    val period: StateFlow<Period> = _period.asStateFlow()

    private val _zoneId = MutableStateFlow(ZoneId.systemDefault())
    val zoneId: StateFlow<ZoneId> = _zoneId.asStateFlow()

    val uiState: StateFlow<UiState> =
        _period.combine(_zoneId) { p, z -> p to z }
            .flatMapLatest { (p, z) ->
                selectFlow(p, z)
                    .map<ListeningHistoryRepository.PeriodStats, UiState> { stats ->
                        UiState(
                            isLoading = false,
                            stats = stats,
                            error = null,
                            period = p,
                            zoneId = z
                        )
                    }
                    .onStart {
                        emit(
                            UiState(
                                isLoading = true,
                                stats = null,
                                error = null,
                                period = p,
                                zoneId = z
                            )
                        )
                    }
                    .catch { e ->
                        emit(
                            UiState(
                                isLoading = false,
                                stats = null,
                                error = e.message ?: "Erreur inconnue",
                                period = p,
                                zoneId = z
                            )
                        )
                    }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = UiState()
            )

    fun setPeriod(period: Period) {
        if (_period.value != period) _period.value = period
    }

    fun setZoneId(zoneId: ZoneId) {
        if (_zoneId.value != zoneId) _zoneId.value = zoneId
    }

    private fun selectFlow(period: Period, zone: ZoneId) = when (period) {
        Period.TODAY    -> repo.statsToday(zone)
        Period.WEEK     -> repo.statsThisWeek(zone)
        Period.MONTH    -> repo.statsThisMonth(zone)
        Period.YEAR     -> repo.statsThisYear(zone)
        Period.ALL_TIME -> repo.statsAllTime(zone)
    }
}