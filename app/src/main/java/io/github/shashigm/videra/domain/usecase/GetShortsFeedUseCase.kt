package io.github.shashigm.videra.domain.usecase

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.repository.AddonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

data class ShortsFeedItem(
    val addon: InstalledAddon,
    val mediaItem: MediaItem
)

data class ShortsFeed(
    val items: List<ShortsFeedItem>,
    val failures: List<ShortsAddonFailure>
)

data class ShortsAddonFailure(
    val addon: InstalledAddon,
    val message: String
)

class GetShortsFeedUseCase(
    private val repository: AddonRepository
) {
    suspend operator fun invoke(
        addons: List<InstalledAddon>
    ): Resource<ShortsFeed> {
        if (addons.isEmpty()) {
            return Resource.Success(
                ShortsFeed(
                    items = emptyList(),
                    failures = emptyList()
                )
            )
        }

        val outcomes = supervisorScope {
            addons.map { addon ->
                async {
                    addon to repository.getMediaItems(addon)
                }
            }.awaitAll()
        }

        val items = linkedMapOf<String, ShortsFeedItem>()
        val failures = mutableListOf<ShortsAddonFailure>()

        outcomes.forEach { (addon, result) ->
            when (result) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    result.data
                        .asSequence()
                        .filter { item ->
                            item.type == MediaType.MICRO_DRAMA
                        }
                        .forEach { item ->
                            items.putIfAbsent(
                                addon.id + ":" + item.id,
                                ShortsFeedItem(
                                    addon = addon,
                                    mediaItem = item
                                )
                            )
                        }
                }

                is Resource.Error -> {
                    failures += ShortsAddonFailure(
                        addon = addon,
                        message = result.throwable.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "This add-on could not provide Shorts content."
                    )
                }
            }
        }

        if (items.isEmpty() && failures.size == addons.size) {
            return Resource.Error(
                IllegalStateException(
                    "None of the installed add-ons could provide Shorts content."
                )
            )
        }

        return Resource.Success(
            ShortsFeed(
                items = items.values.toList(),
                failures = failures
            )
        )
    }
}
