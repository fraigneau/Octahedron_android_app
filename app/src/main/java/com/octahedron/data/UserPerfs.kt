package com.octahedron.data

import android.content.Context
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.SharedPreferencesMigration
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore

data class UserPrefs(
    val theme: AppTheme,
    val nickname: String,
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

val Context.userPrefsDataStore by preferencesDataStore(
    name = "user_prefs",
    produceMigrations = { ctx ->
        listOf(SharedPreferencesMigration(ctx, "legacy_prefs"))
    },
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)