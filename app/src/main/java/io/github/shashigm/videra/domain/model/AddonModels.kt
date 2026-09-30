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

data class Episode(
    val number: Int,
    val title: String,
    val streams: List<Stream>,
    val durationSeconds: Long? = null
)

data class MediaItem(
    val id: String,
    val title: String,
    val posterUrl: String?,
    val bannerUrl: String?,
    val type: MediaType,
    val streams: List<Stream> = emptyList(),
    val episodes: List<Episode> = emptyList()
)

data class Stream(
    val url: String,
    val quality: String?,
    val subtitles: List<String>
)

data class InstalledAddon(
    val id: String,
    val name: String,
    val manifestName: String,
    val baseUrl: String,
    val manifestUrl: String,
    val version: String,
    val mediaItemsUrl: String? = null,
    val customName: String? = null,
    val enabled: Boolean = true,
    val cachedMetadataJson: String? = null
)