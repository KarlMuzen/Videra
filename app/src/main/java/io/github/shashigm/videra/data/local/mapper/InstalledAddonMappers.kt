package io.github.shashigm.videra.data.local.mapper

import io.github.shashigm.videra.data.local.entity.InstalledAddonEntity
import io.github.shashigm.videra.domain.model.InstalledAddon

fun InstalledAddonEntity.toDomain(): InstalledAddon {
    return InstalledAddon(
        id = id,
        name = name,
        baseUrl = baseUrl,
        version = version
    )
}
