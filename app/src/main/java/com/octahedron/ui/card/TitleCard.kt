package com.octahedron.ui.card

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.octahedron.data.relation.ListeningWithTrackAndArtistsAndAlbum
import com.octahedron.data.repository.ListeningHistoryRepository


@Composable
fun TopTracksCard(
    tracks: List<ListeningHistoryRepository.TopItem<ListeningWithTrackAndArtistsAndAlbum>>,
    maxItems: Int = 10,
    modifier: Modifier = Modifier
) {
    val items = tracks.take(maxItems)

    ElevatedCard(modifier) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Top titres",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items.forEachIndexed { index, top ->
                    val data = top.item
                    RankedRow(
                        rank = index + 1,
                        title = data.track.title,
                        subtitle = "${data.artists.firstOrNull()?.name ?: "Inconnu"} — ${data.album.name}",
                        trailing = durationOrBlank(data.track.duration),
                        playCount = top.playCount,
                        leadingContent = { AlbumArt(bitmap = data.album.cover, sizeDp = 56) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Divider(modifier = Modifier.padding(horizontal = 12.dp))
                }
            }
        }
    }
}



@Composable
fun RankedRow(
    rank: Int,
    title: String,
    subtitle: String? = null,
    trailing: String? = null,
    playCount: Int? = null,
    leadingContent: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    ListItem(
        modifier = modifier,
        colors = ListItemDefaults.colors(containerColor = Color.Transparent),
        leadingContent = leadingContent,
        overlineContent = {
            Text(
                "#$rank",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.labelMedium
            )
        },
        headlineContent = {
            Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        supportingContent = subtitle?.let {
            { Text(it, maxLines = 2, overflow = TextOverflow.Ellipsis) }
        },
        trailingContent = {
            Column(horizontalAlignment = Alignment.End) {
                trailing?.let {
                    Text(it, style = MaterialTheme.typography.labelLarge)
                }
                playCount?.let {
                    Text(
                        "$it écoutes",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    )
}

@Composable
fun AlbumArt(bitmap: Bitmap?, sizeDp: Int = 56) {
    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = "Pochette d'album",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp))
        )
    } else {
        Box(
            Modifier
                .size(sizeDp.dp)
                .clip(RoundedCornerShape(8.dp)),
        ) { }
    }
}

private fun durationOrBlank(ms: Long?): String? =
    ms?.let { formatHms(it) }

private fun formatHms(totalMs: Long): String {
    val totalSec = (totalMs / 1000).coerceAtLeast(0)
    val h = totalSec / 3600
    val m = (totalSec % 3600) / 60
    val s = totalSec % 60
    return "%02d:%02d:%02d".format(h, m, s)
}