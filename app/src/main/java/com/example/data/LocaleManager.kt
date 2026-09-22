package com.example.data

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat

object LocaleManager {
    fun setLocale(languageTag: String) {
        val tags = when (languageTag.lowercase()) {
            "ja", "japanese" -> "ja"
            "in", "id", "indonesian" -> "in,id"
            else -> "en"
        }
        val appLocale = LocaleListCompat.forLanguageTags(tags)
        AppCompatDelegate.setApplicationLocales(appLocale)
    }

    fun getCurrentLocaleTag(): String {
        val locales = AppCompatDelegate.getApplicationLocales()
        return if (!locales.isEmpty) {
            val primary = locales.get(0)?.language?.lowercase() ?: "en"
            when (primary) {
                "ja" -> "ja"
                "in", "id" -> "in"
                else -> "en"
            }
        } else {
            "en"
        }
    }
}
