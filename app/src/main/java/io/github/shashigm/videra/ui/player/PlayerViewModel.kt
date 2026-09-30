package io.github.shashigm.videra.ui.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.Player
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.Stream
import io.github.shashigm.videra.media.player.PlayerController
import io.github.shashigm.videra.media.player.PlayerState
import kotlinx.coroutines.flow.StateFlow

class PlayerViewModel(
    private val playerController: PlayerController
) : ViewModel() {

    val uiState: StateFlow<PlayerState> = playerController.state

    val player: Player = playerController.player

    fun play(
        media: MediaItem,
        selectedStream: Stream? = null
    ) {
        val stream = selectedStream
            ?: media.streams.firstOrNull()
            ?: BIG_BUCK_BUNNY_STREAM

        playerController.play(
            media = media,
            stream = stream
        )
    }

    fun selectStream(stream: Stream) {
        val media = uiState.value.currentMedia ?: return

        play(
            media = media,
            selectedStream = stream
        )
    }

    fun pause() {
        playerController.pause()
    }

    fun togglePlayPause() {
        playerController.togglePlayPause()
    }

    fun retry() {
        playerController.retry()
    }

    fun dismiss() {
        playerController.dismiss()
    }

    private companion object {
        val BIG_BUCK_BUNNY_STREAM = Stream(
            url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
            quality = "Development test stream",
            subtitles = emptyList()
        )
    }
}

class PlayerViewModelFactory(
    private val playerController: PlayerController
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(playerController) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: \${modelClass.name}"
        )
    }
}
