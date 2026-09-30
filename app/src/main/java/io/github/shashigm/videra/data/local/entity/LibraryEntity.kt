package io.github.shashigm.videra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "library_items")
data class LibraryEntity(
    @PrimaryKey
    val libraryKey: String,
    val addonId: String,
    val addonName: String,
    val mediaId: String,
    val title: String,
    val posterUrl: String?,
    val bannerUrl: String?,
    val mediaType: String,
    val streamUrl: String?,
    val streamQuality: String?,
    val savedAt: Long
)
