package com.octahedron.ui.helper

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.octahedron.R

@Composable
fun palette(): List<Color> {
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

@Composable
fun AlbumArt(
    bitmap: Bitmap?,
    sizeDp: Int = 56,
    fallbackIconRes: Int
) {
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
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(fallbackIconRes),
                    contentDescription = cd,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.size((sizeDp * 0.8).dp)
                )
            }
        }
    }
}