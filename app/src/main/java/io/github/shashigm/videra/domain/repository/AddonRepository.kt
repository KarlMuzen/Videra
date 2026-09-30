package io.github.shashigm.videra.domain.repository

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.InstalledAddon
import kotlinx.coroutines.flow.Flow

interface AddonRepository {

    fun observeInstalledAddons(): Flow<List<InstalledAddon>>

    suspend fun installAddon(
        baseUrl: String,
        manifestUrl: String = baseUrl
    ): Resource<AddonManifest>

    suspend fun removeAddon(addonId: String)
}
