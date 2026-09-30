package io.github.shashigm.videra.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.usecase.ShortsAddonFailure
import io.github.shashigm.videra.domain.usecase.ShortsFeedItem
import io.github.shashigm.videra.media.player.PlayerPlaybackState
import io.github.shashigm.videra.ui.player.PlayerSurface
import io.github.shashigm.videra.ui.player.PlayerViewModel

@Composable
fun ShortsScreen(
    viewModel: ShortsViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        ShortsUiState.Loading -> ShortsLoading(modifier)

        is ShortsUiState.Empty -> ShortsEmpty(
            failures = state.failures,
            onRefresh = viewModel::refresh,
            modifier = modifier
        )

        is ShortsUiState.Error -> ShortsError(
            message = state.message,
            onRefresh = viewModel::refresh,
            modifier = modifier
        )

        is ShortsUiState.Success -> ShortsPager(
            items = state.feed.items,
            failures = state.feed.failures,
            onRefresh = viewModel::refresh,
            playerViewModel = playerViewModel,
            modifier = modifier
        )
    }
}

@Composable
private fun ShortsPager(
    items: List<ShortsFeedItem>,
    failures: List<ShortsAddonFailure>,
    onRefresh: () -> Unit,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    val selection by playerViewModel.selection.collectAsStateWithLifecycle()

    LaunchedEffect(
        pagerState.currentPage,
        items.size
    ) {
        items.getOrNull(pagerState.currentPage)?.let { item ->
            playerViewModel.play(
                addon = item.addon,
                mediaItem = item.mediaItem
            )
        }
    }

    val activeItem = items.getOrNull(pagerState.currentPage)
    val playerMatchesActiveItem = activeItem != null &&
        playerState.currentMedia?.id == activeItem.mediaItem.id &&
        selection?.addonId == activeItem.addon.id
    val episodes = activeItem?.mediaItem?.episodes.orEmpty()
    val currentEpisodeIndex = playerState.currentEpisodeIndex
        ?.coerceIn(0, episodes.lastIndex.coerceAtLeast(0))
        ?: 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) { page ->
            val item = items.getOrNull(page)
            if (item != null) {
                ShortItemPage(
                    item = item.mediaItem,
                    showVideo = playerState.currentMedia?.id == item.mediaItem.id
                )
            }
        }

        if (playerMatchesActiveItem) {
            PlayerSurface(
                player = playerViewModel.player,
                modifier = Modifier.fillMaxSize()
            )

            when (playerState.playbackState) {
                PlayerPlaybackState.LOADING,
                PlayerPlaybackState.BUFFERING -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }

                PlayerPlaybackState.ERROR -> {
                    Card(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(24.dp)
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
                                text = playerState.errorMessage
                                    ?: "Video playback failed.",
                                color = MaterialTheme.colorScheme.error
                            )
                            Button(onClick = playerViewModel::retry) {
                                Text("Retry")
                            }
                        }
                    }
                }

                else -> Unit
            }
        }

        IconButton(
            onClick = onRefresh,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .safeDrawingPadding()
                .padding(8.dp)
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = Color.Black.copy(alpha = 0.45f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Refresh,
                    contentDescription = "Refresh Shorts",
                    tint = Color.White
                )
            }
        }

        activeItem?.let { item ->
            ShortsOverlay(
                title = item.mediaItem.title,
                addonName = item.addon.name,
                episodes = episodes,
                currentEpisodeIndex = currentEpisodeIndex,
                onPrevious = playerViewModel::previousEpisode,
                onNext = playerViewModel::nextEpisode,
                onEpisodeSelected = playerViewModel::selectEpisode
            )
        }

        if (failures.isNotEmpty()) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .safeDrawingPadding()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                color = Color.Black.copy(alpha = 0.58f),
                contentColor = Color.White,
                shape = MaterialTheme.shapes.medium
            ) {
                Text(
                    text = failures.joinToString(separator = " • ") { failure ->
                        failure.addon.name + ": " + failure.message
                    },
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@Composable
private fun ShortItemPage(
    item: MediaItem,
    showVideo: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (!showVideo) {
            val artwork = item.bannerUrl ?: item.posterUrl
            if (artwork != null) {
                AsyncImage(
                    model = artwork,
                    contentDescription = item.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
private fun ShortsLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ShortsEmpty(
    failures: List<ShortsAddonFailure>,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (failures.isEmpty()) {
                    "No Shorts yet"
                } else {
                    "No Shorts available"
                },
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = if (failures.isEmpty()) {
                    "Install an add-on that returns MICRO_DRAMA media items to populate this feed."
                } else {
                    "No MICRO_DRAMA items were returned. Some add-ons also failed."
                },
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun ShortsError(
    message: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Shorts unavailable",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onRefresh) {
                Text("Retry")
            }
        }
    }
}
