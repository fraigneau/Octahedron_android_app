package com.octahedron.ui.helper

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import ir.ehsannarmani.compose_charts.models.Pie

@Composable
fun PieLegend(
    data: List<Pie>,
    modifier: Modifier = Modifier
) {
    val total = data.sumOf { it.data }.takeIf { it > 0 } ?: 1.0
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        data.forEach { slice ->
            val pct = (slice.data / total * 100.0)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    slice.label ?: "—",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(pct.toString(), style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}