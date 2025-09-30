package com.octahedron.ui.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.octahedron.data.bus.EspConnectionBus
import java.time.Instant

@Composable
fun ConnectionCard(
    ui: EspConnectionBus.Esp32ConnectionUi,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(ui.status, size = 14.dp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (ui.status) {
                        EspConnectionBus.ConnectionStatus.Connected -> "ESP32 connecté"
                        EspConnectionBus.ConnectionStatus.Connecting -> "Connexion en cours…"
                        EspConnectionBus.ConnectionStatus.Disconnected -> "Non connecté"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            InfoLine(
                label = "Dernier ping",
                value = buildString {
                    append(ui.lastPingMs?.let { "$it ms" } ?: "—")
                    val at = formatInstantShort(ui.lastPingAt)
                    if (at != "—") append(" · $at")
                }
            )

            InfoLine(
                label = "Dernière image",
                value = buildString {
                    append(ui.lastImageName ?: "—")
                    val at = formatInstantShort(ui.lastImageAt)
                    if (at != "—") append(" · $at")
                }
            )

            ui.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun StatusDot(status: EspConnectionBus.ConnectionStatus, size: Dp = 12.dp) {
    val color = when (status) {
        EspConnectionBus.ConnectionStatus.Connected -> Color(0xFF4CAF50)
        EspConnectionBus.ConnectionStatus.Connecting -> Color(0xFFFFC107)
        EspConnectionBus.ConnectionStatus.Disconnected -> Color(0xFFF44336)
    }
    Box(
        Modifier
            .size(size)
            .background(color, shape = CircleShape)
    )
}

@Composable
private fun InfoLine(label: String, value: String) {
    Row(
        Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

fun formatInstantShort(instant: Instant?): String {
    if (instant == null) return "—"
    val dt = java.time.ZonedDateTime.ofInstant(instant, java.time.ZoneId.systemDefault())
    val today = java.time.LocalDate.now()
    return if (dt.toLocalDate() == today) {
        "%02d:%02d:%02d".format(dt.hour, dt.minute, dt.second)
    } else {
        "${dt.dayOfMonth}/${dt.monthValue} %02d:%02d:%02d".format(dt.hour, dt.minute, dt.second)
    }
}