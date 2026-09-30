package io.github.shashigm.videra.ui.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.repository.EpisodeProgressRepository
import io.github.shashigm.videra.domain.usecase.GetShortsFeedUseCase
import io.github.shashigm.videra.domain.usecase.ShortsAddonFailure
import io.github.shashigm.videra.domain.usecase.ShortsFeed
import io.github.shashigm.videra.domain.usecase.ShortsFeedItem
import io.github.shashigm.videra.media.player.PlayerController
import io.github.shashigm.videra.media.player.PlayerEvent
import io.github.shashigm.videra.media.player.PlayerPlaybackState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface ShortsUiState {
    data object Loading : ShortsUiState

    data class Empty(
        val failures: List<ShortsAddonFailure> = emptyList()
    ) : ShortsUiState

    data class Success(
        val feed: ShortsFeed
    ) : ShortsUiState

    data class Error(
        val message: String
    ) : ShortsUiState
}

class ShortsViewModel(
    private val repository: AddonRepository,
    private val playerController: PlayerController,
    private val episodeProgressRepository: EpisodeProgressRepository,
    private val getShortsFeed: GetShortsFeedUseCase = GetShortsFeedUseCase(repository)
) : ViewModel() {

    private val refreshKey = MutableStateFlow(0)

    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private var selectionJob: Job? = null
    private var selectedAddonId: String? = null
    private var selectedMediaId: String? = null

    private val enabledAddons: StateFlow<List<InstalledAddon>> =
        repository
            .observeEnabledAddons()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            combine(enabledAddons, refreshKey) { addons, _ ->
                addons
            }.collectLatest { addons ->
                _uiState.value = ShortsUiState.Loading

                when (val result = getShortsFeed(addons)) {
                    Resource.Loading -> Unit

                    is Resource.Success -> {
                        val feed = result.data
                        _uiState.value = when {
                            feed.items.isNotEmpty() -> ShortsUiState.Success(feed)
                            feed.failures.isNotEmpty() -> ShortsUiState.Empty(feed.failures)
                            else -> ShortsUiState.Empty()
                        }
                    }

                    is Resource.Error -> {
                        _uiState.value = ShortsUiState.Error(
                            result.throwable.message
                                ?.takeIf { it.isNotBlank() }
                                ?: "Shorts content could not be loaded."
                        )
                    }
                }
            }
        }

        viewModelScope.launch {
            playerController.events.collect { event ->
                when (event) {
                    is PlayerEvent.EpisodeCompleted -> {
                        val addonId = selectedAddonId
                        if (addonId != null && selectedMediaId == event.mediaId) {
                            saveProgress(
                                addonId = addonId,
                                mediaId = event.mediaId,
                                episodeNumber = event.episodeNumber,
                                positionMs = event.positionMs
                            )
                        }
                    }
                }
            }
        }

        viewModelScope.launch {
            playerController.state.collectLatest { state ->
                if (
                    state.playbackState == PlayerPlaybackState.PAUSED &&
                    state.currentEpisodeIndex != null &&
                    state.currentMedia?.id == selectedMediaId
                ) {
                    saveCurrentProgress()
                }
            }
        }
    }

    fun refresh() {
        refreshKey.update { it + 1 }
    }

    fun selectMedia(item: ShortsFeedItem) {
        val sameItem = selectedAddonId == item.addon.id &&
            selectedMediaId == item.mediaItem.id
        if (sameItem && playerController.state.value.currentMedia?.id == item.mediaItem.id) {
            return
        }

        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            saveCurrentProgress()

            selectedAddonId = item.addon.id
            selectedMediaId = item.mediaItem.id

            val progress = episodeProgressRepository.getProgress(
                addonId = item.addon.id,
                mediaId = item.mediaItem.id
            )
            val startIndex = progress
                ?.let { saved ->
                    item.mediaItem.episodes.indexOfFirst { episode ->
                        episode.number == saved.lastWatchedEpisodeNumber
                    }
                }
                ?.takeIf { it >= 0 }
                ?: 0

            playerController.playEpisodes(
                media = item.mediaItem,
                startEpisodeIndex = startIndex
            )
        }
    }

    fun selectEpisode(
        item: ShortsFeedItem,
        episodeIndex: Int
    ) {
        if (item.mediaItem.episodes.isEmpty()) {
            return
        }

        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            saveCurrentProgress()

            selectedAddonId = item.addon.id
            selectedMediaId = item.mediaItem.id

            playerController.playEpisodes(
                media = item.mediaItem,
                startEpisodeIndex = episodeIndex
            )
        }
    }

    fun previousEpisode() {
        viewModelScope.launch {
            saveCurrentProgress()
            playerController.previousEpisode()
        }
    }

    fun nextEpisode() {
        viewModelScope.launch {
            saveCurrentProgress()
            playerController.nextEpisode()
        }
    }

    private suspend fun saveCurrentProgress() {
        val addonId = selectedAddonId ?: return
        val state = playerController.state.value
        val mediaId = state.currentMedia?.id ?: return
        val episodeIndex = state.currentEpisodeIndex ?: return
        val episode = state.currentMedia.episodes.getOrNull(episodeIndex) ?: return

        if (mediaId != selectedMediaId) {
            return
        }

        saveProgress(
            addonId = addonId,
            mediaId = mediaId,
            episodeNumber = episode.number,
            positionMs = state.currentPositionMs
        )
    }

    private suspend fun saveProgress(
        addonId: String,
        mediaId: String,
        episodeNumber: Int,
        positionMs: Long
    ) {
        try {
            episodeProgressRepository.saveProgress(
                addonId = addonId,
                mediaId = mediaId,
                episodeNumber = episodeNumber,
                positionMs = positionMs
            )
        } catch (exception: CancellationException) {
            throw exception
        }
    }
}

class ShortsViewModelFactory(
    private val repository: AddonRepository,
    private val playerController: PlayerController,
    private val episodeProgressRepository: EpisodeProgressRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShortsViewModel::class.java)) {
            return ShortsViewModel(
                repository = repository,
                playerController = playerController,
                episodeProgressRepository = episodeProgressRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}