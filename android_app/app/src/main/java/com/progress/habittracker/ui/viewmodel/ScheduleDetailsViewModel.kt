package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel a Schedule Details Screen-hez
 * 
 * Funkciók:
 * - Schedule részletes adatainak betöltése ID alapján
 * - Schedule státusz váltása (Planned/Completed/Skipped)
 * - Schedule törlése
 * - Notes frissítése
 * - Progress history megjelenítése
 */
class ScheduleDetailsViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleId: Int
) : ViewModel() {

    /**
     * UI State data class
     */
    data class ScheduleDetailsUiState(
        val schedule: ScheduleResponseDto? = null,
        val isLoading: Boolean = false,
        val error: String? = null,
        val isRefreshing: Boolean = false,
        val isUpdating: Boolean = false,
        val isDeleting: Boolean = false,
        val deleteSuccess: Boolean = false
    )

    private val _uiState = MutableStateFlow(ScheduleDetailsUiState())
    val uiState: StateFlow<ScheduleDetailsUiState> = _uiState.asStateFlow()

    init {
        loadScheduleDetails()
    }

    /**
     * Schedule részletes adatainak betöltése
     */
    fun loadScheduleDetails() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            scheduleRepository.getScheduleById(scheduleId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                schedule = resource.data,
                                isLoading = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Hiba történt a schedule betöltése közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Schedule frissítése (pull-to-refresh)
     */
    fun refreshSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true, error = null) }

            scheduleRepository.getScheduleById(scheduleId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Már be van állítva az isRefreshing
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                schedule = resource.data,
                                isRefreshing = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isRefreshing = false,
                                error = resource.message ?: "Hiba történt a frissítés közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Schedule státusz váltása
     * @param newStatus új státusz (Planned, Completed, Skipped)
     */
    fun updateScheduleStatus(newStatus: ScheduleStatus) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            scheduleRepository.updateScheduleStatus(scheduleId, newStatus.name).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Már be van állítva az isUpdating
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                schedule = resource.data,
                                isUpdating = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                error = resource.message ?: "Hiba történt a státusz frissítése közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Schedule törlése
     */
    fun deleteSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true, error = null) }

            scheduleRepository.deleteSchedule(scheduleId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Már be van állítva az isDeleting
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                deleteSuccess = true,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isDeleting = false,
                                error = resource.message ?: "Hiba történt a schedule törlése közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Hiba üzenet törlése
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Progress százalék számítása
     * @return 0-100 közötti érték
     */
    fun calculateProgressPercentage(): Float {
        val schedule = _uiState.value.schedule ?: return 0f
        val habit = schedule.habit

        // Goal String?-ból Int-re konvertálás
        val goalInt = habit.goal?.toIntOrNull() ?: return 0f

        // Ha nincs goal vagy 0, akkor 0%
        if (goalInt <= 0) return 0f

        // Completed progress rekordok száma
        val completedCount = schedule.progress?.count { it.isCompleted == true } ?: 0

        // Százalék számítás
        return (completedCount.toFloat() / goalInt.toFloat() * 100f).coerceIn(0f, 100f)
    }

    /**
     * Completed progress rekordok száma
     */
    fun getCompletedProgressCount(): Int {
        return _uiState.value.schedule?.progress?.count { it.isCompleted == true } ?: 0
    }

    /**
     * Összes progress rekordok száma
     */
    fun getTotalProgressCount(): Int {
        return _uiState.value.schedule?.progress?.size ?: 0
    }
}
