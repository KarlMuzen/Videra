package io.github.shashigm.videra.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

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

private val SystemLightScheme = lightColorScheme()
private val SystemDarkScheme = darkColorScheme()

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
    val context = LocalContext.current
    val systemScheme = when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && darkSystem ->
            androidx.compose.material3.dynamicDarkColorScheme(context)
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ->
            androidx.compose.material3.dynamicLightColorScheme(context)
        darkSystem -> SystemDarkScheme
        else -> SystemLightScheme
    }
    val scheme = when (appTheme.trim()) {
        "OLED_BLACK" -> OledBlackScheme
        "VELVET_RED" -> VelvetRedScheme
        else -> systemScheme
    }

    MaterialTheme(
        colorScheme = scheme,
        content = content
    )
}
