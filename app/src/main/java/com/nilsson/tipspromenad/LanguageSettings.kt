package com.nilsson.tipspromenad

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate

class LanguageSettings(context: Context) {
    private val preferences = context.getSharedPreferences("language_settings", Context.MODE_PRIVATE)

    var quizLanguage: String
        get() {
            val stored = preferences.getString("quiz_language", "en") ?: "en"
            // Preserve an unknown stored code so the web app can explain unavailability.
            // Silently substituting English here would change the requested quiz content.
            return AppLanguages.normalize(stored) ?: stored
        }
        set(value) {
            val language = requireNotNull(AppLanguages.normalize(value))
            preferences.edit().putString("quiz_language", language).apply()
        }

    companion object {
        fun uiLanguage(context: Context): String {
            val locale = AppCompatDelegate.getApplicationLocales()[0]
                ?: context.resources.configuration.locales[0]
            return AppLanguages.uiLanguage(locale?.toLanguageTag())
        }
    }
}
