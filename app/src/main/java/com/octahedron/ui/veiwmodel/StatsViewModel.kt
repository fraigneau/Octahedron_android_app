package com.octahedron.ui.veiwmodel

import androidx.lifecycle.ViewModel
import com.octahedron.repository.ListeningHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject

@HiltViewModel
class StatsViewModel @Inject constructor(
    private val repo: ListeningHistoryRepository
) : ViewModel() {

}
