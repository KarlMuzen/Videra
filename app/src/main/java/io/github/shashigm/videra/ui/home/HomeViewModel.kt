package io.github.shashigm.videra.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.usecase.GetHomeFeedUseCase
import io.github.shashigm.videra.domain.usecase.HomeFeed
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data object Empty : HomeUiState

    data class Success(
        val feed: HomeFeed
    ) : HomeUiState

    data class Error(
        val message: String
    ) : HomeUiState
}

class HomeViewModel(
    private val repository: AddonRepository,
    private val getHomeFeed: GetHomeFeedUseCase = GetHomeFeedUseCase(repository)
) : ViewModel() {

    private val refreshKey = MutableStateFlow(0)
    private var cachedAddonIds: List<String>? = null
    private var cachedFeed: HomeFeed? = null
    private var cachedRefreshKey = Int.MIN_VALUE

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val enabledAddons: StateFlow<List<InstalledAddon>> =
        repository
            .observeEnabledAddons()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    init {
        viewModelScope.launch {
            combine(enabledAddons, refreshKey) { addons, refreshRequest ->
                addons to refreshRequest
            }.collectLatest { (addons, refreshRequest) ->
                val addonIds = addons.map { it.id }
                val cached = cachedFeed

                if (
                    cached != null &&
                    cachedAddonIds == addonIds &&
                    cachedRefreshKey == refreshRequest
                ) {
                    _uiState.value = when {
                        cached.sections.isNotEmpty() ||
                            cached.failures.isNotEmpty() -> {
                            HomeUiState.Success(cached)
                        }

                        else -> {
                            HomeUiState.Empty
                        }
                    }
                    return@collectLatest
                }

                _uiState.value = HomeUiState.Loading

                when (val result = getHomeFeed(addons)) {
                    Resource.Loading -> Unit

                    is Resource.Success -> {
                        val feed = result.data
                        cachedFeed = feed
                        cachedAddonIds = addonIds
                        cachedRefreshKey = refreshRequest
                        _uiState.value = when {
                            feed.sections.isNotEmpty() ||
                                feed.failures.isNotEmpty() -> {
                                HomeUiState.Success(feed)
                            }

                            else -> {
                                HomeUiState.Empty
                            }
                        }
                    }

                    is Resource.Error -> {
                        _uiState.value = HomeUiState.Error(
                            result.throwable.message
                                ?.takeIf { it.isNotBlank() }
                                ?: "Home content could not be loaded."
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

class HomeViewModelFactory(
    private val repository: AddonRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}