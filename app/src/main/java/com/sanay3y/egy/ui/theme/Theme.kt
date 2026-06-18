package com.sanay3y.egy.ui.theme

import androidx.compose.foundation.LocalIndication
import androidx.compose.material3.*
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = Color.White,

    background = Background,
    onBackground = TextPrimary,

    surface = Surface,
    onSurface = TextPrimary,

    outline = Border
)

@Composable
fun Sanay3yAppTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalIndication provides androidx.compose.material3.ripple()
    ) {
        MaterialTheme(
            colorScheme = LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}