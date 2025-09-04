package com.octahedron

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octahedron.ui.Menu
import com.octahedron.ui.lang.ProvideLocalizedResources
import com.octahedron.ui.theme.OctahedronTheme
import com.octahedron.veiwmodel.SettingsViewModel

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val prefs = vm.state.collectAsStateWithLifecycle().value
            ProvideLocalizedResources(language = prefs.language) {
                OctahedronTheme(appTheme = prefs.theme) {
                    Menu(vm)
                }
           }
        }
    }
}