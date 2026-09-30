package io.github.shashigm.videra.ui.settings

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.repository.AddonRepository
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

data class SettingsUiState(
    val isInstalling: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

sealed interface SettingsEvent {
    data class InstallAddon(val url: String) : SettingsEvent
    data class RemoveAddon(val id: String) : SettingsEvent
}

class SettingsViewModel(
    private val repository: AddonRepository
) : ViewModel() {
    val installedAddons: StateFlow<List<InstalledAddon>> =
        repository.observeInstalledAddons().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.InstallAddon -> installAddon(event.url)
            is SettingsEvent.RemoveAddon -> removeAddon(event.id)
        }
    }

    private fun installAddon(rawUrl: String) {
        val url = rawUrl.trim()
        if (!isValidHttpUrl(url)) {
            _uiState.value = SettingsUiState(
                errorMessage = "Enter a valid HTTP or HTTPS add-on URL."
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = SettingsUiState(isInstalling = true)
            when (val result = repository.installAddon(url, url)) {
                Resource.Loading -> Unit
                is Resource.Success -> {
                    _uiState.value = SettingsUiState(
                        successMessage = "Installed ${result.data.name}."
                    )
                }
                is Resource.Error -> {
                    _uiState.value = SettingsUiState(
                        errorMessage = toUserMessage(result.throwable)
                    )
                }
            }
        }
    }

    private fun removeAddon(addonId: String) {
        if (addonId.isBlank()) {
            _uiState.value = SettingsUiState(
                errorMessage = "Unable to remove the selected add-on."
            )
            return
        }

        viewModelScope.launch {
            try {
                repository.removeAddon(addonId)
                _uiState.value = SettingsUiState(
                    successMessage = "Add-on removed."
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = SettingsUiState(
                    errorMessage = toUserMessage(exception)
                )
            }
        }
    }

    private fun isValidHttpUrl(url: String): Boolean {
        val parsed = Uri.parse(url)
        val scheme = parsed.scheme?.lowercase()
        return (scheme == "http" || scheme == "https") &&
            !parsed.host.isNullOrBlank()
    }

    private fun toUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "Could not reach the add-on server."
            is SocketTimeoutException -> "The add-on server timed out."
            is HttpException -> "The add-on server returned HTTP ${throwable.code()}."
            is SerializationException -> {
                "The add-on manifest is not valid JSON for Videra."
            }
            else -> throwable.message
                ?.takeIf { it.isNotBlank() }
                ?: "The add-on could not be installed."
        }
    }
}

class SettingsViewModelFactory(
    private val repository: AddonRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(repository) as T
        }
        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
