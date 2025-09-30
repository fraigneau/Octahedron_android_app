package com.octahedron.ui.Card

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octahedron.R
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
    val titleText = stringResource(R.string.last_image_title)
    val emptyText = stringResource(R.string.no_image_yet)
    val untitled = stringResource(R.string.untitled)


    val metaSep = " — "
    val tailSep = " · "
    val datePattern = "dd/MM HH:mm:ss"
    val formatter = remember(datePattern) {
        DateTimeFormatter.ofPattern(datePattern).withZone(ZoneId.systemDefault())
    }

    ElevatedCard(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (lastImageName == null && lastImageAt == null && bitmap == null) {
                Text(
                    emptyText,
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
                            .let { m -> if (onOpen != null) m.clickable { onOpen() } else m },
                        contentAlignment = Alignment.Center
                    ) {
                        AlbumArt2(bitmap = bitmap, sizeDp = 72)
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = lastImageName ?: untitled,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            style = MaterialTheme.typography.titleSmall
                        )

                        val meta = listOfNotNull(
                            artist?.takeIf { it.isNotBlank() },
                            album?.takeIf { it.isNotBlank() }
                        ).joinToString(metaSep).ifBlank { null }

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
                        val whenTxt = lastImageAt?.let { formatter.format(it) }
                        val tail = listOfNotNull(duration, whenTxt)
                            .joinToString(tailSep)
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
    val cd = stringResource(R.string.album_art_cd)
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = cd,
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

private fun formatHmsCompact(totalMs: Long): String {
    val totalSec = max(0L, totalMs / 1000)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return if (h > 0) "%02d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
