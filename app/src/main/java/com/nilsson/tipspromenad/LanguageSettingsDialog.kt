package com.nilsson.tipspromenad

import android.app.Dialog
import android.os.Bundle
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.nilsson.tipspromenad.databinding.DialogLanguageSettingsBinding

class LanguageSettingsDialog : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val binding = DialogLanguageSettingsBinding.inflate(layoutInflater)
        val settings = LanguageSettings(requireContext())
        val adapter = ArrayAdapter(
            requireContext(), android.R.layout.simple_spinner_item, AppLanguages.nativeNames
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.uiLanguage.adapter = adapter
        binding.quizLanguage.adapter = adapter
        binding.uiLanguage.setSelection(
            AppLanguages.supported.indexOf(LanguageSettings.uiLanguage(requireContext()))
        )
        binding.quizLanguage.setSelection(
            AppLanguages.supported.indexOf(settings.quizLanguage).coerceAtLeast(0)
        )
        val modes = listOf("direct", "code", "qr")
        binding.correctionMode.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item,
            listOf(getString(R.string.correction_direct), getString(R.string.correction_code), getString(R.string.correction_qr)))
            .apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.correctionMode.setSelection(modes.indexOf(settings.correctionMode))
        binding.organizerButton.setOnClickListener {
            parentFragmentManager.setFragmentResult(ORGANIZER_KEY, Bundle.EMPTY)
            dismiss()
        }

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.action_settings)
            .setView(binding.root)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.apply) { _, _ ->
                val uiLanguage = AppLanguages.supported[binding.uiLanguage.selectedItemPosition]
                val uiLanguageChanged = uiLanguage != LanguageSettings.uiLanguage(requireContext())
                settings.quizLanguage = AppLanguages.supported[binding.quizLanguage.selectedItemPosition]
                settings.correctionMode = modes[binding.correctionMode.selectedItemPosition]
                AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(uiLanguage))
                if (!uiLanguageChanged) {
                    parentFragmentManager.setFragmentResult(RESULT_KEY, Bundle.EMPTY)
                }
            }
            .create()
    }

    companion object {
        const val RESULT_KEY = "language_settings_changed"
        const val ORGANIZER_KEY = "show_quiz_organizer"
        const val TAG = "language_settings"
    }
}
