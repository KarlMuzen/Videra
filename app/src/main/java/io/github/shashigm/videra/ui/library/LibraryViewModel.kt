package io.github.shashigm.videra.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.model.LibraryItem
import io.github.shashigm.videra.domain.model.MediaItem
import io.github.shashigm.videra.domain.model.MediaType
import io.github.shashigm.videra.domain.repository.LibraryRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface LibraryUiState {
    data object Loading : LibraryUiState
    data object Empty : LibraryUiState

    data class Success(
        val items: List<LibraryItem>
    ) : LibraryUiState

    data class Error(val message: String) : LibraryUiState
}

class LibraryViewModel(
    private val repository: LibraryRepository
) : ViewModel() {

    private val _mutationError = MutableStateFlow<String?>(null)

    val uiState: StateFlow<LibraryUiState> =
        combine(
            repository.observeSavedItems(),
            _mutationError
        ) { items, errorMessage ->
            if (!errorMessage.isNullOrBlank()) {
                return@combine LibraryUiState.Error(errorMessage)
            }

            val microDramas = items.filter { item ->
                item.mediaItem.type == MediaType.MICRO_DRAMA
            }

            if (microDramas.isEmpty()) {
                LibraryUiState.Empty
            } else {
                LibraryUiState.Success(microDramas)
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
        if (mediaItem.type != MediaType.MICRO_DRAMA) {
            return
        }

        viewModelScope.launch {
            try {
                _mutationError.value = null
                repository.save(
                    addon = addon,
                    mediaItem = mediaItem
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _mutationError.value = exception.message
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unable to save this micro-drama."
            }
        }
    }

    fun remove(libraryKey: String) {
        viewModelScope.launch {
            try {
                _mutationError.value = null
                repository.remove(libraryKey)
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _mutationError.value = exception.message
                    ?.takeIf { it.isNotBlank() }
                    ?: "Unable to remove this micro-drama."
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
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}
