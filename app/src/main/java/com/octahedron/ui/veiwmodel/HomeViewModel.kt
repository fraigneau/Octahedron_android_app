package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import com.octahedron.repository.ListeningHistoryRepository
import javax.inject.Inject

class HomeViewModel @Inject constructor(
    private val repo: ListeningHistoryRepository
): ViewModel() {
}