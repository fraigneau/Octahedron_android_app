@file:Suppress("unused")
package com.octahedron.ui.screen


import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp


@Composable
fun VinylScreen() {
    SpinningVinyl(
        cover = painterResource(com.octahedron.R.drawable.logo),
        vinylRes = com.octahedron.R.drawable.vinyl,
        rpm = 45f,
        modifier = Modifier.size(300.dp)
    )
}


@Composable
fun SpinningVinyl(
    cover: Painter,
    modifier: Modifier = Modifier.size(260.dp),
    rpm: Float = 33.3f,
    @DrawableRes vinylRes: Int,
    coverDiameterFraction: Float = 0.32f
) {
    val durationMs = if (rpm <= 0f) Int.MAX_VALUE else (120_000f / rpm).toInt()
    val infinite = rememberInfiniteTransition(label = "vinyl-rotation")
    val angle by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "angle"
    )

    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Image(
            painter = cover,
            contentDescription = "Album cover",
            modifier = Modifier
                .fillMaxSize(coverDiameterFraction)
                .graphicsLayer { rotationZ = angle },
            contentScale = ContentScale.Crop
        )

        Image(
            painter = painterResource(vinylRes),
            contentDescription = "Vinyl record",
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { rotationZ = angle },
            contentScale = ContentScale.Fit
        )
    }
}
