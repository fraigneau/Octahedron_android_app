package com.octahedron.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.octahedron.data.AppTheme

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun OctahedronTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {

    val context = LocalContext.current
    val dynamicLight = dynamicLightColorScheme(context)
    val dynamicDark  = dynamicDarkColorScheme(context)

    val colorScheme = when (appTheme) {
        AppTheme.SYSTEM -> if (isSystemInDarkTheme()) dynamicDark else dynamicLight
        AppTheme.DARK   -> darkColorScheme()
        AppTheme.LIGHT  -> lightColorScheme()
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}