package io.github.shashigm.videra.ui.shorts

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.SkipNext
import androidx.compose.material.icons.outlined.SkipPrevious
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.github.shashigm.videra.domain.model.Episode

@Composable
fun BoxScope.ShortsOverlay(
    title: String,
    addonName: String,
    episodes: List<Episode>,
    currentEpisodeIndex: Int,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onEpisodeSelected: (Int) -> Unit
) {
    var showEpisodeSheet by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .padding(bottom = 80.dp),
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
                    text = title,
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = addonName,
                    color = Color.White.copy(alpha = 0.78f),
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            OverlayControl(
                enabled = currentEpisodeIndex > 0,
                contentDescription = "Previous episode",
                onClick = onPrevious,
                image = Icons.Outlined.SkipPrevious
            )

            OverlayControl(
                enabled = episodes.isNotEmpty() &&
                    currentEpisodeIndex < episodes.lastIndex,
                contentDescription = "Next episode",
                onClick = onNext,
                image = Icons.Outlined.SkipNext
            )
        }

        if (episodes.isNotEmpty()) {
            Surface(
                onClick = { showEpisodeSheet = true },
                shape = RoundedCornerShape(50),
                color = Color.Black.copy(alpha = 0.52f),
                contentColor = Color.White
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = 14.dp,
                        vertical = 8.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "EP. " + (currentEpisodeIndex + 1) +
                            " / " + episodes.size,
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

    if (showEpisodeSheet && episodes.isNotEmpty()) {
        EpisodeBottomSheet(
            episodes = episodes,
            selectedIndex = currentEpisodeIndex,
            onDismiss = { showEpisodeSheet = false },
            onEpisodeSelected = { index ->
                onEpisodeSelected(index)
                showEpisodeSheet = false
            }
        )
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
                tint = Color.White.copy(
                    alpha = if (enabled) 1f else 0.38f
                ),
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EpisodeBottomSheet(
    episodes: List<Episode>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onEpisodeSelected: (Int) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )

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
