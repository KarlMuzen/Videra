package io.github.shashigm.videra.data.local.entity

import androidx.room.Entity

@Entity(tableName = "library_items")
data class LibraryEntity(
    @androidx.room.PrimaryKey
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
