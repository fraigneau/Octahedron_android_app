package com.octahedron

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octahedron.ui.Menu
import com.octahedron.ui.theme.OctahedronTheme
import com.octahedron.veiwmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            OctahedronTheme(appTheme = vm.state.collectAsState().value.theme) {
              Menu(vm)
            }
        }
    }
}