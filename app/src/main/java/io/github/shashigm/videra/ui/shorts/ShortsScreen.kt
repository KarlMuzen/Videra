package io.github.shashigm.videra.ui.shorts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.shashigm.videra.domain.model.Episode
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
            viewModel = viewModel,
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
    viewModel: ShortsViewModel,
    playerViewModel: PlayerViewModel,
    modifier: Modifier = Modifier
) {
    val pagerState = rememberPagerState(
        initialPage = 0,
        pageCount = { items.size }
    )
    val playerState by playerViewModel.uiState.collectAsStateWithLifecycle()
    var showEpisodeSheet by remember { mutableStateOf(false) }

    LaunchedEffect(items.size) {
        if (items.isNotEmpty()) {
            pagerState.scrollToPage(
                pagerState.currentPage.coerceAtMost(items.lastIndex)
            )
        }
    }

    val activeItem = items.getOrNull(pagerState.currentPage)

    LaunchedEffect(
        activeItem?.addon?.id,
        activeItem?.mediaItem?.id
    ) {
        activeItem?.let(viewModel::selectMedia)
    }

    val playerMatchesActiveItem = activeItem != null &&
        playerState.currentMedia?.id == activeItem.mediaItem.id
    val episodes = activeItem?.mediaItem?.episodes.orEmpty()
    val currentEpisodeIndex = playerState.currentEpisodeIndex
        ?.coerceIn(0, episodes.lastIndex.coerceAtLeast(0))
        ?: 0

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
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
                    item = item.mediaItem,
                    showVideo = playerState.currentMedia?.id == item.mediaItem.id
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
                shape = RoundedCornerShape(50),
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
                item = item,
                episodeCount = episodes.size,
                currentEpisodeIndex = currentEpisodeIndex,
                onPrevious = {
                    viewModel.previousEpisode()
                },
                onNext = {
                    viewModel.nextEpisode()
                },
                onOpenEpisodes = {
                    showEpisodeSheet = true
                },
                previousEnabled = currentEpisodeIndex > 0,
                nextEnabled = episodes.isNotEmpty() &&
                    currentEpisodeIndex < episodes.lastIndex
            )
        }
    }

    if (showEpisodeSheet && activeItem != null && episodes.isNotEmpty()) {
        EpisodeBottomSheet(
            episodes = episodes,
            selectedIndex = currentEpisodeIndex,
            onDismiss = { showEpisodeSheet = false },
            onEpisodeSelected = { index ->
                viewModel.selectEpisode(activeItem, index)
                showEpisodeSheet = false
            }
        )
    }
}

@Composable
private fun ShortsOverlay(
    item: ShortsFeedItem,
    episodeCount: Int,
    currentEpisodeIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onOpenEpisodes: () -> Unit,
    previousEnabled: Boolean,
    nextEnabled: Boolean
) {
    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = item.mediaItem.title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = item.addon.name,
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OverlayControl(
                enabled = previousEnabled,
                contentDescription = "Previous episode",
                onClick = onPrevious,
                image = Icons.Outlined.SkipPrevious
            )

            OverlayControl(
                enabled = nextEnabled,
                contentDescription = "Next episode",
                onClick = onNext,
                image = Icons.Outlined.SkipNext
            )
        }

        if (episodeCount > 0) {
            Surface(
                onClick = onOpenEpisodes,
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.52f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "EP. " + (currentEpisodeIndex + 1) + " / " + episodeCount,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Icon(
                        imageVector = Icons.Outlined.KeyboardArrowDown,
                        contentDescription = "Choose episode"
                    )
                }
            }
        }
    }
}

@Composable
private fun OverlayControl(
    enabled: Boolean,
    contentDescription: String,
    onClick: () -> Unit,
    image: androidx.compose.ui.graphics.vector.ImageVector
) {
    IconButton(
        onClick = onClick,
        enabled = enabled
    ) {
        Surface(
            shape = RoundedCornerShape(50),
            color = if (enabled) {
                Color.Black.copy(alpha = 0.48f)
            } else {
                Color.Black.copy(alpha = 0.24f)
            }
        ) {
            Icon(
                imageVector = image,
                contentDescription = contentDescription,
                tint = Color.White.copy(alpha = if (enabled) 1f else 0.38f),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
private fun EpisodeBottomSheet(
    episodes: List<Episode>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onEpisodeSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Episodes",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(horizontal = 20.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 120.dp),
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = 16.dp,
                    top = 8.dp,
                    end = 16.dp,
                    bottom = 28.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = episodes,
                    key = { episode -> episode.number }
                ) { episode ->
                    val index = episodes.indexOf(episode)
                    Card(
                        onClick = { onEpisodeSelected(index) }
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "EP. " + episode.number,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (index == selectedIndex) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                }
                            )
                            Text(
                                text = episode.title,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
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
                text = if (failures.isEmpty()) "No Shorts yet" else "No Shorts available",
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
            TextButton(onClick = onRefresh) {
                Text("Retry")
            }
        }
    }
}