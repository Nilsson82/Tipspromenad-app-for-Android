package com.nilsson.tipspromenad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class AppLanguagesTest {
    @Test fun canonicalLanguagesRemainUnchanged() {
        for (language in AppLanguages.supported) {
            assertEquals(language, AppLanguages.normalize(language))
        }
    }

    @Test fun aliasesAndRegionalTagsResolveToCanonicalCodes() {
        val examples = mapOf(
            "se" to "sv", "se-SE" to "sv", "SV_se" to "sv",
            "dk" to "da", "DK-dk" to "da", "da-DK" to "da",
            "nb-NO" to "no", "nn_NO" to "no", "no-NO" to "no",
            "en-GB" to "en", " es-MX " to "es", "fi-FI" to "fi"
        )
        for ((input, expected) in examples) {
            assertEquals(input, expected, AppLanguages.normalize(input))
        }
    }

    @Test fun unsupportedQuestionLanguagesDoNotSilentlyBecomeEnglish() {
        for (input in listOf(null, "", "de-DE", "fr", "zz")) {
            assertNull(input, AppLanguages.normalize(input))
        }
    }

    @Test fun unsupportedUiLanguagesFallBackToEnglish() {
        assertEquals("en", AppLanguages.uiLanguage("de-DE"))
        assertEquals("en", AppLanguages.uiLanguage(null))
        assertEquals("sv", AppLanguages.uiLanguage("se"))
    }

    @Test fun languageNamesAndCodesStayPaired() {
        assertEquals(6, AppLanguages.supported.size)
        assertEquals(AppLanguages.supported.size, AppLanguages.nativeNames.size)
        assertEquals(AppLanguages.supported.size, AppLanguages.supported.toSet().size)
    }
}
