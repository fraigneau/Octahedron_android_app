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
    val musicApp: AppMusic = AppMusic.SPOTIFY,
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

enum class AppMusic(val tag: String, @StringRes val labelRes: Int) {
    SPOTIFY("com.spotify.music", R.string.music_app_spotify),
    YOUTUBE_MUSIC("com.google.android.apps.youtube.music", R.string.music_app_youtube_music),
    DEEZER("deezer.android.app", R.string.music_app_deezer);

    companion object {
        fun fromPref(value: String?): AppMusic = when (value) {
            "com.spotify.music" -> SPOTIFY
            "com.google.android.apps.youtube.music" -> YOUTUBE_MUSIC
            "deezer.android.app" -> DEEZER
            else -> SPOTIFY
        }
    }
}

val Context.userPrefsDataStore by preferencesDataStore(
    name = "user_prefs",
    corruptionHandler = ReplaceFileCorruptionHandler { emptyPreferences() }
)