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
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull

class AddonRepositoryImpl(
    private val api: VideraAddonApi,
    private val addonDao: AddonDao
) : AddonRepository {

    override fun observeInstalledAddons(): Flow<List<InstalledAddon>> {
        return addonDao
            .observeInstalledAddons()
            .map { entities ->
                entities.map(InstalledAddonEntity::toDomain)
            }
    }

    override fun observeEnabledAddons(): Flow<List<InstalledAddon>> {
        return addonDao
            .observeEnabledAddons()
            .map { entities ->
                entities.map(InstalledAddonEntity::toDomain)
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
            validateManifest(manifestDto, normalizedManifestUrl)

            val resolvedMediaItemsUrl = manifestDto.mediaItemsUrl
                ?.trim()
                ?.takeIf { it.isNotBlank() }
                ?.let { endpoint ->
                    resolveUrl(normalizedManifestUrl, endpoint)
                }

            val existing = addonDao.findById(manifestDto.id)
            val cachedMetadataJson = Json {
                explicitNulls = false
            }.encodeToString(manifestDto)

            addonDao.insert(
                InstalledAddonEntity(
                    id = manifestDto.id,
                    name = manifestDto.name.trim(),
                    baseUrl = normalizedBaseUrl,
                    manifestUrl = normalizedManifestUrl,
                    version = manifestDto.version.trim(),
                    mediaItemsUrl = resolvedMediaItemsUrl,
                    customName = existing?.customName,
                    enabled = existing?.enabled ?: true,
                    cachedMetadataJson = cachedMetadataJson
                )
            )

            Resource.Success(
                manifestDto.toDomain().copy(
                    name = manifestDto.name.trim(),
                    version = manifestDto.version.trim(),
                    mediaItemsUrl = resolvedMediaItemsUrl
                )
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: SerializationException) {
            Resource.Error(exception)
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
                    "Add-on " + addon.name + " does not provide a valid mediaItemsUrl."
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

    override suspend fun setAddonEnabled(addonId: String, enabled: Boolean) {
        if (addonId.isBlank()) {
            throw IllegalArgumentException("Add-on id cannot be blank.")
        }
        if (addonDao.findById(addonId) == null) {
            throw IllegalStateException("Add-on is no longer installed.")
        }
        addonDao.setEnabled(addonId, enabled)
    }

    override suspend fun setCustomName(
        addonId: String,
        customName: String?
    ) {
        if (addonId.isBlank()) {
            throw IllegalArgumentException("Add-on id cannot be blank.")
        }
        if (addonDao.findById(addonId) == null) {
            throw IllegalStateException("Add-on is no longer installed.")
        }
        val normalizedName = customName
            ?.trim()
            ?.takeIf { it.isNotBlank() }
        addonDao.setCustomName(addonId, normalizedName)
    }

    override suspend fun removeAddon(addonId: String) {
        if (addonId.isBlank()) {
            throw IllegalArgumentException("Add-on id cannot be blank.")
        }
        addonDao.deleteById(addonId)
    }

    private fun validateManifest(
        manifest: io.github.shashigm.videra.data.remote.dto.AddonManifestDto,
        manifestUrl: String
    ) {
        if (manifest.id.isBlank()) {
            throw IllegalArgumentException("Add-on manifest id cannot be blank.")
        }
        if (manifest.name.isBlank()) {
            throw IllegalArgumentException("Add-on manifest name cannot be blank.")
        }
        if (manifest.version.isBlank()) {
            throw IllegalArgumentException("Add-on manifest version cannot be blank.")
        }
        if (manifest.supportedTypes.isEmpty()) {
            throw IllegalArgumentException("Add-on manifest must declare at least one supported media type.")
        }

        manifest.mediaItemsUrl
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { endpoint ->
                val resolved = resolveUrl(manifestUrl, endpoint)
                if (!isValidHttpUrl(resolved)) {
                    throw IllegalArgumentException(
                        "Add-on mediaItemsUrl must resolve to a valid HTTP or HTTPS URL."
                    )
                }
            }
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