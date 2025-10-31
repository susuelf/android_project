package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ScheduleRepository

/**
 * Home ViewModel Factory
 * 
 * ViewModelProvider.Factory implementáció a HomeViewModel példányosításához.
 * Szükséges mert a HomeViewModel konstruktor paraméterekkel rendelkezik (ScheduleRepository).
 * 
 * Használat:
 * ```
 * val viewModel: HomeViewModel = viewModel(
 *     factory = HomeViewModelFactory(scheduleRepository)
 * )
 * ```
 * 
 * @property repository Schedule repository instance
 */
class HomeViewModelFactory(
    private val repository: ScheduleRepository
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
            // HomeViewModel létrehozása repository-val
            return HomeViewModel(repository) as T
        }
        
        // Ismeretlen ViewModel típus
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
