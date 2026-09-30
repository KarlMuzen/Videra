package io.github.shashigm.videra.data.repository

import io.github.shashigm.videra.data.local.dao.EpisodeProgressDao
import io.github.shashigm.videra.data.local.entity.EpisodeProgressEntity
import io.github.shashigm.videra.domain.model.EpisodeProgress
import io.github.shashigm.videra.domain.repository.EpisodeProgressRepository

class EpisodeProgressRepositoryImpl(
    private val dao: EpisodeProgressDao
) : EpisodeProgressRepository {

    override suspend fun getProgress(
        addonId: String,
        mediaId: String
    ): EpisodeProgress? {
        return dao.find(progressKey(addonId, mediaId))?.let { entity ->
            EpisodeProgress(
                addonId = entity.addonId,
                mediaId = entity.mediaId,
                lastWatchedEpisodeNumber = entity.lastWatchedEpisodeNumber,
                positionMs = entity.positionMs,
                updatedAt = entity.updatedAt
            )
        }
    }

    override suspend fun saveProgress(
        addonId: String,
        mediaId: String,
        episodeNumber: Int,
        positionMs: Long
    ) {
        require(addonId.isNotBlank()) { "Add-on id cannot be blank." }
        require(mediaId.isNotBlank()) { "Media id cannot be blank." }
        require(episodeNumber > 0) { "Episode number must be positive." }

        dao.upsert(
            EpisodeProgressEntity(
                progressKey = progressKey(addonId, mediaId),
                addonId = addonId,
                mediaId = mediaId,
                lastWatchedEpisodeNumber = episodeNumber,
                positionMs = positionMs.coerceAtLeast(0L),
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    private fun progressKey(
        addonId: String,
        mediaId: String
    ): String = addonId + "::" + mediaId
}