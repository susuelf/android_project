package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.data.repository.ScheduleRepository

/**
 * Home ViewModel Factory
 * 
 * ViewModelProvider.Factory implementáció a HomeViewModel példányosításához.
 * Szükséges mert a HomeViewModel konstruktor paraméterekkel rendelkezik.
 * 
 * Használat:
 * ```
 * val viewModel: HomeViewModel = viewModel(
 *     factory = HomeViewModelFactory(scheduleRepository, progressRepository)
 * )
 * ```
 * 
 * @property scheduleRepository Schedule repository instance
 * @property progressRepository Progress repository instance
 */
class HomeViewModelFactory(
    private val scheduleRepository: ScheduleRepository,
    private val progressRepository: ProgressRepository
) : ViewModelProvider.Factory {
    
    /**
     * ViewModel instance létrehozása
     * 
     * @param modelClass A létrehozandó ViewModel osztály típusa
     * @return ViewModel instance
     * @throws IllegalArgumentException ha ismeretlen ViewModel osztályt kap
     */
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        // Ellenőrizzük hogy HomeViewModel típust kérnek-e
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            // HomeViewModel létrehozása repositoryval
            return HomeViewModel(scheduleRepository, progressRepository) as T
        }
        
        // Ismeretlen ViewModel típus
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
