package com.nilsson.tipspromenad

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

/** Verifies actual source resources so missing keys cannot be hidden by fallback. */
class LocalizationResourcesTest {
    private val resourcesDirectory: File
        get() = listOf(File("src/main/res"), File("app/src/main/res"))
            .first { it.isDirectory }

    private fun readStrings(file: File): Map<String, String> {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(file)
        val entries = document.getElementsByTagName("string")
        val strings = (0 until entries.length).associate { index ->
            val node = entries.item(index)
            node.attributes.getNamedItem("name").nodeValue to node.textContent
        }
        assertEquals("Duplicate translation keys in $file", entries.length, strings.size)
        return strings
    }

    @Test fun allSupportedLanguagesHaveEveryUiString() {
        val english = readStrings(File(resourcesDirectory, "values/strings.xml"))
        assertFalse(english.isEmpty())
        for (language in AppLanguages.supported.filter { it != "en" }) {
            val strings = readStrings(File(resourcesDirectory, "values-$language/strings.xml"))
            assertEquals(language, english.keys, strings.keys)
            assertTrue("Empty translations: $language", strings.values.all { it.isNotBlank() })
        }
    }

    @Test fun localeConfigMatchesLanguagePicker() {
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder()
            .parse(File(resourcesDirectory, "xml/locale_config.xml"))
        val locales = document.getElementsByTagName("locale")
        val codes = (0 until locales.length).map { index ->
            locales.item(index).attributes.getNamedItem("android:name").nodeValue
        }
        assertEquals(AppLanguages.supported, codes)
    }
}
