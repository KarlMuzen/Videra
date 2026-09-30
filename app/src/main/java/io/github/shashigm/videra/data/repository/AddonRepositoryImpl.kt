package io.github.shashigm.videra.data.repository

import io.github.shashigm.videra.data.local.dao.AddonDao
import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import io.github.shashigm.videra.data.local.mapper.toDomain
import io.github.shashigm.videra.data.remote.api.VideraAddonApi
import io.github.shashigm.videra.data.remote.mapper.toDomain
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.Stream
import io.github.shashigm.videra.domain.repository.AddonRepository
import java.net.URI
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

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

        if (!isValidHttpUrl(normalizedBaseUrl)) {
            return Resource.Error(
                IllegalArgumentException(
                    "Add-on base URL must be a valid HTTP or HTTPS URL."
                )
            )
        }

        if (!isValidHttpUrl(normalizedManifestUrl)) {
            return Resource.Error(
                IllegalArgumentException(
                    "Add-on manifest URL must be a valid HTTP or HTTPS URL."
                )
            )
        }

        return try {
            val manifestDto = api.getManifest(normalizedManifestUrl)
            val resolvedMediaItemsUrl = manifestDto.mediaItemsUrl
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { endpoint ->
                    resolveUrl(normalizedManifestUrl, endpoint)
                }

            addonDao.insert(
                InstalledAddonEntity(
                    id = manifestDto.id,
                    name = manifestDto.name,
                    baseUrl = normalizedBaseUrl,
                    version = manifestDto.version,
                    mediaItemsUrl = resolvedMediaItemsUrl
                )
            )

            Resource.Success(
                manifestDto.toDomain().copy(
                    mediaItemsUrl = resolvedMediaItemsUrl
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Exception) {
            Resource.Error(exception)
        }
    }

    override suspend fun getMediaItems(
        addon: InstalledAddon
    ): Resource<List<MediaItem>> {
        val endpoint = addon.mediaItemsUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }

        if (endpoint == null || !isValidHttpUrl(endpoint)) {
            return Resource.Error(
                IllegalStateException(
                    "Add-on ${addon.name} does not provide a valid mediaItemsUrl."
                )
            )
        }

        return try {
            Resource.Success(
                api.getMediaItems(endpoint).map { item ->
                    item.toDomain().copy(
                        streams = item.streams.map { stream ->
                            Stream(
                                url = stream.url,
                                quality = stream.quality,
                                subtitles = stream.subtitles
                            )
                        }
                    )
                }
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

    private fun resolveUrl(
        baseUrl: String,
        endpoint: String
    ): String {
        return URI(baseUrl).resolve(endpoint).toString()
    }

    private fun isValidHttpUrl(url: String): Boolean {
        val parsed = url.toHttpUrlOrNull()
        return parsed?.scheme == "http" || parsed?.scheme == "https"
    }
}
