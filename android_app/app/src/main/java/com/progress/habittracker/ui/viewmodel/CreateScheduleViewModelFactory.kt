package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.HabitRepository
import com.progress.habittracker.data.repository.ScheduleRepository

/**
 * Factory a CreateScheduleViewModel létrehozásához
 * 
 * Szükséges, mert a ViewModel konstruktorban paramétereket vár:
 * - scheduleRepository
 * - habitRepository
 */
class CreateScheduleViewModelFactory(
    private val scheduleRepository: ScheduleRepository,
    private val habitRepository: HabitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CreateScheduleViewModel::class.java)) {
            return CreateScheduleViewModel(scheduleRepository, habitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
