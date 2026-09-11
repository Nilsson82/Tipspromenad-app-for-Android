package com.nilsson.tipspromenad

import android.content.res.Configuration
import android.os.LocaleList
import android.text.Html
import android.text.style.URLSpan
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Locale

@RunWith(AndroidJUnit4::class)
class LanguageResourcesInstrumentedTest {
    @Test fun packagedResourcesResolveAllSixLanguagesAndKeepInfoLinks() {
        val baseContext = InstrumentationRegistry.getInstrumentation().targetContext
        val expectedSettingsTitles = mapOf(
            "en" to "Language settings",
            "es" to "Configuración de idiomas",
            "sv" to "Språkinställningar",
            "da" to "Sprogindstillinger",
            "no" to "Språkinnstillinger",
            "fi" to "Kieliasetukset"
        )
        for ((language, expectedTitle) in expectedSettingsTitles) {
            val configuration = Configuration(baseContext.resources.configuration)
            configuration.setLocales(LocaleList(Locale.forLanguageTag(language)))
            val context = baseContext.createConfigurationContext(configuration)
            assertEquals(language, expectedTitle, context.getString(R.string.action_settings))
            val info = Html.fromHtml(context.getString(R.string.app_info), Html.FROM_HTML_MODE_LEGACY)
            assertTrue(language, info.getSpans(0, info.length, URLSpan::class.java).any {
                it.url == "https://nilsson82.github.io/TipspromenadQuizWebPage/"
            })
        }
    }
}
