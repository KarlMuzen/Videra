package io.github.shashigm.videra.ui.player

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shashigm.videra.media.player.PlayerPlaybackState

@Composable
fun MiniPlayer(
    viewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val isVisible = state.currentMedia != null &&
        state.playbackState != PlayerPlaybackState.IDLE &&
        state.playbackState != PlayerPlaybackState.ERROR

    if (!isVisible) {
        return
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .height(64.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = state.currentMedia?.title ?: "Now Playing",
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.titleSmall
            )

            IconButton(
                onClick = viewModel::togglePlayPause
            ) {
                Icon(
                    imageVector = if (
                        state.playbackState == PlayerPlaybackState.PLAYING ||
                        state.playbackState == PlayerPlaybackState.BUFFERING
                    ) {
                        Icons.Default.Pause
                    } else {
                        Icons.Default.PlayArrow
                    },
                    contentDescription = if (
                        state.playbackState == PlayerPlaybackState.PLAYING ||
                        state.playbackState == PlayerPlaybackState.BUFFERING
                    ) {
                        "Pause"
                    } else {
                        "Play"
                    }
                )
            }

            IconButton(
                onClick = viewModel::dismiss
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close player"
                )
            }
        }
    }
}
