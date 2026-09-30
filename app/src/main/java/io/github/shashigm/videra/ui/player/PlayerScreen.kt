package io.github.shashigm.videra.ui.player

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.shashigm.videra.media.player.PlayerPlaybackState
import io.github.shashigm.videra.ui.shorts.ShortsOverlay

@Composable
fun PlayerScreen(
    viewModel: PlayerViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val selection by viewModel.selection.collectAsStateWithLifecycle()
    val media = selection?.mediaItem
    val episodes = media?.episodes.orEmpty()

    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { if (media == null) 0 else 1 }
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (media != null) {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(0.dp),
                userScrollEnabled = false
            ) {
                PlayerArtworkPage()
            }

            PlayerSurface(
                player = viewModel.player,
                modifier = Modifier.fillMaxSize()
            )

            ShortsOverlay(
                title = media.title,
                addonName = selection?.addonName ?: "Micro-drama",
                episodes = episodes,
                currentEpisodeIndex = state.currentEpisodeIndex
                    ?.coerceIn(0, episodes.lastIndex.coerceAtLeast(0))
                    ?: 0,
                onPrevious = viewModel::previousEpisode,
                onNext = viewModel::nextEpisode,
                onEpisodeSelected = viewModel::selectEpisode
            )
        } else {
            Text(
                text = "No micro-drama selected.",
                modifier = Modifier.align(Alignment.Center),
                style = MaterialTheme.typography.headlineSmall,
                color = Color.White
            )
        }

        Surface(
            modifier = Modifier
                .align(Alignment.TopStart)
                .safeDrawingPadding()
                .padding(12.dp),
            shape = MaterialTheme.shapes.large,
            color = Color.Black.copy(alpha = 0.46f),
            contentColor = Color.White
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Outlined.ArrowBack,
                    contentDescription = "Back"
                )
            }
        }

        when (state.playbackState) {
            PlayerPlaybackState.LOADING,
            PlayerPlaybackState.BUFFERING -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            PlayerPlaybackState.ERROR -> {
                PlayerPlaybackError(
                    message = state.errorMessage
                        ?: "Video playback failed.",
                    attemptedStreamUrl = state.attemptedStreamUrl,
                    errorCodeName = state.errorCodeName,
                    errorCauseMessage = state.errorCauseMessage,
                    onRetry = viewModel::retry
                )
            }

            else -> Unit
        }
    }
}

@Composable
private fun PlayerArtworkPage(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    )
}

@Composable
private fun BoxScope.PlayerPlaybackError(
    message: String,
    attemptedStreamUrl: String?,
    errorCodeName: String?,
    errorCauseMessage: String?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(20.dp),
        color = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Playback error",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            SelectionContainer {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Stream URL: " +
                            (attemptedStreamUrl ?: "Unavailable"),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Media3 error code: " +
                            (errorCodeName ?: "Unavailable"),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Exception message: " +
                            (errorCauseMessage ?: "Unavailable"),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }
        }
    }
}
