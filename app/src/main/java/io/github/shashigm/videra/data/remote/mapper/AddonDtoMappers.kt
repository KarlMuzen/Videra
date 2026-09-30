package io.github.shashigm.videra.data.remote.mapper

import io.github.shashigm.videra.data.remote.dto.AddonManifestDto
import io.github.shashigm.videra.data.remote.dto.MediaItemDto
import io.github.shashigm.videra.data.remote.dto.MediaTypeDto
import io.github.shashigm.videra.data.remote.dto.StreamDto
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.model.Stream

fun MediaTypeDto.toDomain(): MediaType {
    return when (this) {
        MediaTypeDto.MOVIE -> MediaType.MOVIE
        MediaTypeDto.SERIES -> MediaType.SERIES
        MediaTypeDto.ANIME -> MediaType.ANIME
        MediaTypeDto.MICRO_DRAMA -> MediaType.MICRO_DRAMA
    }
}

fun AddonManifestDto.toDomain(): AddonManifest {
    return AddonManifest(
        id = id,
        name = name,
        version = version,
        supportedTypes = supportedTypes.map(MediaTypeDto::toDomain)
    )
}

fun MediaItemDto.toDomain(): MediaItem {
    return MediaItem(
        id = id,
        title = title,
        posterUrl = posterUrl,
        bannerUrl = bannerUrl,
        type = type.toDomain()
    )
}

fun StreamDto.toDomain(): Stream {
    return Stream(
        url = url,
        quality = quality,
        subtitles = subtitles
    )
}
