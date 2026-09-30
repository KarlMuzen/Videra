package io.github.shashigm.videra.data.local.mapper

import io.github.shashigm.videra.data.local.entity.LibraryEntity
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.model.Stream

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
            streams = streams
        ),
        savedAt = savedAt
    )
}

fun LibraryItem.toEntity(): LibraryEntity {
    val stream = mediaItem.streams.firstOrNull()

    return LibraryEntity(
        libraryKey = libraryKey,
        addonId = addonId,
        addonName = addonName,
        mediaId = mediaItem.id,
        title = mediaItem.title,
        posterUrl = mediaItem.posterUrl,
        bannerUrl = mediaItem.bannerUrl,
        mediaType = mediaItem.type.name,
        streamUrl = stream?.url,
        streamQuality = stream?.quality,
        savedAt = savedAt
    )
}

fun InstalledAddonLibraryKey(
    addonId: String,
    mediaId: String
): String = "$addonId::$mediaId"
