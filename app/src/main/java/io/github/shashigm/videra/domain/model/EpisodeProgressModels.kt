package io.github.shashigm.videra.domain.model

data class EpisodeProgress(
    val addonId: String,
    val mediaId: String,
    val lastWatchedEpisodeNumber: Int,
    val positionMs: Long,
    val updatedAt: Long
)