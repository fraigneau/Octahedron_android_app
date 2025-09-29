package com.octahedron.ui.helper

import androidx.compose.runtime.Composable

@Composable
fun ConnectionCard(
    ui: Esp32ConnectionUi,
    onPing: () -> Unit,
    onSendImage: () -> Unit,
    onReconnect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors()
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                StatusDot(ui.status)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = when (ui.status) {
                        ConnectionStatus.Connected -> "ESP32 connecté"
                        ConnectionStatus.Connecting -> "Connexion en cours…"
                        ConnectionStatus.Disconnected -> "Non connecté"
                    },
                    style = MaterialTheme.typography.titleMedium
                )
            }

            // Infos
            InfoLine(
                label = "Dernier ping",
                value = buildString {
                    append(ui.lastPingMs?.let { "$it ms" } ?: "—")
                    val at = formatInstantShort(ui.lastPingAt)
                    if (at != "—") append(" · $at")
                }
            )
            InfoLine(
                label = "Dernière image envoyée",
                value = buildString {
                    append(formatBytes(ui.lastImageBytes))
                    val at = formatInstantShort(ui.lastImageAt)
                    if (at != "—") append(" · $at")
                }
            )

            // Erreur éventuelle
            ui.errorMessage?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                FilledTonalButton(
                    onClick = onPing,
                    enabled = ui.status != ConnectionStatus.Connecting
                ) {
                    Icon(Icons.Outlined.NetworkPing, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Ping")
                }
                OutlinedButton(
                    onClick = onSendImage,
                    enabled = ui.status == ConnectionStatus.Connected
                ) {
                    Icon(Icons.Outlined.Image, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Envoyer une image")
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = onReconnect,
                    enabled = ui.status != ConnectionStatus.Connecting
                ) {
                    Icon(Icons.Outlined.Sync, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Reconnexion")
                }
            }
        }
    }
}

@Composable
private fun StatusDot(status: ConnectionStatus, size: Dp = 12.dp) {
    val color = when (status) {
        ConnectionStatus.Connected -> MaterialTheme.colorScheme.tertiary
        ConnectionStatus.Connecting -> MaterialTheme.colorScheme.secondary
        ConnectionStatus.Disconnected -> MaterialTheme.colorScheme.error
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
