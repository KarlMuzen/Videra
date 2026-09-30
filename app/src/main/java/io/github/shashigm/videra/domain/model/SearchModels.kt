package io.github.shashigm.videra.domain.model

data class SearchResult(
    val addon: InstalledAddon,
    val mediaItem: MediaItem
)

data class SearchAddonFailure(
    val addon: InstalledAddon,
    val message: String
)

data class SearchCatalog(
    val results: List<SearchResult>,
    val failures: List<SearchAddonFailure>
)
