package io.github.shashigm.videra.media.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.AudioAttributes
import androidx.media3.common.C
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import io.github.shashigm.videra.domain.model.Episode
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.Stream
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
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
    private val applicationContext = context.applicationContext

    private var exoPlayer: ExoPlayer = createExoPlayer()

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
    private var playbackScope = createPlaybackScope()
    private var progressPollingJob: Job? = null

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
        configureAudioFocus()
        exoPlayer.addListener(listener)
        startProgressPolling()
    }

    val player: Player
        get() = exoPlayer

    fun playEpisodes(
        media: MediaItem,
        startEpisodeIndex: Int = 0,
        episodes: List<Episode> = media.episodes,
        startPositionMs: Long = 0L
    ) {
        if (released) {
            reinitializePlayer()
        }

        if (episodes.isEmpty()) {
            val stream = media.streams.firstOrNull()
            if (stream == null) {
                _state.value = PlayerState(
                    playbackState = PlayerPlaybackState.ERROR,
                    currentMedia = media,
                    errorMessage = "No streams or episodes available for this title."
                )
                return
            }

            playSingle(
                media = media,
                stream = stream,
                startPositionMs = startPositionMs
            )
            return
        }

        val safeIndex = startEpisodeIndex.coerceIn(0, episodes.lastIndex)
        val stream = episodeStream(media, episodes[safeIndex])
        if (stream == null) {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.ERROR,
                currentMedia = media,
                currentEpisodeIndex = safeIndex,
                totalEpisodeCount = episodes.size,
                errorMessage = "No stream is available for this episode or title."
            )
            return
        }

        currentMedia = media
        currentEpisodes = episodes
        currentEpisodeIndex = safeIndex
        handledEndedEpisodeIndex = null
        currentStream = stream
        loadCurrentStream(
            startPositionMs = startPositionMs.coerceAtLeast(0L)
        )
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

    fun resume() {
        if (!released) {
            exoPlayer.play()
        }
    }

    fun pauseAndClearVideoSurface() {
        if (!released) {
            exoPlayer.pause()
            exoPlayer.clearVideoSurface()
        }
    }

    fun clearVideoSurface() {
        if (!released) {
            exoPlayer.clearVideoSurface()
        }
    }

    fun setPlaybackError(
        media: MediaItem,
        message: String,
        cause: Throwable? = null
    ) {
        _state.value = PlayerState(
            playbackState = PlayerPlaybackState.ERROR,
            currentMedia = media,
            currentStream = currentStream,
            currentEpisodeIndex = currentEpisodeIndex,
            totalEpisodeCount = currentEpisodes.size,
            currentPositionMs = if (released) 0L else exoPlayer.currentPosition.coerceAtLeast(0L),
            errorMessage = message,
            attemptedStreamUrl = currentStream?.url?.trim(),
            errorCauseMessage = cause?.message ?: cause?.javaClass?.simpleName,
            errorStackTrace = cause?.stackTraceToString()
        )
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

        val media = currentMedia
        val stream = currentStream
        if (media != null && stream != null) {
            playSingle(
                media = media,
                stream = stream,
                startPositionMs = exoPlayer.currentPosition.coerceAtLeast(0L)
            )
        }
    }

    fun dismiss() {
        if (released) {
            return
        }

        pauseAndClearVideoSurface()
        exoPlayer.clearMediaItems()
        currentMedia = null
        currentStream = null
        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        _state.value = PlayerState()
    }

    fun release() {
        if (released) {
            return
        }

        released = true
        progressPollingJob?.cancel()
        progressPollingJob = null
        playbackScope.cancel()
        exoPlayer.removeListener(listener)
        exoPlayer.release()

        currentMedia = null
        currentStream = null
        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        _state.value = PlayerState()
    }

    private fun createExoPlayer(): ExoPlayer {
        val httpDataSourceFactory = androidx.media3.datasource.DefaultHttpDataSource.Factory()
            .setUserAgent("Videra/1.0 (Android; Mobile)")
            .setConnectTimeoutMs(15_000)
            .setReadTimeoutMs(20_000)
            .setAllowCrossProtocolRedirects(true)

        val mediaSourceFactory =
            androidx.media3.exoplayer.source.DefaultMediaSourceFactory(applicationContext)
                .setDataSourceFactory(httpDataSourceFactory)

        return ExoPlayer.Builder(applicationContext)
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
    }

    private fun configureAudioFocus() {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.AUDIO_CONTENT_TYPE_MOVIE)
            .build()

        exoPlayer.setAudioAttributes(audioAttributes, true)
    }

    private fun createPlaybackScope(): CoroutineScope {
        return CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    }

    private fun startProgressPolling() {
        progressPollingJob?.cancel()
        progressPollingJob = playbackScope.launch {
            while (isActive) {
                delay(1000L)
                if (!released && exoPlayer.isPlaying) {
                    syncState()
                }
            }
        }
    }

    private fun reinitializePlayer() {
        playbackScope = createPlaybackScope()
        exoPlayer = createExoPlayer()
        configureAudioFocus()
        exoPlayer.addListener(listener)
        released = false
        _state.value = PlayerState()
        startProgressPolling()
    }

    private fun playEpisodeAt(index: Int) {
        if (released || currentMedia == null || currentEpisodes.isEmpty()) {
            return
        }

        val safeIndex = index.coerceIn(0, currentEpisodes.lastIndex)
        currentEpisodeIndex = safeIndex
        handledEndedEpisodeIndex = null

        val media = currentMedia ?: return
        val stream = episodeStream(media, currentEpisodes[safeIndex])
        if (stream == null) {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.ERROR,
                currentMedia = media,
                currentEpisodeIndex = safeIndex,
                totalEpisodeCount = currentEpisodes.size,
                errorMessage = "No stream is available for this episode or title."
            )
            return
        }

        currentStream = stream
        loadCurrentStream(startPositionMs = 0L)
    }

    private fun playSingle(
        media: MediaItem,
        stream: Stream,
        startPositionMs: Long
    ) {
        if (stream.url.isBlank()) {
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.ERROR,
                currentMedia = media,
                currentStream = stream,
                errorMessage = "Stream URL is blank for this title.",
                attemptedStreamUrl = stream.url
            )
            return
        }

        currentMedia = media
        currentEpisodes = emptyList()
        currentEpisodeIndex = null
        handledEndedEpisodeIndex = null
        currentStream = stream
        loadCurrentStream(
            startPositionMs = startPositionMs.coerceAtLeast(0L)
        )
    }

    private fun episodeStream(
        media: MediaItem,
        episode: Episode
    ): Stream? {
        return episode.streams.firstOrNull()
            ?: media.streams.firstOrNull()
    }

    private fun loadCurrentStream(
        startPositionMs: Long = 0L
    ) {
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
            currentPositionMs = startPositionMs.coerceAtLeast(0L)
        )

        val episode = currentEpisodeIndex?.let(currentEpisodes::getOrNull)
        val title = episode?.let {
            media.title + " • EP. " + it.number
        } ?: media.title

        val mimeType = when {
            normalizedUrl.contains(".m3u8", ignoreCase = true) ->
                androidx.media3.common.MimeTypes.APPLICATION_M3U8

            normalizedUrl.contains(".mp4", ignoreCase = true) ->
                androidx.media3.common.MimeTypes.APPLICATION_MP4

            else -> null
        }

        val mediaItemBuilder = Media3MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .build()
            )

        if (mimeType != null) {
            mediaItemBuilder.setMimeType(mimeType)
        }

        val mediaItem = mediaItemBuilder.build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()

        if (startPositionMs > 0L) {
            exoPlayer.seekTo(startPositionMs.coerceAtLeast(0L))
        }

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
        val completedPositionMs = 0L

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

    }
}
