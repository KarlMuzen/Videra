package io.github.shashigm.videra.domain.repository

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface AddonRepository {

    fun observeInstalledAddons(): Flow<List<InstalledAddon>>

    fun observeEnabledAddons(): Flow<List<InstalledAddon>>

    suspend fun installAddon(
        baseUrl: String,
        manifestUrl: String = baseUrl
    ): Resource<AddonManifest>

    suspend fun getMediaItems(
        addon: InstalledAddon
    ): Resource<List<MediaItem>>

    suspend fun setAddonEnabled(
        addonId: String,
        enabled: Boolean
    )

    suspend fun setCustomName(
        addonId: String,
        customName: String?
    )

    suspend fun removeAddon(addonId: String)
}