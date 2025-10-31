package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ScheduleRepository

/**
 * Factory a ScheduleDetailsViewModel létrehozásához
 * 
 * Szükséges, mert a ViewModel konstruktorban paramétereket vár:
 * - scheduleRepository: Repository a schedule műveletek végrehajtásához
 * - scheduleId: Melyik schedule-t szeretnénk megjeleníteni
 */
class ScheduleDetailsViewModelFactory(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleId: Int
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ScheduleDetailsViewModel::class.java)) {
            return ScheduleDetailsViewModel(scheduleRepository, scheduleId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
