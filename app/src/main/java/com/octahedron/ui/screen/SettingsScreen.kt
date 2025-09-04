package com.octahedron.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octahedron.data.AppLanguage
import com.octahedron.data.AppTheme
import com.octahedron.veiwmodel.SettingsViewModel
import com.octahedron.R

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val prefs by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text(stringResource(R.string.settings_title), style = MaterialTheme.typography.titleLarge)

        Text(stringResource(R.string.settings_theme))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip(stringResource(R.string.theme_system), prefs.theme == AppTheme.SYSTEM) { vm.onThemeSelected(AppTheme.SYSTEM) }
            ThemeChip(stringResource(R.string.theme_light),  prefs.theme == AppTheme.LIGHT)  { vm.onThemeSelected(AppTheme.LIGHT) }
            ThemeChip(stringResource(R.string.theme_dark),   prefs.theme == AppTheme.DARK)   { vm.onThemeSelected(AppTheme.DARK) }
        }

        OutlinedTextField(
            value = prefs.nickname,
            onValueChange = vm::onNicknameChanged,
            label = { Text(stringResource(R.string.nickname)) },
            modifier = Modifier.fillMaxWidth()
        )

        Text(stringResource(R.string.settings_language))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(AppLanguage.FRENCH, AppLanguage.ENGLISH).forEach { lang ->
                LanguageChip(
                    text = stringResource(lang.labelRes),
                    selected = prefs.language == lang
                ) { vm.onLanguageSelected(lang) }
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