package com.octahedron.ui.screen

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.octahedron.ui.helper.ConnectionCard
import com.octahedron.ui.helper.LastImageCard
import com.octahedron.ui.helper.TimeCard
import com.octahedron.ui.veiwmodel.HomeViewModel
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.PopupProperties
import java.time.Instant
import java.time.format.TextStyle
import java.util.Locale


@Composable
fun HomeScreen(vm: HomeViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        ConnectionSection(vm)
        LastImageSection(vm)
        WeeklyChartSection(vm)
    }
}

@Composable
private fun ConnectionSection(vm: HomeViewModel) {
    val connUi by vm.ui.collectAsState()
    ConnectionCard(
        ui = connUi,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
private fun LastImageSection(vm: HomeViewModel) {
    val playUi by vm.lastPlayed.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LastImageCard(
        lastImageName = playUi.title,
        lastImageAt = Instant.now(),
        artist = playUi.artist,
        album = playUi.album,
        durationMs = playUi.durationMs,
        bitmap = playUi.bitmap,
        modifier = Modifier.fillMaxWidth(),
        onOpen = { showDialog = true },
    )

    if (showDialog && playUi.bitmap != null) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Image(
                        bitmap = playUi.bitmap!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeklyChartSection(vm: HomeViewModel) {
    val ui by vm.uiState.collectAsState()

    TimeCard(
        title = "Écoute cette semaine",
        subtitle = formatHm(ui.weekTotalMs),
        leading = {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
        },
        footer = {
            Text(
                "Lun → Dim",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Max/jour : ${formatHm(ui.days.maxOfOrNull { it.totalPlayTimeMs } ?: 0)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        onClick = { }
    ) {
        val dayLabels = remember(ui.days) {
            ui.days.map {
                it.date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
            }
        }
        val valuesMs = remember(ui.days) { ui.days.map { it.totalPlayTimeMs } }

        WeeklyListeningChart(
            dayLabels = dayLabels,
            valuesMs = valuesMs,
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )
    }
}

@Composable
private fun WeeklyListeningChart(
    dayLabels: List<String>,
    valuesMs: List<Long>,
    modifier: Modifier = Modifier,
    palette: List<Color> = donutPalette(),
    highlightToday: Boolean = true,
) {
    fun msToMinutes(ms: Long) = (ms / 60000.0)
    fun formatHm(ms: Long): String {
        val totalSec = ms / 1000
        val h = totalSec / 3600
        val m = (totalSec % 3600) / 60
        return if (h > 0) "${h}h ${m}min" else "${m}min"
    }

    if (dayLabels.isEmpty() || valuesMs.isEmpty()) {
        Text(
            "Aucune donnée cette semaine",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        return
    }

    val todayIdx = (java.time.LocalDate.now().dayOfWeek.value - 1)
        .coerceIn(0, dayLabels.lastIndex)

    val data = dayLabels.zip(valuesMs).mapIndexed { idx, (label, ms) ->
        val barColor = if (highlightToday && idx == todayIdx)
            SolidColor(MaterialTheme.colorScheme.tertiary)
        else
            SolidColor(palette[idx % palette.size])

        Bars(
            label = label,
            values = listOf(
                Bars.Data(
                    label = label,
                    value = msToMinutes(ms),
                    color = barColor
                )
            )
        )
    }

    Column(modifier) {
        ColumnChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            data = data,
            barProperties = BarProperties(
                thickness = 18.dp,
                spacing = 10.dp,
                cornerRadius = Bars.Data.Radius.Rectangle(topLeft = 8.dp, topRight = 8.dp),
                style = DrawStyle.Fill
            ),
            gridProperties = GridProperties(
                xAxisProperties = GridProperties.AxisProperties(
                    enabled = false,
                    lineCount = dayLabels.size.coerceAtLeast(1),
                    color = SolidColor(MaterialTheme.colorScheme.onSurface)
                ),
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = false,
                    lineCount = 4,
                    color = SolidColor(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f))
                )
            ),
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                labels = dayLabels
            ),
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = false
            ),
            popupProperties = PopupProperties(
                enabled = true,
                containerColor = MaterialTheme.colorScheme.surface,
                textStyle = MaterialTheme.typography.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                contentBuilder = { dataIndex, _, valueMinutes ->
                    val ms = (valueMinutes * 60_000).toLong()
                    "${dayLabels[dataIndex]} • ${formatHm(ms)}"
                }
            ),
            labelHelperProperties = LabelHelperProperties(
                enabled = false
            )
        )
    }
}

@Composable
fun donutPalette(): List<Color> {
    return listOf(
        Color(0xFF66C2A5),
        Color(0xFFFC8D62),
        Color(0xFF8DA0CB),
        Color(0xFFE78AC3),
        Color(0xFFA6D854),
        Color(0xFFFFD92F),
        Color(0xFFE5C494),
    )
}
private fun formatHm(ms: Long): String {
    val totalSec = ms / 1000
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    return if (h > 0) "${h}h ${m}min" else "${m}min"
}