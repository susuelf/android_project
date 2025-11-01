package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ScheduleRepository

/**
 * EditScheduleViewModelFactory
 * 
 * Factory osztály az EditScheduleViewModel létrehozásához
 * Szükséges, mert konstruktor paramétereket kell átadni
 * 
 * @property scheduleId Schedule azonosító
 * @property scheduleRepository Schedule repository
 */
class EditScheduleViewModelFactory(
    private val scheduleId: Int,
    private val scheduleRepository: ScheduleRepository
) : ViewModelProvider.Factory {
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditScheduleViewModel::class.java)) {
            return EditScheduleViewModel(scheduleId, scheduleRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
