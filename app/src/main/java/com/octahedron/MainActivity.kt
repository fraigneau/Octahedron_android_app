package com.octahedron

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.octahedron.service.BlePacketManager
import com.octahedron.ui.Menu
import com.octahedron.ui.lang.ProvideLocalizedResources
import com.octahedron.ui.theme.OctahedronTheme
import com.octahedron.veiwmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_CONNECT, Manifest.permission.BLUETOOTH_SCAN), 1)

        }

        val intent = Intent(this, BlePacketManager::class.java)
        startService(intent)

        enableEdgeToEdge()
        setContent {
            val vm: SettingsViewModel = viewModel()
            val prefs = vm.state.collectAsStateWithLifecycle().value

            // donner les permission pour le service de NotificationListener
            val enabled = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
            if (enabled?.contains(packageName) != true) {
                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
            }

            ProvideLocalizedResources(appLanguage = prefs.language) {
                OctahedronTheme(appTheme = prefs.theme) {
                    Menu(vm)
                }
            }
        }
    }
}