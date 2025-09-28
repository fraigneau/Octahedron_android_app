package com.octahedron.ui.screen

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.octahedron.ui.veiwmodel.HomeViewModel

@Composable fun HomeScreen(vm : HomeViewModel) {
    Text(text = "Home Screen",modifier = Modifier.padding(16.dp))
}