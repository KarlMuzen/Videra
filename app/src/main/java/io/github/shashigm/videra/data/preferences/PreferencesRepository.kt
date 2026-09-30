package io.github.shashigm.videra.data.preferences

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppTheme {
    VELVET_RED,
    OLED_BLACK;

    companion object {
        fun parseTheme(raw: String?): AppTheme {
            if (raw.isNullOrBlank()) {
                return VELVET_RED
            }

            val normalized = raw
                .trim()
                .replace(" ", "_")
                .uppercase()

            return entries.firstOrNull { it.name == normalized }
                ?: VELVET_RED
        }
    }
}

class PreferencesRepository(
    context: Context
) {
    companion object {
        private const val PREFS_NAME = "videra_preferences"
        private const val KEY_ONBOARDING_COMPLETED = "onboardingCompleted"
        private const val KEY_APP_THEME = "app_theme"
        private const val LEGACY_KEY_APP_THEME = "appTheme"
    }

    private val sharedPreferences = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    private val _onboardingCompleted = MutableStateFlow(
        sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    )
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    fun isOnboardingCompleted(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    private val _appTheme = MutableStateFlow(getTheme())
    val appTheme: StateFlow<AppTheme> = _appTheme.asStateFlow()

    fun getTheme(): AppTheme {
        val raw = sharedPreferences.getString(KEY_APP_THEME, null)
            ?: sharedPreferences.getString(LEGACY_KEY_APP_THEME, null)
            ?: return AppTheme.VELVET_RED

        return AppTheme.parseTheme(raw)
    }

    fun setOnboardingCompleted(completed: Boolean) {
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, completed)
            .apply()
        _onboardingCompleted.value = completed
    }

    fun setTheme(theme: AppTheme) {
        sharedPreferences.edit()
            .putString(KEY_APP_THEME, theme.name)
            .apply()
        _appTheme.value = theme
    }
}
