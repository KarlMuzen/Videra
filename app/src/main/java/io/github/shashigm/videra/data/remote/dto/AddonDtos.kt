package io.github.shashigm.videra.data.remote.dto

import kotlinx.serialization.Serializable

@Serializable
enum class MediaTypeDto {
    MOVIE,
    SERIES,
    ANIME,
    MICRO_DRAMA
}

@Serializable
data class AddonManifestDto(
    val id: String,
    val name: String,
    val version: String,
    val supportedTypes: List<MediaTypeDto>,
    val mediaItemsUrl: String? = null
)

@Serializable
data class MediaItemDto(
    val id: String,
    val title: String,
    val posterUrl: String? = null,
    val bannerUrl: String? = null,
    val type: MediaTypeDto
)

@Serializable
data class StreamDto(
    val url: String,
    val quality: String? = null,
    val subtitles: List<String> = emptyList()
)
