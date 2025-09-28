package com.octahedron.ui.helper

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import com.octahedron.model.Artist
import com.octahedron.repository.ListeningHistoryRepository
import ir.ehsannarmani.compose_charts.PieChart
import ir.ehsannarmani.compose_charts.models.Pie
import kotlin.math.roundToInt
import com.octahedron.R


@Composable
fun TopArtistsChart(
    artists: List<ListeningHistoryRepository.TopItem<Artist>>,
    totalArtiste: Int,
    top3ProviderFrom: (artistName: Long) -> List<Pair<String, Int>>,
    maxSlices: Int = 6,
    modifier: Modifier = Modifier,
    labelResolver: (Long) -> String = { it.toString() }
) {
    val top = artists.take(maxSlices)
    val others = artists.drop(maxSlices).sumOf { it.playCount }
    val palette = donutPalette()
    val othersLabel = stringResource(R.string.others)

    val pies = buildList {
        top.forEachIndexed { i, t ->
            add(
                Pie(
                    data = t.playCount.toDouble(),
                    label = t.item.uid.toString(),
                    color = palette[i % palette.size]
                )
            )
        }
        if (others > 0) {
            add(
                Pie(
                    data = others.toDouble(),
                    label = othersLabel,
                    color = palette[top.size % palette.size].copy(alpha = 0.6f)
                )
            )
        }
    }

    ElevatedCard(modifier) {
        Column(
            Modifier.fillMaxWidth().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                stringResource(R.string.top_artists),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                Modifier.fillMaxWidth().height(260.dp),
                contentAlignment = Alignment.Center
            ) {
                DonutCard(
                    data = pies,
                    totalLabelTop = "%,d".format(totalArtiste),
                    totalLabelBottom = stringResource(R.string.artists_label),
                    ringWidth = 36.dp,
                    gapDegrees = 2.5f,
                    top3Provider = top3ProviderFrom,
                    labelResolver = labelResolver,
                )
            }
            PieChartLegend(
                pies = pies,
                total = totalArtiste,
                labelResolver = { lbl -> lbl.toLongOrNull()?.let(labelResolver) ?: lbl }
            )

        }
    }
}

@Composable
fun DonutCard(
    data: List<Pie>,
    totalLabelTop: String? = null,
    totalLabelBottom: String? = null,
    ringWidth: Dp = 28.dp,
    gapDegrees: Float = 2.5f,
    top3Provider: (artistId: Long) -> List<Pair<String, Int>> = { emptyList() },
    labelResolver: (Long) -> String = { it.toString() },
    modifier: Modifier = Modifier
        .fillMaxWidth()
        .height(260.dp)
        .padding(8.dp)
) {
    var selectedId by remember { mutableStateOf<Long?>(null) }

    val donutData = remember(data, ringWidth, selectedId) {
        data.map { p ->
            p.copy(
                style = Pie.Style.Stroke(ringWidth),
                selected = (p.label == selectedId?.toString())
            )
        }
    }

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val cs = MaterialTheme.colorScheme
        val minSide = min(maxWidth, maxHeight)
        val innerCircleSize = (minSide - (ringWidth * 2) - 8.dp).coerceAtLeast(0.dp)

        PieChart(
            data = donutData,
            spaceDegree = gapDegrees,
            onPieClick = { pie ->
                val id = pie.label?.toLongOrNull()
                selectedId = if (id == null) selectedId else if (selectedId == id) null else id
            },
            selectedScale = 1.08f,
            selectedPaddingDegree = 5f,
            style = Pie.Style.Stroke(ringWidth),
            modifier = Modifier.matchParentSize()
        )

        Box(
            Modifier
                .size(innerCircleSize)
                .clip(CircleShape)
                .background(cs.background)
                .pointerInput(Unit) { detectTapGestures { selectedId = null } },
            contentAlignment = Alignment.Center
        ) {
            if (selectedId == null) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    totalLabelTop?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    totalLabelBottom?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.labelLarge,
                            color = cs.onSurfaceVariant,
                            maxLines = 1
                        )
                    }
                }
            } else {
                val top3 = remember(selectedId) { top3Provider(selectedId!!) }
                val artistName = labelResolver(selectedId!!)

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(horizontal = 8.dp)
                ) {
                    Text(
                        artistName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    top3.take(3).forEach { (title, count) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "• $title",
                                style = MaterialTheme.typography.labelLarge,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "${count}×",
                                style = MaterialTheme.typography.labelLarge.copy(fontFeatureSettings = "tnum"),
                                softWrap = false
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PieChartLegend(
    pies: List<Pie>,
    total: Int,
    labelResolver: (String) -> String = { it }
) {
    val safeTotal = total.coerceAtLeast(1)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        pies.forEach { p ->
            val plays = p.data.roundToInt()
            val pct = (p.data / safeTotal * 100.0)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(10.dp)
                        .clip(CircleShape)
                        .background(p.color)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = labelResolver(p.label ?: "—"),
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.width(8.dp))
                Text("${"%.1f".format(pct)}%", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.width(8.dp))
                Text("%,d".format(plays), style = MaterialTheme.typography.labelMedium)
            }
        }
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