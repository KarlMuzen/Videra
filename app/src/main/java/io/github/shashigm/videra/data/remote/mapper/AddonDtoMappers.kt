package io.github.shashigm.videra.data.remote.mapper

import io.github.shashigm.videra.data.remote.dto.AddonManifestDto
import io.github.shashigm.videra.data.remote.dto.EpisodeDto
import io.github.shashigm.videra.data.remote.dto.MediaItemDto
import io.github.shashigm.videra.data.remote.dto.MediaTypeDto
import io.github.shashigm.videra.data.remote.dto.StreamDto
import io.github.shashigm.videra.domain.model.AddonManifest
import io.github.shashigm.videra.domain.model.Episode
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
        supportedTypes = supportedTypes.map(MediaTypeDto::toDomain),
        mediaItemsUrl = mediaItemsUrl
    )
}

fun EpisodeDto.toDomain(): Episode {
    return Episode(
        number = number,
        title = title,
        streams = streams.map(StreamDto::toDomain),
        durationSeconds = durationSeconds
    )
}

fun MediaItemDto.toDomain(): MediaItem {
    val mediaType = type.toDomain()
    val explicitEpisodes = episodes
        .map(EpisodeDto::toDomain)
        .sortedBy(Episode::number)


        id = id,
        title = title,
        posterUrl = posterUrl,
        bannerUrl = bannerUrl,
        type = mediaType,
        streams = streams.map(StreamDto::toDomain),
        episodes = explicitEpisodes
    )
}

fun StreamDto.toDomain(): Stream {
    return Stream(
        url = url,
        quality = quality,
        subtitles = subtitles
    )
}
