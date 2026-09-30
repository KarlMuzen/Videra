package io.github.shashigm.videra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val VelvetRedScheme = darkColorScheme(
    primary = VelvetRedPrimary,
    onPrimary = OledBlackOnSurface,
    background = VelvetRedBackground,
    onBackground = VelvetRedOnSurface,
    surface = VelvetRedSurface,
    onSurface = VelvetRedOnSurface,
    surfaceVariant = ColorTokens.VelvetRedSurfaceVariant,
    onSurfaceVariant = ColorTokens.VelvetRedOnSurfaceVariant
)

private val OledBlackScheme = darkColorScheme(
    primary = OledBlackPrimary,
    onPrimary = OledBlackOnSurface,
    background = OledBlackBackground,
    onBackground = OledBlackOnSurface,
    surface = OledBlackSurface,
    onSurface = OledBlackOnSurface,
    surfaceVariant = ColorTokens.OledBlackSurfaceVariant,
    onSurfaceVariant = ColorTokens.OledBlackOnSurfaceVariant
)

private val SystemLightScheme = lightColorScheme(
    primary = VelvetRedPrimary,
    onPrimary = OledBlackOnSurface,
    background = androidx.compose.ui.graphics.Color(0xFFFFFBFF),
    onBackground = androidx.compose.ui.graphics.Color(0xFF201A1B),
    surface = androidx.compose.ui.graphics.Color(0xFFFFFBFF),
    onSurface = androidx.compose.ui.graphics.Color(0xFF201A1B)
)

private val SystemDarkScheme = darkColorScheme(
    primary = VelvetRedPrimary,
    onPrimary = OledBlackOnSurface,
    background = androidx.compose.ui.graphics.Color(0xFF201A1B),
    onBackground = androidx.compose.ui.graphics.Color(0xFFEAE0E0),
    surface = androidx.compose.ui.graphics.Color(0xFF201A1B),
    onSurface = androidx.compose.ui.graphics.Color(0xFFEAE0E0)
)

private object ColorTokens {
    val VelvetRedSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF35151B)
    val VelvetRedOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE5BFC5)
    val OledBlackSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF242424)
    val OledBlackOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCACACA)
}

@Composable
fun VideraTheme(
    appTheme: String,
    content: @Composable () -> Unit
) {
    val darkSystem = isSystemInDarkTheme()
    val scheme = when (appTheme.trim()) {
        "OLED_BLACK" -> OledBlackScheme
        "VELVET_RED" -> VelvetRedScheme
        else -> if (darkSystem) SystemDarkScheme else SystemLightScheme
    }

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
