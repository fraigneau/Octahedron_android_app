package com.octahedron.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.octahedron.data.AppLanguage
import com.octahedron.data.AppMusic
import com.octahedron.data.AppTheme
import com.octahedron.data.UserPrefs
import com.octahedron.data.userPrefsDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

class UserPrefsRepository(private val context: Context) {

    private object Keys {
        val THEME = stringPreferencesKey("theme")
        val NICKNAME = stringPreferencesKey("nickname")
        val LANGUAGE = stringPreferencesKey("language")
        val MUSIC = stringPreferencesKey("music")
    }

    val prefs: Flow<UserPrefs> =
        context.userPrefsDataStore.data
            .catch { e ->
                if (e is IOException) emit(emptyPreferences())
                else throw e
            }
            .map { p ->
                UserPrefs(
                    theme = AppTheme.fromPref(p[Keys.THEME]),
                    nickname = p[Keys.NICKNAME] ?: "",
                    language = AppLanguage.fromPref(p[Keys.LANGUAGE]),
                    musicApp = AppMusic.fromPref(p[Keys.MUSIC])
                )
            }

    suspend fun setTheme(theme: AppTheme) {
        context.userPrefsDataStore.edit { it[Keys.THEME] = theme.prefValue }
    }

    suspend fun setNickname(nick: String) {
        context.userPrefsDataStore.edit { it[Keys.NICKNAME] = nick }
    }

    suspend fun setLanguage(lang: AppLanguage) {
        context.userPrefsDataStore.edit { it[Keys.LANGUAGE] = lang.tag }
    }

    suspend fun setMusic(musicApp: AppMusic) {
        context.userPrefsDataStore.edit { it[Keys.MUSIC] = musicApp.tag }
    }
}