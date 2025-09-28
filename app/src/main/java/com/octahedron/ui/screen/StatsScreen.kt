package com.octahedron.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.octahedron.repository.ListeningHistoryRepository
import com.octahedron.ui.helper.TopArtistsChart
import com.octahedron.ui.helper.TopTracksCard
import com.octahedron.ui.veiwmodel.StatsViewModel
import com.octahedron.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel) {

    val ui = vm.uiState.collectAsState().value

    Scaffold(
    ) { padding ->
        when {
            ui.isLoading -> Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }

            ui.error != null -> Box(
                Modifier
                    .padding(padding)
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(stringResource(R.string.error_with_message, ui.error))
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {  }) {
                        Text(stringResource(R.string.retry))
                    }
                }
            }

            else -> {
                val stats = ui.stats
                if (stats == null) {
                    Box(
                        Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                } else {
                    Content(
                        padding = padding,
                        stats = stats,
                        currentPeriod = ui.period,
                        onPeriodSelected = vm::setPeriod
                    )
                }
            }
        }
    }
}

@Composable
private fun Content(
    padding: PaddingValues,
    stats: ListeningHistoryRepository.PeriodStats,
    currentPeriod: StatsViewModel.Period,
    onPeriodSelected: (StatsViewModel.Period) -> Unit,
) {
    val nameOf = remember(stats) { ListeningHistoryRepository.makeArtistNameResolver(stats) }
    val top3ById = remember(stats) { ListeningHistoryRepository.makeTopTracksProviderByArtistId(stats, topN = 3) }

    Column(
        Modifier
            .padding(padding)
            .fillMaxSize()
    ) {
        PeriodTabs(
            current = currentPeriod,
            onSelect = onPeriodSelected
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 6.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {

            // --- KPI Cards ---
            item {
                MetricsGrid(
                    totalPlays = stats.totalPlays,
                    uniqueTracks = stats.uniqueTracks,
                    totalPlayTimeMs = stats.totalPlayTimeMs
                )
            }

            item { Spacer(Modifier.height(2.dp)) }

            // --- Top Artists ---
            item {
                TopArtistsChart(
                    artists = stats.topArtists,
                    totalArtiste = stats.totalArtists,
                    top3ProviderFrom = top3ById,
                    labelResolver = nameOf,
                )
            }

            item { Spacer(Modifier.height(2.dp)) }

            // --- Top Tracks ---
            item {
                TopTracksCard(
                    tracks = stats.topItems,
                )
            }
        }
    }
}

@Composable
private fun PeriodTabs(
    current: StatsViewModel.Period,
    onSelect: (StatsViewModel.Period) -> Unit
) {
    val periods = listOf(
        StatsViewModel.Period.TODAY to stringResource(R.string.period_day),
        StatsViewModel.Period.WEEK to stringResource(R.string.period_week),
        StatsViewModel.Period.MONTH to stringResource(R.string.period_month),
        StatsViewModel.Period.YEAR to stringResource(R.string.period_year),
        StatsViewModel.Period.ALL_TIME to stringResource(R.string.period_all)
    )
    val selectedIndex = periods.indexOfFirst { it.first == current }.coerceAtLeast(0)

    TabRow(selectedTabIndex = selectedIndex) {
        periods.forEachIndexed { idx, (p, label) ->
            Tab(
                selected = idx == selectedIndex,
                onClick = { onSelect(p) },
                text = { Text(label) }
            )
        }
    }
}

@Composable
private fun MetricsGrid(
    totalPlays: Int,
    uniqueTracks: Int,
    totalPlayTimeMs: Long
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.metric_plays),
                value = "%,d".format(totalPlays),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = stringResource(R.string.metric_unique_tracks),
                value = "%,d".format(uniqueTracks),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = stringResource(R.string.metric_listening_time),
                value = formatHms(totalPlayTimeMs),
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier
) {
    ElevatedCard(modifier) {
        Column(
            Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

private fun formatHms(totalMs: Long): String {
    val totalSec = (totalMs / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}