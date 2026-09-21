package com.nilsson.tipspromenad

import java.util.Locale

/** Language codes shared with the web quiz. Content has no implicit fallback. */
object AppLanguages {
    val supported = listOf("en", "sv", "es", "da", "no", "fi", "is", "th", "zh", "ja", "ko", "de", "fr", "it", "nl", "pt", "pl")
    val nativeNames = listOf("English", "Svenska", "Español", "Dansk", "Norsk", "Suomi", "Íslenska", "ไทย", "中文", "日本語", "한국어", "Deutsch", "Français", "Italiano", "Nederlands", "Português", "Polski")

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
