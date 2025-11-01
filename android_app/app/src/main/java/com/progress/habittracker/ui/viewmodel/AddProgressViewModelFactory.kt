package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ProgressRepository

/**
 * AddProgressViewModelFactory
 * 
 * Factory az AddProgressViewModel létrehozásához
 * Szükséges, mert az AddProgressViewModel konstruktorában paramétereket várunk
 * 
 * @property scheduleId Schedule azonosító
 * @property progressRepository Progress repository
 */
class AddProgressViewModelFactory(
    private val scheduleId: Int,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddProgressViewModel::class.java)) {
            return AddProgressViewModel(scheduleId, progressRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
