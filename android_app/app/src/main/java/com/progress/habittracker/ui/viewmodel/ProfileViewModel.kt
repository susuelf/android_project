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
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Profile ViewModel
 * 
 * Profil képernyő üzleti logikája
 * 
 * Funkciók:
 * - Profil betöltése
 * - Habit-ek betöltése
 * - Habit statisztikák számítása
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
                            // Habit-ek betöltése
                            loadUserHabits(profile.id)
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
     * Felhasználó habit-jeinek betöltése
     */
    private fun loadUserHabits(userId: Int) {
        viewModelScope.launch {
            profileRepository.getUserHabits(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingHabits = true) }
                    }
                    is Resource.Success -> {
                        resource.data?.let { habits ->
                            _uiState.update {
                                it.copy(
                                    isLoadingHabits = false,
                                    habits = habits
                                )
                            }
                            // Statisztikák számítása
                            calculateHabitStats(habits)
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(isLoadingHabits = false) }
                    }
                }
            }
        }
    }

    /**
     * Statisztikák számítása minden habit-hez
     */
    private fun calculateHabitStats(habits: List<HabitResponseDto>) {
        viewModelScope.launch {
            val stats = mutableMapOf<Int, Float>()
            
            // Párhuzamos lekérdezések
            val deferredStats = habits.map { habit ->
                async {
                    try {
                        val schedulesResource = scheduleRepository.getSchedulesByHabitId(habit.id).first()
                        if (schedulesResource is Resource.Success) {
                            val schedules = schedulesResource.data ?: emptyList()
                            if (schedules.isNotEmpty()) {
                                val totalPercentage = schedules.sumOf { schedule ->
                                    // Idő alapú progress számítás a központosított kalkulátorral
                                    val uiState = com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule)
                                    (uiState.progressPercentage / 100.0)
                                }
                                val average = totalPercentage / schedules.size
                                habit.id to average.toFloat()
                            } else {
                                habit.id to 0f
                            }
                        } else {
                            habit.id to 0f
                        }
                    } catch (e: Exception) {
                        habit.id to 0f
                    }
                }
            }
            
            deferredStats.awaitAll().forEach { (id, stat) ->
                stats[id] = stat
            }
            
            _uiState.update { it.copy(habitStats = stats) }
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
