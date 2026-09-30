package io.github.shashigm.videra.media.player

import android.content.Context
import android.net.Uri
import androidx.media3.common.MediaItem as Media3MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.PlaybackException
import androidx.media3.exoplayer.ExoPlayer
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.Stream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    val errorMessage: String? = null,
    val attemptedStreamUrl: String? = null,
    val errorCodeName: String? = null,
    val errorCauseMessage: String? = null,
    val errorStackTrace: String? = null
)

class PlayerController(
    context: Context
) {
    private val exoPlayer: ExoPlayer =
        ExoPlayer.Builder(context.applicationContext).build()

    private val _state = MutableStateFlow(PlayerState())
    val state: StateFlow<PlayerState> = _state.asStateFlow()

    private var currentMedia: MediaItem? = null
    private var currentStream: Stream? = null
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

        val normalizedUrl = stream.url.trim()
        val uri = Uri.parse(normalizedUrl)
        val scheme = uri.scheme?.lowercase()

        if (normalizedUrl.isBlank() || scheme !in HTTP_SCHEMES) {
            currentMedia = media
            currentStream = stream
            _state.value = PlayerState(
                playbackState = PlayerPlaybackState.ERROR,
                currentMedia = media,
                currentStream = stream,
                errorMessage = "Stream URL must use HTTP or HTTPS.",
                attemptedStreamUrl = normalizedUrl,
                errorCauseMessage = "Stream URL must use HTTP or HTTPS.",
                errorStackTrace = "Local stream validation failed before Media3 playback."
            )
            return
        }

        currentMedia = media
        currentStream = stream

        _state.value = PlayerState(
            playbackState = PlayerPlaybackState.LOADING,
            currentMedia = media,
            currentStream = stream
        )

        val mediaItem = Media3MediaItem.Builder()
            .setUri(uri)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(media.title)
                    .build()
            )
            .build()

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.play()
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
        _state.value = PlayerState()
    }

    fun retry() {
        val media = currentMedia
        val stream = currentStream

        if (!released && media != null && stream != null) {
            play(
                media = media,
                stream = stream
            )
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
        _state.value = PlayerState()
    }

    private fun updatePlaybackError(error: PlaybackException) {
        val media = currentMedia
        val stream = currentStream

        _state.value = PlayerState(
            playbackState = PlayerPlaybackState.ERROR,
            currentMedia = media,
            currentStream = stream,
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

        val playbackState = when (exoPlayer.playbackState) {
            Player.STATE_BUFFERING -> {
                PlayerPlaybackState.BUFFERING
            }

            Player.STATE_READY -> {
                if (exoPlayer.isPlaying) {
                    PlayerPlaybackState.PLAYING
                } else {
                    PlayerPlaybackState.PAUSED
                }
            }

            Player.STATE_ENDED -> {
                PlayerPlaybackState.PAUSED
            }

            else -> {
                PlayerPlaybackState.LOADING
            }
        }

        _state.value = PlayerState(
            playbackState = playbackState,
            currentMedia = media,
            currentStream = stream
        )
    }

    private companion object {
        val HTTP_SCHEMES = setOf("http", "https")
    }
}
