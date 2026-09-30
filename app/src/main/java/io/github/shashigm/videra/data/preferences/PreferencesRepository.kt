package io.github.shashigm.videra.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesRepository(
    context: Context
) {
    companion object {
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_OLED_BLACK = "OLED_BLACK"
        const val THEME_VELVET_RED = "VELVET_RED"

        private const val PREFS_NAME = "videra_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboardingCompleted"
        private const val KEY_APP_THEME = "appTheme"
    }

    private val preferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _onboardingCompleted = MutableStateFlow(
        preferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _appTheme = MutableStateFlow(
        normalizeTheme(preferences.getString(KEY_APP_THEME, THEME_SYSTEM))
    )
    val appTheme: StateFlow<String> = _appTheme.asStateFlow()

    fun setOnboardingCompleted(completed: Boolean) {
        preferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
        _onboardingCompleted.value = completed
    }

    fun setAppTheme(theme: String) {
        val normalized = normalizeTheme(theme)
        preferences.edit()
            .putString(KEY_APP_THEME, normalized)
            .apply()
        _appTheme.value = normalized
    }

    private fun normalizeTheme(theme: String?): String {
        return when (theme) {
            THEME_OLED_BLACK -> THEME_OLED_BLACK
            THEME_VELVET_RED -> THEME_VELVET_RED
            else -> THEME_SYSTEM
        }
    }
}
