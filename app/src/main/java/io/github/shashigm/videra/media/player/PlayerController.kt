package io.github.shashigm.videra.media.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import io.github.shashigm.videra.domain.model.Episode
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.Stream
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

enum class PlayerPlaybackState {
    IDLE,
    LOADING,
    PLAYING,
    PAUSED,
    BUFFERING,
    ERROR
}

data class PlayerState(
    val playbackState: PlayerPlaybackState = PlayerPlaybackState.IDLE,
    val currentMedia: MediaItem? = null,
    val currentStream: Stream? = null,
    val currentEpisodeIndex: Int? = null,
    val totalEpisodeCount: Int = 0,
    val currentPositionMs: Long = 0L,
    val errorMessage: String? = null,
    val attemptedStreamUrl: String? = null,
    val errorCodeName: String? = null,
    val errorCauseMessage: String? = null,
    val errorStackTrace: String? = null
)

sealed interface PlayerEvent {
    data class EpisodeCompleted(
        val mediaId: String,
        val episodeNumber: Int,
        val positionMs: Long
    ) : PlayerEvent
}

class PlayerController(
    context: Context
) {
    private val exoPlayer: ExoPlayer =
        ExoPlayer.Builder(context.applicationContext).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<PlayerEvent>(
        extraBufferCapacity = 16
    )
    val events: SharedFlow<PlayerEvent> = _events.asSharedFlow()

    private var currentMedia: MediaItem? = null
    private var currentStream: Stream? = null
    private var currentEpisodes: List<Episode> = emptyList()
    private var currentEpisodeIndex: Int? = null
    private var handledEndedEpisodeIndex: Int? = null
    private var released = false

    private val listener = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            syncState()
        }

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            syncState()
        }

        override fun onPlayerError(error: PlaybackException) {
            updatePlaybackError(error)
        }
    }

    init {
        exoPlayer.addListener(listener)
    }

    val player: Player
        get() = exoPlayer

    fun play(
        media: MediaItem,
        stream: Stream
    ) {
        if (released) {
            return
        }

        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        currentMedia = media
        currentStream = stream
        loadCurrentStream()
    }

    fun playEpisodes(
        media: MediaItem,
        startEpisodeIndex: Int = 0,
        episodes: List<Episode> = media.episodes,
        startPositionMs: Long = 0L
    ) {
        if (released) {
            return
        }

        if (episodes.isEmpty()) {
            play(
                media = media,
                stream = media.streams.firstOrNull() ?: BIG_BUCK_BUNNY_STREAM
            )
            return
        }

        val safeIndex = startEpisodeIndex.coerceIn(0, episodes.lastIndex)
        currentMedia = media
        currentEpisodes = episodes
        currentEpisodeIndex = safeIndex
        handledEndedEpisodeIndex = null
        currentStream = episodeStream(media, episodes[safeIndex])
        loadCurrentStream(startPositionMs = startPositionMs.coerceAtLeast(0L))
    }

    fun previousEpisode() {
        val index = currentEpisodeIndex ?: return
        if (index > 0) {
            playEpisodeAt(index - 1)
        }
    }

    fun nextEpisode() {
        val index = currentEpisodeIndex ?: return
        if (index < currentEpisodes.lastIndex) {
            playEpisodeAt(index + 1)
        }
    }

    fun pause() {
        if (!released) {
            exoPlayer.pause()
        }
    }

    fun togglePlayPause() {
        if (released) {
            return
        }

        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
    }

    fun dismiss() {
        if (released) {
            return
        }

        exoPlayer.clearMediaItems()
        currentMedia = null
        currentStream = null
        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        _state.value = PlayerState()
    }

    fun retry() {
        if (released) {
            return
        }

        val index = currentEpisodeIndex
        if (index != null && currentEpisodes.isNotEmpty()) {
            playEpisodeAt(index)
            return
        }

        if (currentMedia != null && currentStream != null) {
            loadCurrentStream()
        }
    }

    fun release() {
        if (released) {
            return
        }

        released = true
        exoPlayer.removeListener(listener)
        exoPlayer.release()

        currentMedia = null
        currentStream = null
        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        _state.value = PlayerState()
    }

    private fun playEpisodeAt(index: Int) {
        if (released || currentMedia == null || currentEpisodes.isEmpty()) {
            return
        }

        val safeIndex = index.coerceIn(0, currentEpisodes.lastIndex)
        currentEpisodeIndex = safeIndex
        handledEndedEpisodeIndex = null
        currentStream = episodeStream(
            currentMedia ?: return,
            currentEpisodes[safeIndex]
        )
        loadCurrentStream(startPositionMs = 0L)
    }

    private fun episodeStream(
        media: MediaItem,
        episode: Episode
    ): Stream {
        return episode.streams.firstOrNull()
            ?: media.streams.firstOrNull()
            ?: BIG_BUCK_BUNNY_STREAM
    }

    private fun loadCurrentStream(startPositionMs: Long = 0L) {
        val media = currentMedia ?: return
        val stream = currentStream ?: return
        val normalizedUrl = stream.url.trim()
        val uri = Uri.parse(normalizedUrl)
        val scheme = uri.scheme?.lowercase()

        if (normalizedUrl.isBlank() || scheme !in HTTP_SCHEMES) {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.ERROR,
                currentMedia = media,
                currentStream = stream,
                currentEpisodeIndex = currentEpisodeIndex,
                totalEpisodeCount = currentEpisodes.size,
                currentPositionMs = 0L,
                errorMessage = "Stream URL must use HTTP or HTTPS.",
                attemptedStreamUrl = normalizedUrl,
                errorCauseMessage = "Stream URL must use HTTP or HTTPS.",
                errorStackTrace = "Local stream validation failed before Media3 playback."
            )
            return
        }

        _state.value = PlayerState(
            playbackState = PlayerPlaybackState.LOADING,
            currentMedia = media,
            currentStream = stream,
            currentEpisodeIndex = currentEpisodeIndex,
            totalEpisodeCount = currentEpisodes.size,
            currentPositionMs = 0L
        )

        val episode = currentEpisodeIndex?.let(currentEpisodes::getOrNull)
        val title = episode?.let {
            media.title + " • EP. " + it.number
        } ?: media.title

        val mediaItem = Media3MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(startPositionMs.coerceAtLeast(0L))
        exoPlayer.play()
    }

    private fun updatePlaybackError(error: PlaybackException) {
        val media = currentMedia
        val stream = currentStream

        _state.value = PlayerState(
            playbackState = PlayerPlaybackState.ERROR,
            currentMedia = media,
            currentStream = stream,
            currentEpisodeIndex = currentEpisodeIndex,
            totalEpisodeCount = currentEpisodes.size,
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L),
            errorMessage = error.message
                ?.takeIf { it.isNotBlank() }
                ?: "Video playback failed.",
            attemptedStreamUrl = stream?.url?.trim(),
            errorCodeName = error.errorCodeName,
            errorCauseMessage = error.cause?.message ?: error.message,
            errorStackTrace = error.stackTraceToString()
        )
    }

    private fun syncState() {
        if (released) {
            return
        }

        val media = currentMedia ?: run {
            _state.value = PlayerState()
            return
        }

        val stream = currentStream
        val error = exoPlayer.playerError

        if (error != null) {
            updatePlaybackError(error)
            return
        }

        if (exoPlayer.playbackState == Player.STATE_ENDED) {
            handleEpisodeEnded(media)
            return
        }

        val playbackState = when (exoPlayer.playbackState) {
            Player.STATE_BUFFERING -> PlayerPlaybackState.BUFFERING
            Player.STATE_READY -> {
                if (exoPlayer.isPlaying) {
                    PlayerPlaybackState.PLAYING
                } else {
                    PlayerPlaybackState.PAUSED
                }
            }
            else -> PlayerPlaybackState.LOADING
        }

        _state.value = PlayerState(
            playbackState = playbackState,
            currentMedia = media,
            currentStream = stream,
            currentEpisodeIndex = currentEpisodeIndex,
            totalEpisodeCount = currentEpisodes.size,
            currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
        )
    }

    private fun handleEpisodeEnded(media: MediaItem) {
        val index = currentEpisodeIndex
        if (index == null || currentEpisodes.isEmpty()) {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.PAUSED,
                currentMedia = media,
                currentStream = currentStream,
                currentPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            )
            return
        }

        if (handledEndedEpisodeIndex == index) {
            return
        }
        handledEndedEpisodeIndex = index

        val episode = currentEpisodes[index]
        val knownDurationMs = episode.durationSeconds
            ?.coerceAtLeast(0L)
            ?.times(1000L)
            ?: 0L
        val completedPositionMs = maxOf(
            exoPlayer.currentPosition.coerceAtLeast(0L),
            knownDurationMs
        )

        _events.tryEmit(
            PlayerEvent.EpisodeCompleted(
                mediaId = media.id,
                episodeNumber = episode.number,
                positionMs = completedPositionMs
            )
        )

        if (index < currentEpisodes.lastIndex) {
            playEpisodeAt(index + 1)
        } else {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.PAUSED,
                currentMedia = media,
                currentStream = currentStream,
                currentEpisodeIndex = index,
                totalEpisodeCount = currentEpisodes.size,
                currentPositionMs = completedPositionMs
            )
        }
    }

    private companion object {
        val HTTP_SCHEMES = setOf("http", "https")
        val BIG_BUCK_BUNNY_STREAM = Stream(
            url = "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4",
            quality = "Development test stream",
            subtitles = emptyList()
        )
    }
}