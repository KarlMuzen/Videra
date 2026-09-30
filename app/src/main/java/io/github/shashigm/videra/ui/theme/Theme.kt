package io.github.shashigm.videra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import io.github.shashigm.videra.data.preferences.AppTheme

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

private object ColorTokens {
    val VelvetRedSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF3A121A)
    val VelvetRedOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFE7C1C7)
    val OledBlackSurfaceVariant = androidx.compose.ui.graphics.Color(0xFF242424)
    val OledBlackOnSurfaceVariant = androidx.compose.ui.graphics.Color(0xFFCACACA)
}

@Composable
fun VideraTheme(
    appTheme: AppTheme,
    content: @Composable () -> Unit
) {
    val colorScheme = when (appTheme) {
        AppTheme.VELVET_RED -> VelvetRedScheme
        AppTheme.OLED_BLACK -> OledBlackScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}
