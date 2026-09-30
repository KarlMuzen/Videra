package io.github.shashigm.videra.domain.repository

import io.github.shashigm.videra.domain.model.EpisodeProgress

interface EpisodeProgressRepository {

    suspend fun getProgress(
        addonId: String,
        mediaId: String
    ): EpisodeProgress?

    suspend fun saveProgress(
        addonId: String,
        mediaId: String,
        episodeNumber: Int,
        positionMs: Long
    )
}