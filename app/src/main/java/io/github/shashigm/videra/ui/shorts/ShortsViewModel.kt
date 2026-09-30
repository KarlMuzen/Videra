package io.github.shashigm.videra.ui.shorts

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.repository.AddonRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope

data class ShortsFeed(
    val items: List<MediaItem>,
    val failures: List<ShortsAddonFailure>
)

data class ShortsAddonFailure(
    val addon: InstalledAddon,
    val message: String
)

sealed interface ShortsUiState {
    data object Loading : ShortsUiState

    data class Empty(
        val failures: List<ShortsAddonFailure> = emptyList()
    ) : ShortsUiState

    data class Success(
        val feed: ShortsFeed
    ) : ShortsUiState

    data class Error(
        val message: String
    ) : ShortsUiState
}

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

        val items = linkedMapOf<String, MediaItem>()
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
                                item
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

class ShortsViewModel(
    private val repository: AddonRepository,
    private val getShortsFeed: GetShortsFeedUseCase =
        GetShortsFeedUseCase(repository)
) : ViewModel() {

    private val refreshKey = MutableStateFlow(0)

    private val _uiState = MutableStateFlow<ShortsUiState>(ShortsUiState.Loading)
    val uiState: StateFlow<ShortsUiState> = _uiState.asStateFlow()

    private val installedAddons: StateFlow<List<InstalledAddon>> =
        repository
            .observeInstalledAddons()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            combine(installedAddons, refreshKey) { addons, _ ->
                addons
            }.collectLatest { addons ->
                _uiState.value = ShortsUiState.Loading

                when (val result = getShortsFeed(addons)) {
                    Resource.Loading -> Unit

                    is Resource.Success -> {
                        val feed = result.data
                        _uiState.value = when {
                            feed.items.isNotEmpty() -> {
                                ShortsUiState.Success(feed)
                            }

                            feed.failures.isNotEmpty() -> {
                                ShortsUiState.Empty(feed.failures)
                            }

                            else -> {
                                ShortsUiState.Empty()
                            }
                        }
                    }

                    is Resource.Error -> {
                        _uiState.value = ShortsUiState.Error(
                            result.throwable.message
                                ?.takeIf { it.isNotBlank() }
                                ?: "Shorts content could not be loaded."
                        )
                    }
                }
            }
        }
    }

    fun refresh() {
        refreshKey.update { it + 1 }
    }
}

class ShortsViewModelFactory(
    private val repository: AddonRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ShortsViewModel::class.java)) {
            return ShortsViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}
