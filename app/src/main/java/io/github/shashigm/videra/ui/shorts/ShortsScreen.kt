package io.github.shashigm.videra.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.usecase.ShortsAddonFailure
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
        ShortsUiState.Loading -> {
            ShortsLoading(modifier)
        }

        is ShortsUiState.Empty -> {
            ShortsEmpty(
                failures = state.failures,
                onRefresh = viewModel::refresh,
                modifier = modifier
            )
        }

        is ShortsUiState.Error -> {
            ShortsError(
                message = state.message,
                onRefresh = viewModel::refresh,
                modifier = modifier
            )
        }

        is ShortsUiState.Success -> {
            ShortsPager(
                items = state.feed.items,
                failures = state.feed.failures,
                onRefresh = viewModel::refresh,
                playerViewModel = playerViewModel,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun ShortsPager(
    items: List<MediaItem>,
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

    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) {
            pagerState.scrollToPage(
                pagerState.currentPage.coerceAtMost(items.lastIndex)
            )
        }
    }

    val activeItem = items.getOrNull(pagerState.currentPage)
    val playerMatchesActiveItem = activeItem != null &&
        playerState.currentMedia == activeItem

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        if (playerMatchesActiveItem) {
            PlayerSurface(
                player = playerViewModel.player,
                modifier = Modifier.fillMaxSize()
            )
        }

        VerticalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(0.dp)
        ) { page ->
            items.getOrNull(page)?.let { item ->
                ShortItemPage(
                    item = item,
                    isActive = pagerState.currentPage == page,
                    showVideo = playerState.currentMedia == item,
                    playerViewModel = playerViewModel
                )
            }
        }

        if (playerMatchesActiveItem) {
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
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .safeDrawingPadding()
                            .padding(16.dp)
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
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodyMedium
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.End
        ) {
            Button(onClick = onRefresh) {
                Text("Refresh")
            }

            if (failures.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Some add-ons are unavailable",
                            style = MaterialTheme.typography.titleSmall
                        )

                        Text(
                            text = failures.joinToString(
                                separator = " • "
                            ) { failure ->
                                failure.addon.name + ": " + failure.message
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ShortItemPage(
    item: MediaItem,
    isActive: Boolean,
    showVideo: Boolean,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    LaunchedEffect(item.id, isActive) {
        if (isActive) {
            playerViewModel.play(item)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surface.copy(
                    alpha = if (showVideo) 0f else 1f
                )
            )
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
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No artwork",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(16.dp),
            color = MaterialTheme.colorScheme.inverseSurface,
            contentColor = MaterialTheme.colorScheme.inverseOnSurface
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.headlineSmall,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Micro-drama",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(
                    modifier = Modifier.height(2.dp)
                )

                Text(
                    text = if (item.streams.isEmpty()) {
                        "Development test stream"
                    } else {
                        "Swipe to change Shorts"
                    },
                    style = MaterialTheme.typography.bodySmall
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
            .background(MaterialTheme.colorScheme.background),
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

            if (failures.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = failures.joinToString(
                            separator = " • "
                        ) { failure ->
                            failure.addon.name + ": " + failure.message
                        },
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

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
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )

            Button(onClick = onRefresh) {
                Text("Retry")
            }
        }
    }
}
