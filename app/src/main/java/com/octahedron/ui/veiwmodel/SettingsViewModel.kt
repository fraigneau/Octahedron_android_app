package com.octahedron.ui.veiwmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.octahedron.data.AppLanguage
import com.octahedron.data.AppMusic
import com.octahedron.data.AppTheme
import com.octahedron.data.UserPrefs
import com.octahedron.repository.UserPrefsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = UserPrefsRepository(app.applicationContext)

    val state: StateFlow<UserPrefs> =
        repo.prefs.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), UserPrefs.DEFAULT)

    fun onThemeSelected(theme: AppTheme) = viewModelScope.launch { repo.setTheme(theme) }
    fun onNicknameChanged(nick: String)   = viewModelScope.launch { repo.setNickname(nick) }
    fun onLanguageSelected(lang: AppLanguage) = viewModelScope.launch { repo.setLanguage(lang) }
    fun onMusicSelected(musicApp: AppMusic) = viewModelScope.launch { repo.setMusic(musicApp) }
}