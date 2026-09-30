package io.github.shashigm.videra.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.usecase.HomeFeed
import io.github.shashigm.videra.domain.usecase.HomeFeedSection

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onItemClick: (InstalledAddon, MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeContent(
        state = uiState,
        onRefresh = viewModel::refresh,
        onItemClick = onItemClick,
        modifier = modifier
    )
}

@Composable
private fun HomeContent(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onItemClick: (InstalledAddon, MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        HomeUiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        HomeUiState.Empty -> {
            EmptyHome(
                modifier = modifier,
                onRefresh = onRefresh
            )
        }

        is HomeUiState.Error -> {
            HomeError(
                message = state.message,
                modifier = modifier,
                onRefresh = onRefresh
            )
        }

        is HomeUiState.Success -> {
            HomeFeedList(
                feed = state.feed,
                modifier = modifier,
                onRefresh = onRefresh,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun HomeFeedList(
    feed: HomeFeed,
    onRefresh: () -> Unit,
    onItemClick: (InstalledAddon, MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            start = 16.dp,
            top = 20.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Micro-Drama",
                    style = MaterialTheme.typography.headlineMedium
                )

                IconButton(onClick = onRefresh) {
                    Icon(
                        imageVector = Icons.Outlined.Refresh,
                        contentDescription = "Refresh discover feed"
                    )
                }
            }
        }

        if (feed.failures.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "Some add-ons are unavailable",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Text(
                            text = feed.failures.joinToString(
                                separator = "\n"
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

        items(
            items = feed.sections,
            key = { section -> section.addon.id }
        ) { section ->
            HomeFeedSectionRow(
                section = section,
                onItemClick = onItemClick
            )
        }
    }
}

@Composable
private fun HomeFeedSectionRow(
    section: HomeFeedSection,
    onItemClick: (InstalledAddon, MediaItem) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = section.addon.name,
            style = MaterialTheme.typography.titleLarge
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 2.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = section.items,
                key = { item -> item.id }
            ) { item ->
                MediaItemCard(
                    item = item,
                    onClick = {
                        onItemClick(section.addon, item)
                    }
                )
            }
        }
    }
}

@Composable
private fun MediaItemCard(
    item: MediaItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.width(150.dp)
    ) {
        Column {
            if (item.posterUrl != null || item.bannerUrl != null) {
                AsyncImage(
                    model = item.posterUrl ?: item.bannerUrl,
                    contentDescription = item.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No artwork",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Micro-drama",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EmptyHome(
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "No micro-dramas yet",
                style = MaterialTheme.typography.headlineSmall
            )

            Text(
                text = "Install an add-on with a mediaItemsUrl that exposes MICRO_DRAMA items to populate Home.",
                style = MaterialTheme.typography.bodyMedium
            )

            Button(onClick = onRefresh) {
                Text("Refresh")
            }
        }
    }
}

@Composable
private fun HomeError(
    message: String,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Home unavailable",
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
