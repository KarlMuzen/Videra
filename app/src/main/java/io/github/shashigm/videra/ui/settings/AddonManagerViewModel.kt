package io.github.shashigm.videra.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.model.InstalledAddon
import io.github.shashigm.videra.domain.repository.AddonRepository
import io.github.shashigm.videra.domain.usecase.InstallAddonUseCase
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import retrofit2.HttpException

enum class AddonManagerErrorKind {
    VALIDATION,
    NETWORK,
    HTTP,
    PARSING,
    UNKNOWN
}

data class AddonManagerUiState(
    val isInstalling: Boolean = false,
    val workingAddonId: String? = null,
    val errorMessage: String? = null,
    val errorKind: AddonManagerErrorKind? = null,
    val successMessage: String? = null
)

class AddonManagerViewModel(
    private val repository: AddonRepository,
    private val installAddon: InstallAddonUseCase = InstallAddonUseCase(repository)
) : ViewModel() {

    val installedAddons: StateFlow<List<InstalledAddon>> =
        repository.observeInstalledAddons().stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    private val _uiState = MutableStateFlow(AddonManagerUiState())
    val uiState: StateFlow<AddonManagerUiState> = _uiState.asStateFlow()

    fun installAddon(manifestUrl: String) {
        if (_uiState.value.isInstalling) return

        viewModelScope.launch {
            _uiState.value = AddonManagerUiState(isInstalling = true)

            when (val result = installAddon.invoke(manifestUrl)) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    _uiState.value = AddonManagerUiState(
                        successMessage = "Installed " + result.data.name + "."
                    )
                }

                is Resource.Error -> {
                    _uiState.value = AddonManagerUiState(
                        errorMessage = toUserMessage(result.throwable),
                        errorKind = toErrorKind(result.throwable)
                    )
                }
            }
        }
    }

    fun setEnabled(addonId: String, enabled: Boolean) {
        if (_uiState.value.workingAddonId != null) return

        viewModelScope.launch {
            _uiState.value = AddonManagerUiState(workingAddonId = addonId)
            try {
                repository.setAddonEnabled(addonId, enabled)
                _uiState.value = AddonManagerUiState(
                    successMessage = if (enabled) {
                        "Add-on enabled."
                    } else {
                        "Add-on disabled."
                    }
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = AddonManagerUiState(
                    errorMessage = toUserMessage(exception),
                    errorKind = toErrorKind(exception)
                )
            }
        }
    }

    fun renameAddon(addonId: String, customName: String) {
        if (_uiState.value.workingAddonId != null) return

        viewModelScope.launch {
            _uiState.value = AddonManagerUiState(workingAddonId = addonId)
            try {
                repository.setCustomName(addonId, customName)
                _uiState.value = AddonManagerUiState(
                    successMessage = "Add-on name updated."
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = AddonManagerUiState(
                    errorMessage = toUserMessage(exception),
                    errorKind = toErrorKind(exception)
                )
            }
        }
    }

    fun removeAddon(addonId: String) {
        if (_uiState.value.workingAddonId != null) return

        viewModelScope.launch {
            _uiState.value = AddonManagerUiState(workingAddonId = addonId)
            try {
                repository.removeAddon(addonId)
                _uiState.value = AddonManagerUiState(
                    successMessage = "Add-on removed."
                )
            } catch (exception: CancellationException) {
                throw exception
            } catch (exception: Exception) {
                _uiState.value = AddonManagerUiState(
                    errorMessage = toUserMessage(exception),
                    errorKind = toErrorKind(exception)
                )
            }
        }
    }

    fun clearMessage() {
        val current = _uiState.value
        if (current.isInstalling || current.workingAddonId != null) return
        _uiState.value = AddonManagerUiState()
    }

    private fun toErrorKind(throwable: Throwable): AddonManagerErrorKind {
        return when (throwable) {
            is UnknownHostException,
            is SocketTimeoutException -> AddonManagerErrorKind.NETWORK
            is HttpException -> AddonManagerErrorKind.HTTP
            is SerializationException -> AddonManagerErrorKind.PARSING
            is IllegalArgumentException -> AddonManagerErrorKind.VALIDATION
            else -> AddonManagerErrorKind.UNKNOWN
        }
    }

    private fun toUserMessage(throwable: Throwable): String {
        return when (throwable) {
            is UnknownHostException -> "Could not reach the add-on server."
            is SocketTimeoutException -> "The add-on server timed out."
            is HttpException -> "The add-on server returned HTTP " + throwable.code() + "."
            is SerializationException -> "The add-on manifest is not valid Videra JSON."
            else -> throwable.message
                ?.takeIf { it.isNotBlank() }
                ?: "The add-on operation failed."
        }
    }
}

class AddonManagerViewModelFactory(
    private val repository: AddonRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddonManagerViewModel::class.java)) {
            return AddonManagerViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}