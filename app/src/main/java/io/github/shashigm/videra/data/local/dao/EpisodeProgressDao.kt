package io.github.shashigm.videra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.shashigm.videra.data.local.entity.EpisodeProgressEntity

@Dao
interface EpisodeProgressDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: EpisodeProgressEntity)

    @Query("SELECT * FROM episode_progress WHERE progressKey = :progressKey LIMIT 1")
    suspend fun find(progressKey: String): EpisodeProgressEntity?
}