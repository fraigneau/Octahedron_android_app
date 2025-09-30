package com.octahedron.ui.helper

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.math.max

@Composable
fun LastImageCard(
    lastImageName: String?,
    lastImageAt: Instant?,
    bitmap: Bitmap?,
    artist: String? = null,
    album: String? = null,
    durationMs: Long? = null,
    modifier: Modifier = Modifier,
    onOpen: (() -> Unit)? = null
) {
    ElevatedCard(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Dernière image envoyée",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (lastImageName == null && lastImageAt == null && bitmap == null) {
                Text(
                    "Aucune image envoyée pour le moment",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .let { m ->
                                if (onOpen != null) m.clickable { onOpen() } else m
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        AlbumArt2(bitmap = bitmap, sizeDp = 72)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = lastImageName ?: "Sans nom",
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall
                        )

                        val meta = listOfNotNull(
                            artist?.takeIf { it.isNotBlank() },
                            album?.takeIf { it.isNotBlank() }
                        ).joinToString(" — ").ifBlank { null }

                        meta?.let {
                            Text(
                                text = it,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val duration = durationMs?.let { formatHmsCompact(it) }
                        val whenTxt = formatInstantExact(lastImageAt)
                        val tail = listOfNotNull(duration, whenTxt).joinToString(" · ")
                            .ifBlank { null }

                        tail?.let {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun AlbumArt2(bitmap: Bitmap?, sizeDp: Int = 56) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Pochette / vignette",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        Surface(
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp)),
            tonalElevation = 2.dp
        ) {}
    }
}

private val exactFormatter: DateTimeFormatter by lazy {
    DateTimeFormatter.ofPattern("dd/MM HH:mm:ss").withZone(ZoneId.systemDefault())
}

fun formatInstantExact(instant: Instant?): String? =
    instant?.let { exactFormatter.format(it) }

private fun formatHmsCompact(totalMs: Long): String {
    val totalSec = max(0L, totalMs / 1000)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
