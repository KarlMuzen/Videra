package io.github.shashigm.videra.data.local.mapper

import io.github.shashigm.videra.data.local.entity.LibraryEntity
import io.github.shashigm.videra.domain.model.Episode
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.model.Stream

private const val BIG_BUCK_BUNNY_URL =
    "https://storage.googleapis.com/exoplayer-test-media-0/BigBuckBunny_320x180.mp4"

fun LibraryEntity.toDomain(): LibraryItem {
    val mediaType = MediaType.entries.firstOrNull { type ->
        type.name == this.mediaType
    } ?: MediaType.MOVIE

    val streams = streamUrl
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?.let { url ->
            listOf(
                Stream(
                    url = url,
                    quality = streamQuality,
                    subtitles = emptyList()
                )
            )
        }
        ?: emptyList()

    val episodes = if (mediaType == MediaType.MICRO_DRAMA) {
        (1..5).map { number ->
            Episode(
                number = number,
                title = "Episode " + number,
                streams = listOf(
                    Stream(
                        url = BIG_BUCK_BUNNY_URL,
                        quality = "Development test stream",
                        subtitles = emptyList()
                    )
                ),
                durationSeconds = 60L
            )
        }
    } else {
        emptyList()
    }

    return LibraryItem(
        libraryKey = libraryKey,
        addonId = addonId,
        addonName = addonName,
        mediaItem = MediaItem(
            id = mediaId,
            title = title,
            posterUrl = posterUrl,
            bannerUrl = bannerUrl,
            type = mediaType,
            streams = streams,
            episodes = episodes
        ),
        savedAt = savedAt
    )
}
