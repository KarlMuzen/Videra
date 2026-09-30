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

    @Query("SELECT * FROM installed_addons WHERE id = :addonId LIMIT 1")
    suspend fun findById(addonId: String): InstalledAddonEntity?

    @Query("DELETE FROM installed_addons WHERE id = :addonId")
    suspend fun deleteById(addonId: String)

    @Query(
        "SELECT * FROM installed_addons " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeInstalledAddons(): Flow<List<InstalledAddonEntity>>

    @Query(
        "SELECT * FROM installed_addons " +
            "WHERE enabled = 1 " +
            "ORDER BY name COLLATE NOCASE ASC"
    )
    fun observeEnabledAddons(): Flow<List<InstalledAddonEntity>>

    @Query(
        "UPDATE installed_addons " +
            "SET enabled = :enabled WHERE id = :addonId"
    )
    suspend fun setEnabled(addonId: String, enabled: Boolean)

    @Query(
        "UPDATE installed_addons " +
            "SET customName = :customName WHERE id = :addonId"
    )
    suspend fun setCustomName(addonId: String, customName: String?)
}