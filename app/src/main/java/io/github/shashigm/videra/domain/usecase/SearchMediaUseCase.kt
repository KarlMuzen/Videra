package io.github.shashigm.videra.domain.usecase

import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.model.SearchAddonFailure
import io.github.shashigm.videra.domain.model.SearchCatalog
import io.github.shashigm.videra.domain.model.SearchResult
import io.github.shashigm.videra.domain.repository.AddonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.supervisorScope

class SearchMediaUseCase(
    private val repository: AddonRepository
) {

    suspend operator fun invoke(
        addons: List<InstalledAddon>
    ): Resource<SearchCatalog> {
        if (addons.isEmpty()) {
            return Resource.Success(
                SearchCatalog(
                    results = emptyList(),
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

        val results = mutableListOf<SearchResult>()
        val failures = mutableListOf<SearchAddonFailure>()

        outcomes.forEach { (addon, result) ->
            when (result) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    results += result.data
                        .filter { mediaItem ->
                            mediaItem.type == MediaType.MICRO_DRAMA
                        }
                        .map { mediaItem ->
                            SearchResult(
                                addon = addon,
                                mediaItem = mediaItem
                            )
                        }
                }

                is Resource.Error -> {
                    failures += SearchAddonFailure(
                        addon = addon,
                        message = result.throwable.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "This add-on could not provide micro-drama search results."
                    )
                }
            }
        }

        if (results.isEmpty() && failures.size == addons.size) {
            return Resource.Error(
                IllegalStateException(
                    "None of the installed add-ons could provide micro-drama search results."
                )
            )
        }

        return Resource.Success(
            SearchCatalog(
                results = results,
                failures = failures
            )
        )
    }
}
