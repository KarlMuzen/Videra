package io.github.shashigm.videra.domain.repository

import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import kotlinx.coroutines.flow.Flow

interface LibraryRepository {

    fun observeSavedItems(): Flow<List<LibraryItem>>

    suspend fun save(
        addon: InstalledAddon,
        mediaItem: MediaItem
    )

    suspend fun remove(libraryKey: String)
}
