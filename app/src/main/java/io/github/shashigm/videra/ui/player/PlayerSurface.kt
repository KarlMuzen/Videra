package io.github.shashigm.videra.ui.player

import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.Player
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView

@Composable
fun PlayerSurface(
    player: Player,
    modifier: Modifier = Modifier
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            PlayerView(context).apply {
                this.player = player
                useController = false
                resizeMode = AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                keepScreenOn = true
                importantForAccessibility = View.IMPORTANT_FOR_ACCESSIBILITY_YES
            }
        },
        onRelease = { playerView ->
            playerView.player = null
            playerView.keepScreenOn = false
        }
    )
}
