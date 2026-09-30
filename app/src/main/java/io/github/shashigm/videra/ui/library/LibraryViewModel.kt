package io.github.shashigm.videra.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.repository.LibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState

    data class Success(
        val items: List<LibraryItem>
    ) : LibraryUiState
}

class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    val uiState: StateFlow<LibraryUiState> =
        repository
            .observeSavedItems()
            .map { items ->
                if (items.isEmpty()) {
                    LibraryUiState.Empty
                } else {
                    LibraryUiState.Success(items)
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = LibraryUiState.Loading
            )

    fun save(
        addon: InstalledAddon,
        mediaItem: MediaItem
    ) {
        viewModelScope.launch {
            try {
                repository.save(
                    addon = addon,
                    mediaItem = mediaItem
                )
            } catch (exception: CancellationException) {
                throw exception
            }
        }
    }

    fun remove(libraryKey: String) {
        viewModelScope.launch {
            try {
                repository.remove(libraryKey)
            } catch (exception: CancellationException) {
                throw exception
            }
        }
    }
}

class LibraryViewModelFactory(
    private val repository: LibraryRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LibraryViewModel::class.java)) {
            return LibraryViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
