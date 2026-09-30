package io.github.shashigm.videra.domain.model

enum class MediaType {
    MOVIE,
    SERIES,
    ANIME,
    MICRO_DRAMA
}

data class AddonManifest(
    val id: String,
    val name: String,
    val version: String,
    val supportedTypes: List<MediaType>,
    val mediaItemsUrl: String? = null
)

data class MediaItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val bannerUrl: String?,
    val type: MediaType
)

data class Stream(
    val url: String,
    val quality: String?,
    val subtitles: List<String>
)

data class InstalledAddon(
    val id: String,
    val name: String,
    val baseUrl: String,
    val version: String,
    val mediaItemsUrl: String? = null
)
