package io.github.shashigm.videra.ui.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.SearchAddonFailure
import io.github.shashigm.videra.domain.model.SearchCatalog
import io.github.shashigm.videra.domain.model.SearchResult
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.usecase.SearchMediaUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface SearchUiState {
    data object Loading : SearchUiState

    data class Success(
        val query: String = "",
        val results: List<SearchResult> = emptyList(),
        val failures: List<SearchAddonFailure> = emptyList()
    ) : SearchUiState

    data class Error(
        val message: String
    ) : SearchUiState
}

class SearchViewModel(
    private val repository: AddonRepository,
    private val searchMedia: SearchMediaUseCase = SearchMediaUseCase(repository)
) : ViewModel() {

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    private val refreshKey = MutableStateFlow(0)
    private val catalog = MutableStateFlow<Resource<SearchCatalog>>(
        Resource.Loading
    )

    private val installedAddons: StateFlow<List<InstalledAddon>> =
        repository
            .observeInstalledAddons()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptyList()
            )

    val uiState: StateFlow<SearchUiState> =
        combine(_query, catalog) { rawQuery, catalogResource ->
            val normalizedQuery = rawQuery.trim()

            when (catalogResource) {
                Resource.Loading -> SearchUiState.Loading

                is Resource.Error -> SearchUiState.Error(
                    catalogResource.throwable.message
                        ?.takeIf { it.isNotBlank() }
                        ?: "Search catalog could not be loaded."
                )

                is Resource.Success -> {
                    val results = if (normalizedQuery.isBlank()) {
                        emptyList()
                    } else {
                        catalogResource.data.results.filter { result ->
                            result.mediaItem.title.contains(
                                other = normalizedQuery,
                                ignoreCase = true
                            )
                        }
                    }

                    SearchUiState.Success(
                        query = rawQuery,
                        results = results,
                        failures = catalogResource.data.failures
                    )
                }
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = SearchUiState.Loading
        )

    init {
        viewModelScope.launch {
            combine(installedAddons, refreshKey) { addons, _ ->
                addons
            }.collectLatest { addons ->
                catalog.value = Resource.Loading

                try {
                    catalog.value = searchMedia(addons)
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    catalog.value = Resource.Error(exception)
                }
            }
        }
    }

    fun onQueryChange(value: String) {
        _query.value = value
    }

    fun refresh() {
        refreshKey.update { it + 1 }
    }
}

class SearchViewModelFactory(
    private val repository: AddonRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
