package io.github.shashigm.videra.data.repository

import io.github.shashigm.videra.data.local.dao.LibraryDao
import io.github.shashigm.videra.data.local.entity.LibraryEntity
import io.github.shashigm.videra.data.local.mapper.InstalledAddonLibraryKey
import io.github.shashigm.videra.data.local.mapper.toDomain
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class LibraryRepositoryImpl(
    private val libraryDao: LibraryDao
) : LibraryRepository {

    override fun observeSavedItems(): Flow<List<io.github.shashigm.videra.domain.model.LibraryItem>> {
        return libraryDao.observeSavedItems().map { entities ->
            entities.map { entity ->
                entity.toDomain()
            }
        }
    }

    override suspend fun save(
        addon: InstalledAddon,
        mediaItem: MediaItem
    ) {
        val stream = mediaItem.streams.firstOrNull()
        libraryDao.insert(
            LibraryEntity(
                libraryKey = InstalledAddonLibraryKey(
                    addonId = addon.id,
                    mediaId = mediaItem.id
                ),
                addonId = addon.id,
                addonName = addon.name,
                mediaId = mediaItem.id,
                title = mediaItem.title,
                posterUrl = mediaItem.posterUrl,
                bannerUrl = mediaItem.bannerUrl,
                mediaType = mediaItem.type.name,
                streamUrl = stream?.url,
                streamQuality = stream?.quality,
                savedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun remove(libraryKey: String) {
        libraryDao.deleteByKey(libraryKey)
    }
}
