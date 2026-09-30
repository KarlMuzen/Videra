package io.github.shashigm.videra.domain.usecase

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.repository.AddonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

data class HomeFeedSection(
    val addon: InstalledAddon,
    val items: List<MediaItem>
)

data class HomeFeedFailure(
    val addon: InstalledAddon,
    val message: String
)

data class HomeFeed(
    val sections: List<HomeFeedSection>,
    val failures: List<HomeFeedFailure>
)

class GetHomeFeedUseCase(
    private val repository: AddonRepository
) {

    suspend operator fun invoke(
        addons: List<InstalledAddon>
    ): Resource<HomeFeed> {
        if (addons.isEmpty()) {
            return Resource.Success(
                HomeFeed(
                    sections = emptyList(),
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

        val sections = mutableListOf<HomeFeedSection>()
        val failures = mutableListOf<HomeFeedFailure>()

        outcomes.forEach { (addon, result) ->
            when (result) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    if (result.data.isNotEmpty()) {
                        sections += HomeFeedSection(
                            addon = addon,
                            items = result.data
                        )
                    }
                }

                is Resource.Error -> {
                    failures += HomeFeedFailure(
                        addon = addon,
                        message = result.throwable.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "This add-on could not provide home content."
                    )
                }
            }
        }

        if (sections.isEmpty() && failures.size == addons.size) {
            return Resource.Error(
                IllegalStateException(
                    "None of the installed add-ons could provide home content."
                )
            )
        }

        return Resource.Success(
            HomeFeed(
                sections = sections,
                failures = failures
            )
        )
    }
}
