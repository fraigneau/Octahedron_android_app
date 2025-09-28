package com.octahedron.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octahedron.data.AppLanguage
import com.octahedron.data.AppTheme
import com.octahedron.ui.veiwmodel.SettingsViewModel
import com.octahedron.R
import com.octahedron.data.AppMusic

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val prefs by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        /** Theme Selection Chips **/
        Text(stringResource(R.string.settings_theme))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip(stringResource(R.string.theme_system), prefs.theme == AppTheme.SYSTEM) { vm.onThemeSelected(AppTheme.SYSTEM) }
            ThemeChip(stringResource(R.string.theme_light),  prefs.theme == AppTheme.LIGHT)  { vm.onThemeSelected(AppTheme.LIGHT) }
            ThemeChip(stringResource(R.string.theme_dark),   prefs.theme == AppTheme.DARK)   { vm.onThemeSelected(AppTheme.DARK) }
        }

        /** Nickname Input **/
        OutlinedTextField(
            value = prefs.nickname,
            onValueChange = vm::onNicknameChanged,
            label = { Text(stringResource(R.string.nickname)) },
            modifier = Modifier.fillMaxWidth()
        )

        /** Language Dropdown Menu **/
        Text(stringResource(R.string.settings_language))
        var langExpanded by remember { mutableStateOf(false) }
        val languages = listOf(AppLanguage.FRENCH, AppLanguage.ENGLISH)
        val selectedLanguage = prefs.language
        ExposedDropdownMenuBox(
            expanded = langExpanded,
            onExpandedChange = { langExpanded = !langExpanded }
        ) {
            OutlinedTextField(
                value = stringResource(selectedLanguage.labelRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_language)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = langExpanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = langExpanded,
                onDismissRequest = { langExpanded = false }
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(stringResource(lang.labelRes)) },
                        onClick = {
                            vm.onLanguageSelected(lang)
                            langExpanded = false
                        },
                        leadingIcon = {
                            if (selectedLanguage == lang) Icon(Icons.Default.Check, null)
                        }
                    )
                }
            }
        }

        /** Music App Dropdown Menu **/
        Text(stringResource(R.string.settings_music_app))
        var expanded by remember { mutableStateOf(false) }
        val musicApps = listOf(AppMusic.SPOTIFY, AppMusic.YOUTUBE_MUSIC, AppMusic.DEEZER)
        val selectedMusicApp = prefs.musicApp
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            OutlinedTextField(
                value = stringResource(selectedMusicApp.labelRes),
                onValueChange = {},
                readOnly = true,
                label = { Text(stringResource(R.string.settings_music_app)) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                musicApps.forEach { musicApp ->
                    DropdownMenuItem(
                        text = { Text(stringResource(musicApp.labelRes)) },
                        onClick = {
                            vm.onMusicSelected(musicApp)
                            expanded = false
                        },
                        leadingIcon = {
                            if (selectedMusicApp == musicApp) Icon(Icons.Default.Check, null)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun ThemeChip(text: String, selected: Boolean, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) }
    )
}

@Composable
fun LanguageChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) { { Icon(Icons.Default.Check, null) } } else null
    )
}

@Composable
fun MusicAppChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    AssistChip(
        onClick = onClick,
        label = { Text(text) },
        leadingIcon = if (selected) { { Icon(Icons.Default.Check, null) } } else null
    )
}