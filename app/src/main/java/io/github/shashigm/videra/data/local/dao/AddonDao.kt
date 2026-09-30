package io.github.shashigm.videra.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AddonDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(addon: InstalledAddonEntity)

    @Query("DELETE FROM installed_addons WHERE id = :addonId")
    suspend fun deleteById(addonId: String)

    @Query(
        "SELECT * FROM installed_addons " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeInstalledAddons(): Flow<List<InstalledAddonEntity>>
}
