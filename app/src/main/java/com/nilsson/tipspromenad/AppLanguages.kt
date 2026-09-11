package com.nilsson.tipspromenad

import java.util.Locale

/** Language codes shared with the web quiz. Content has no implicit fallback. */
object AppLanguages {
    val supported = listOf("en", "es", "sv", "da", "no", "fi")
    val nativeNames = listOf("English", "Español", "Svenska", "Dansk", "Norsk", "Suomi")

    fun normalize(languageTag: String?): String? {
        val language = languageTag?.trim()?.replace('_', '-')
            ?.substringBefore('-')?.lowercase(Locale.ROOT)
        val canonical = when (language) {
            "se" -> "sv"
            "dk" -> "da"
            "nb", "nn" -> "no"
            else -> language
        }
        return canonical?.takeIf { it in supported }
    }

    fun uiLanguage(languageTag: String?): String = normalize(languageTag) ?: "en"
}
