package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.HabitResponseDto
import com.progress.habittracker.data.model.ProfileResponseDto
import com.progress.habittracker.data.repository.ProfileRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters

/**
 * Profile ViewModel
 * 
 * Profil képernyő üzleti logikája
 * 
 * Funkciók:
 * - Profil betöltése
 * - Habit-ek betöltése
 * - Heti statisztikák számítása
 * - Kijelentkezés
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val tokenManager: TokenManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * UI State data class
     */
    data class ProfileUiState(
        val isLoading: Boolean = false,
        val profile: ProfileResponseDto? = null,
        val habits: List<HabitResponseDto> = emptyList(),
        val habitStats: Map<Int, Float> = emptyMap(), // Habit ID -> Consistency % (0.0 - 1.0)
        val isLoadingHabits: Boolean = false,
        val error: String? = null
    )

    /**
     * Profil betöltése
     */
    fun loadProfile() {
        viewModelScope.launch {
            profileRepository.getMyProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        resource.data?.let { profile ->
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    profile = profile,
                                    error = null
                                )
                            }
                            // Habit-ek betöltése és statisztikák indítása
                            loadUserHabitsAndStats(profile.id)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Ismeretlen hiba"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Felhasználó habit-jeinek és statisztikáinak betöltése
     * Combine operátorral összefésülve
     */
    private fun loadUserHabitsAndStats(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHabits = true) }

            // 1. Mai dátum lekérése
            val today = LocalDate.now()
            val dateFormatter = DateTimeFormatter.ISO_DATE
            val todayString = today.format(dateFormatter)

            // 2. Flow-k definiálása
            val habitsFlow = profileRepository.getUserHabits(userId)
            val schedulesFlow = scheduleRepository.getSchedulesByDay(todayString)
            
            // 3. Combine operátor használata: Habits + Today's Schedules
            combine(habitsFlow, schedulesFlow) { habitsResource, schedulesResource ->
                var currentHabits = _uiState.value.habits
                var currentStats = _uiState.value.habitStats
                var isLoading = true
                
                // Hibakezelés (egyszerűsítve: ha bármelyik hiba, akkor hiba)
                if (habitsResource is Resource.Error || schedulesResource is Resource.Error) {
                    isLoading = false
                    // Itt lehetne error message-t beállítani
                }

                // Ha mindkettő sikeres, számoljuk a statisztikát
                if (habitsResource is Resource.Success && schedulesResource is Resource.Success) {
                    val habits = habitsResource.data ?: emptyList()
                    val schedules = schedulesResource.data ?: emptyList()
                    
                    currentHabits = habits
                    
                    // Statisztika számítása: Mai átlagos progress habit-enként
                    currentStats = habits.associate { habit ->
                        // Keressük a habit-hez tartozó mai schedule-öket
                        // Fontos: schedule.habit.id-t használunk, mert a habitId mező nem biztosított
                        val habitSchedules = schedules.filter { it.habit.id == habit.id }
                        
                        val progress = if (habitSchedules.isNotEmpty()) {
                            // Összeadjuk a százalékokat
                            val totalProgress = habitSchedules.sumOf { schedule ->
                                com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule).progressPercentage.toDouble()
                            }
                            // Átlagolunk
                            val avgPercent = totalProgress / habitSchedules.size
                            // 0.0 - 1.0 tartományba konvertáljuk a UI számára
                            (avgPercent / 100.0).toFloat()
                        } else {
                            0f // Ha nincs mára ütemezve, 0%
                        }
                        habit.id to progress
                    }
                    isLoading = false
                } else if (habitsResource is Resource.Loading || schedulesResource is Resource.Loading) {
                    isLoading = true
                }
                
                Triple(currentHabits, currentStats, isLoading)
            }.collect { (habits, stats, isLoading) ->
                _uiState.update { 
                    it.copy(
                        habits = habits,
                        habitStats = stats,
                        isLoadingHabits = isLoading
                    ) 
                }
            }
        }
    }

    /**
     * Kijelentkezés
     */
    fun logout() {
        viewModelScope.launch {
            tokenManager.clearAll()
        }
    }

    /**
     * Hibaüzenet törlése
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

/**
 * ProfileViewModel Factory
 */
class ProfileViewModelFactory(
    private val profileRepository: ProfileRepository,
    private val scheduleRepository: ScheduleRepository,
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, scheduleRepository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
