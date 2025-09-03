package com.octahedron.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.octahedron.data.AppTheme
import com.octahedron.veiwmodel.SettingsViewModel

@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val prefs by vm.state.collectAsStateWithLifecycle()

    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Paramètres", style = MaterialTheme.typography.titleLarge)

        Text("Thème")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ThemeChip("Système", prefs.theme == AppTheme.SYSTEM) { vm.onThemeSelected(AppTheme.SYSTEM) }
            ThemeChip("Clair",   prefs.theme == AppTheme.LIGHT)  { vm.onThemeSelected(AppTheme.LIGHT) }
            ThemeChip("Sombre",  prefs.theme == AppTheme.DARK)   { vm.onThemeSelected(AppTheme.DARK) }
        }

        OutlinedTextField(
            value = prefs.nickname,
            onValueChange = vm::onNicknameChanged,
            label = { Text("Nickname") },
            modifier = Modifier.fillMaxWidth()
        )
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