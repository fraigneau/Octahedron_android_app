package com.octahedron.data

import android.content.Context
import androidx.annotation.StringRes
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.octahedron.R

data class UserPrefs(
    val theme: AppTheme,
    val nickname: String,
    val language: AppLanguage = AppLanguage.ENGLISH,
) {
    companion object {
        val DEFAULT = UserPrefs(AppTheme.SYSTEM, "nickname")
    }
}

enum class AppTheme(val prefValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromPref(value: String?): AppTheme =
            entries.firstOrNull { it.prefValue == value } ?: SYSTEM
    }
}

enum class AppLanguage(val tag: String, @StringRes val labelRes: Int) {
    SYSTEM("",  R.string.lang_system),
    FRENCH("fr", R.string.lang_french),
    ENGLISH("en", R.string.lang_english);

    companion object {
        fun fromPref(value: String?): AppLanguage = when (value) {
            null, "" -> SYSTEM
            "fr"     -> FRENCH
            "en"     -> ENGLISH
            else     -> SYSTEM
        }
    }
}

val Context.userPrefsDataStore by preferencesDataStore(
    name = "user_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)