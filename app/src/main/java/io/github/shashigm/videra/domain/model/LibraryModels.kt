package io.github.shashigm.videra.domain.model

data class LibraryItem(
    val libraryKey: String,
    val addonId: String,
    val addonName: String,
    val mediaItem: MediaItem,
    val savedAt: Long
)
