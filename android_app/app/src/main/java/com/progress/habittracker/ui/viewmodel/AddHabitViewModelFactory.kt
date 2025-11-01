package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.HabitRepository

/**
 * AddHabitViewModelFactory - ViewModel Factory
 *
 * Factory pattern a AddHabitViewModel létrehozásához dependency injection-nel.
 * A HabitRepository-t injektáljuk a ViewModel-be.
 *
 * @property habitRepository Habit repository instance
 */
class AddHabitViewModelFactory(
    private val habitRepository: HabitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AddHabitViewModel::class.java)) {
            return AddHabitViewModel(habitRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
