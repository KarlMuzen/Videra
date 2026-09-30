package io.github.shashigm.videra.data.local.mapper

import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import io.github.shashigm.videra.domain.model.InstalledAddon

fun InstalledAddonEntity.toDomain(): InstalledAddon {
    val effectiveName = customName
        ?.trim()
        ?.takeIf { it.isNotBlank() }
        ?: name

    return InstalledAddon(
        id = id,
        name = effectiveName,
        manifestName = name,
        baseUrl = baseUrl,
        manifestUrl = manifestUrl,
        version = version,
        mediaItemsUrl = mediaItemsUrl,
        customName = customName,
        enabled = enabled,
        cachedMetadataJson = cachedMetadataJson
    )
}