package io.github.shashigm.videra.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "installed_addons")
data class InstalledAddonEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val baseUrl: String,
    val version: String,
    val mediaItemsUrl: String? = null
)
