package io.github.shashigm.videra.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.github.shashigm.videra.data.preferences.PreferencesRepository
import io.github.shashigm.videra.domain.common.Resource
import io.github.shashigm.videra.domain.usecase.InstallAddonUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class OnboardingUiState(
    val isInstalling: Boolean = false,
    val errorMessage: String? = null
)

class OnboardingViewModel(
    private val preferencesRepository: PreferencesRepository,
    private val installAddon: InstallAddonUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun startWatching(
        manifestUrl: String,
        onSuccess: () -> Unit
    ) {
        if (_uiState.value.isInstalling) return

        viewModelScope.launch {
            _uiState.value = OnboardingUiState(isInstalling = true)

            when (val result = installAddon(manifestUrl)) {
                Resource.Loading -> Unit

                is Resource.Success -> {
                    preferencesRepository.setOnboardingCompleted(true)
                    _uiState.value = OnboardingUiState()
                    onSuccess()
                }

                is Resource.Error -> {
                    _uiState.value = OnboardingUiState(
                        errorMessage = result.throwable.message
                            ?.takeIf { it.isNotBlank() }
                            ?: "The add-on could not be installed."
                    )
                }
            }
        }
    }
}

class OnboardingViewModelFactory(
    private val preferencesRepository: PreferencesRepository,
    private val installAddon: InstallAddonUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            return OnboardingViewModel(
                preferencesRepository = preferencesRepository,
                installAddon = installAddon
            ) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: " + modelClass.name
        )
    }
}
