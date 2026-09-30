package io.github.shashigm.videra.data.local.entity

import androidx.room.Entity

@Entity(tableName = "episode_progress")
data class EpisodeProgressEntity(
    @androidx.room.PrimaryKey
    val progressKey: String,
    val addonId: String,
    val mediaId: String,
    val lastWatchedEpisodeNumber: Int,
    val positionMs: Long,
    val updatedAt: Long
)