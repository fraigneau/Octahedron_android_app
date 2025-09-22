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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octahedron.repository.ListeningHistoryRepository
import com.octahedron.ui.veiwmodel.StatsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel) {


// TODO: Implement stats of week, month, year maybe alltime and top artists
// TODO: Use a menu for selecting the period ! (week, month, year, alltime)
// TODO: TimePlay, Top Artists, Top Albums, Top Tracks, etc.

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
                    Text("Oups : ${ui.error}")
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = {  }) {
                        Text("Réessayer")
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
    onPeriodSelected: (StatsViewModel.Period) -> Unit
) {
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
            verticalArrangement = Arrangement.spacedBy(1.dp)
        ) {
            // --- KPI Cards ---
            item {
                MetricsGrid(
                    totalPlays = stats.totalPlays,
                    uniqueTracks = stats.uniqueTracks,
                    totalPlayTimeMs = stats.totalPlayTimeMs
                )
            }

            // --- Top Tracks ---
            item {
                SectionHeader("Top 10 titres")
            }
            itemsIndexed(stats.topTracks) { index, top ->
                RankedRow(
                    rank = index + 1,
                    title = top.item.title,                 // Track.title
                    subtitle = "Écoutes : ${top.playCount}",
                    trailing = durationOrBlank(top.item.duration)
                )
            }

            item { Spacer(Modifier.height(8.dp)) }

            // --- Top Artists ---
            item {
                SectionHeader("Top 10 artistes")
            }
            itemsIndexed(stats.topArtists) { index, top ->
                RankedRow(
                    rank = index + 1,
                    title = top.item.name,                  // Artist.name
                    subtitle = "Écoutes : ${top.playCount}",
                    trailing = null
                )
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun PeriodTabs(
    current: StatsViewModel.Period,
    onSelect: (StatsViewModel.Period) -> Unit
) {
    val periods = listOf(
        StatsViewModel.Period.TODAY to "Jour",
        StatsViewModel.Period.WEEK to "Semaine",
        StatsViewModel.Period.MONTH to "Mois",
        StatsViewModel.Period.YEAR to "Année",
        StatsViewModel.Period.ALL_TIME to "Tout"
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
                title = "Lectures",
                value = "%,d".format(totalPlays),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Titres uniques",
                value = "%,d".format(uniqueTracks),
                modifier = Modifier.weight(1f)
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatCard(
                title = "Temps d’écoute",
                value = formatHms(totalPlayTimeMs),
                modifier = Modifier.weight(1f)
            )
            StatCard(
                title = "Moy. par play",
                value = formatHmsSafe(
                    if (totalPlays > 0) totalPlayTimeMs / totalPlays else 0
                ),
                modifier = Modifier.weight(1f)
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
                .fillMaxWidth()
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun RankedRow(
    rank: Int,
    title: String,
    subtitle: String? = null,
    trailing: String? = null
) {
    ListItem(
        overlineContent = {
            Text(
                "#$rank",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        },
        headlineContent = {
            Text(
                title,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        supportingContent = if (subtitle != null) {
            { Text(subtitle) }
        } else null,
        trailingContent = if (trailing != null) {
            { Text(trailing, style = MaterialTheme.typography.labelLarge) }
        } else null
    )
}

private fun durationOrBlank(ms: Long?): String? =
    ms?.let { formatHms(it) }

private fun formatHmsSafe(ms: Long?): String =
    formatHms(ms ?: 0L)

private fun formatHms(totalMs: Long): String {
    val totalSec = (totalMs / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}