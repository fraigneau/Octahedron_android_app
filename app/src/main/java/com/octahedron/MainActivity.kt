package com.octahedron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.runtime.LaunchedEffect
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octahedron.ui.Menu
import com.octahedron.ui.lang.ProvideLocalizedResources
import com.octahedron.ui.theme.OctahedronTheme
import com.octahedron.veiwmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val prefs = vm.state.collectAsStateWithLifecycle().value

            OctahedronTheme(appTheme = prefs.theme) {
                Menu(vm)
            }
        }
    }
}