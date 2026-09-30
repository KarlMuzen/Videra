package io.github.shashigm.videra.data.repository

import io.github.shashigm.videra.data.local.dao.AddonDao
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import io.github.shashigm.videra.data.local.mapper.toDomain
import io.github.shashigm.videra.data.remote.api.VideraAddonApi
import io.github.shashigm.videra.data.remote.mapper.toDomain
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.repository.AddonRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class AddonRepositoryImpl(
    private val api: VideraAddonApi,
    private val addonDao: AddonDao
) : AddonRepository {

    override fun observeInstalledAddons(): Flow<List<InstalledAddon>> {
        return addonDao
            .observeInstalledAddons()
            .map { entities ->
                entities.map { entity ->
                    entity.toDomain()
                }
            }
    }

    override suspend fun installAddon(
        baseUrl: String,
        manifestUrl: String
    ): Resource<AddonManifest> {
        val normalizedBaseUrl = baseUrl.trim().trimEnd('/')
        val normalizedManifestUrl = manifestUrl.trim()

        if (normalizedBaseUrl.isBlank()) {
            return Resource.Error(
                IllegalArgumentException("Add-on base URL cannot be blank.")
            )
        }

        if (normalizedManifestUrl.isBlank()) {
            return Resource.Error(
                IllegalArgumentException("Add-on manifest URL cannot be blank.")
            )
        }

        return try {
            val manifestDto = api.getManifest(normalizedManifestUrl)

            addonDao.insert(
                InstalledAddonEntity(
                    id = manifestDto.id,
                    name = manifestDto.name,
                    baseUrl = normalizedBaseUrl,
                    version = manifestDto.version
                )
            )

            Resource.Success(
                manifestDto.toDomain()
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun removeAddon(addonId: String) {
        addonDao.deleteById(addonId)
    }
}
