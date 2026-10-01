package io.github.shashigm.videra.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.media3.common.Player
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.repository.EpisodeProgressRepository
import io.github.shashigm.videra.media.player.PlayerController
import io.github.shashigm.videra.media.player.PlayerEvent
import io.github.shashigm.videra.media.player.PlayerPlaybackState
import io.github.shashigm.videra.media.player.PlayerState
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class PlayerSelection(
    val addonId: String,
    val addonName: String,
    val mediaItem: MediaItem
)

class PlayerViewModel(
    private val playerController: PlayerController,
    private val addonRepository: AddonRepository,
    private val episodeProgressRepository: EpisodeProgressRepository
) : ViewModel() {

    val uiState: StateFlow<PlayerState> = playerController.state
    val player: Player = playerController.player

    private val _selection = MutableStateFlow<PlayerSelection?>(null)
    val selection: StateFlow<PlayerSelection?> = _selection.asStateFlow()

    private var selectionJob: Job? = null

    init {
        viewModelScope.launch {
            playerController.events.collect { event ->
                when (event) {
                    is PlayerEvent.EpisodeCompleted -> {
                        val selected = _selection.value
                        if (
                            selected != null &&
                            selected.mediaItem.id == event.mediaId
                        ) {
                            saveProgress(
                                addonId = selected.addonId,
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
                val selected = _selection.value
                if (
                    selected != null &&
                    state.playbackState == PlayerPlaybackState.PAUSED &&
                    state.currentEpisodeIndex != null &&
                    state.currentMedia?.id == selected.mediaItem.id
                ) {
                    saveCurrentProgress()
                }
            }
        }
    }

    fun play(
        addon: InstalledAddon,
        mediaItem: MediaItem
    ) {
        if (mediaItem.type != MediaType.MICRO_DRAMA) {
            return
        }

        val currentSelection = _selection.value
        if (
            currentSelection?.addonId == addon.id &&
            currentSelection.mediaItem.id == mediaItem.id &&
            playerController.state.value.currentMedia?.id == mediaItem.id
        ) {
            playerController.resume()
            return
        }

        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            saveCurrentProgress()
            startPlayback(
                addonId = addon.id,
                addonName = addon.name,
                mediaItem = mediaItem
            )
        }
    }

    fun playLibraryItem(item: LibraryItem) {
        if (item.mediaItem.type != MediaType.MICRO_DRAMA) {
            return
        }

        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            saveCurrentProgress()

            _selection.value = PlayerSelection(
                addonId = item.addonId,
                addonName = item.addonName,
                mediaItem = item.mediaItem
            )

            val installedAddon = addonRepository
                .observeInstalledAddons()
                .first()
                .firstOrNull { addon ->
                    addon.id == item.addonId
                }

            val resolvedMedia = if (installedAddon != null) {
                when (val result = addonRepository.getMediaItems(installedAddon)) {
                    is Resource.Success -> {
                        result.data.firstOrNull { media ->
                            media.id == item.mediaItem.id &&
                                media.type == MediaType.MICRO_DRAMA
                        }
                    }

                    Resource.Loading,
                    is Resource.Error -> null
                }
            } else {
                null
            }

            startPlayback(
                addonId = installedAddon?.id ?: item.addonId,
                addonName = installedAddon?.name ?: item.addonName,
                mediaItem = resolvedMedia ?: item.mediaItem
            )
        }
    }

    fun selectEpisode(index: Int) {
        val selected = _selection.value ?: return
        if (selected.mediaItem.episodes.isEmpty()) {
            return
        }

        selectionJob?.cancel()
        selectionJob = viewModelScope.launch {
            saveCurrentProgress()
            playerController.playEpisodes(
                media = selected.mediaItem,
                startEpisodeIndex = index
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

    fun pause() {
        playerController.pause()
    }

    fun resume() {
        playerController.resume()
    }

    fun retry() {
        playerController.retry()
    }

    fun pauseAndClearVideoSurface() {
        playerController.pauseAndClearVideoSurface()
    }

    private suspend fun startPlayback(
        addonId: String,
        addonName: String,
        mediaItem: MediaItem
    ) {
        try {
            if (mediaItem.type != MediaType.MICRO_DRAMA) {
                return
            }

            _selection.value = PlayerSelection(
                addonId = addonId,
                addonName = addonName,
                mediaItem = mediaItem
            )

            val progress = try {
                episodeProgressRepository.getProgress(
                    addonId = addonId,
                    mediaId = mediaItem.id
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (_: Exception) {
                null
            }

            if (mediaItem.episodes.isEmpty()) {
                playerController.playEpisodes(
                    media = mediaItem,
                    startPositionMs = progress?.positionMs ?: 0L
                )
                return
            }

            val startIndex = progress
                ?.let { saved ->
                    mediaItem.episodes.indexOfFirst { episode ->
                        episode.number == saved.lastWatchedEpisodeNumber
                    }
                }
                ?.takeIf { it >= 0 }
                ?: 0

            playerController.playEpisodes(
                media = mediaItem,
                startEpisodeIndex = startIndex,
                startPositionMs = progress?.positionMs ?: 0L
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            playerController.setPlaybackError(
                media = mediaItem,
                message = exception.message
                    ?.takeIf { it.isNotBlank() }
                    ?.let { "Unable to start playback: $it" }
                    ?: "Unable to start playback due to an unexpected error.",
                cause = exception
            )
        }
    }

    private suspend fun saveCurrentProgress() {
        val selected = _selection.value ?: return
        val state = playerController.state.value
        val media = state.currentMedia ?: return
        val episodeIndex = state.currentEpisodeIndex ?: return
        val episode = media.episodes.getOrNull(episodeIndex) ?: return

        if (
            media.id != selected.mediaItem.id ||
            state.totalEpisodeCount == 0
        ) {
            return
        }

        saveProgress(
            addonId = selected.addonId,
            mediaId = media.id,
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
        } catch (_: Exception) {
            // Keep playback functional if local progress persistence fails.
        }
    }
}

class PlayerViewModelFactory(
    private val playerController: PlayerController,
    private val addonRepository: AddonRepository,
    private val episodeProgressRepository: EpisodeProgressRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(
                playerController = playerController,
                addonRepository = addonRepository,
                episodeProgressRepository = episodeProgressRepository
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}
