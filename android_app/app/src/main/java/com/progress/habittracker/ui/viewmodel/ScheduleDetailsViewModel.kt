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
 * - Ugyanazon nap más scheduleinek betöltése (recent activity)
 * - Schedule státusz váltása (Planned/Completed/Skipped)
 * - Schedule törlése
 * - Notes frissítése
 * - Progress percentage számítás (időalapú: eltöltött idő / schedule duration)
 */
@Suppress("NewApi") // Java Time API is available via desugaring
class ScheduleDetailsViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val scheduleId: Int
) : ViewModel() {

    /**
     * UI State data class
     */
    data class ScheduleDetailsUiState(
        val schedule: ScheduleResponseDto? = null,
        val daySchedules: List<ScheduleResponseDto> = emptyList(), // Ugyanazon nap más schedulejai
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
     * + Ugyanazon nap más scheduleinek betöltése
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
                        val schedule = resource.data
                        _uiState.update {
                            it.copy(
                                schedule = schedule,
                                isLoading = false,
                                error = null
                            )
                        }
                        
                        // Betöltjük ugyanazon nap más scheduleit
                        if (schedule != null) {
                            loadDaySchedules(schedule.date)
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
     * Ugyanazon nap más scheduleinek betöltése
     */
    private fun loadDaySchedules(date: String) {
        viewModelScope.launch {
            scheduleRepository.getSchedulesByDay(date).collect { resource ->
                when (resource) {
                    is Resource.Success -> {
                        // Minden schedule az adott napon, kivéve a jelenlegit
                        // A felhasználó kérése alapján az ÖSSZES schedule-t listázzuk, de a jelenlegit
                        // érdemes kihagyni a duplikáció elkerülése végett, vagy megjeleníteni másképp.
                        // A "Recent Activity" helyett "Daily Schedule List" lesz, így logikusabb a teljes lista.
                        // De a Details nézetben vagyunk, így a fókusz a kiválasztott elemen van.
                        // A kérés: "az adott naphoz tartozó ÖSSZES schedule megjelenjen benne".
                        
                        val allSchedules = resource.data?.filter { schedule ->
                            schedule.id != scheduleId
                        }?.sortedBy { 
                            // Időrendi sorrend (növekvő)
                            try {
                                val timeStr = if (it.startTime.contains('T')) {
                                    it.startTime.substringAfter('T').substringBefore('Z').substringBefore('+')
                                } else {
                                    it.startTime
                                }
                                java.time.LocalTime.parse(timeStr)
                            } catch (e: Exception) {
                                java.time.LocalTime.MIN
                            }
                        } ?: emptyList()
                        
                        _uiState.update { it.copy(daySchedules = allSchedules) }
                    }
                    is Resource.Error -> {
                        _uiState.update { it.copy(daySchedules = emptyList()) }
                    }
                    is Resource.Loading -> {
                        // Opcionális: loading state kezelése
                    }
                }
            }
        }
    }

    /**
     * Schedule frissítése (pull-to-refresh)
     * + Ugyanazon nap más scheduleinek frissítése
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
                        val schedule = resource.data
                        _uiState.update {
                            it.copy(
                                schedule = schedule,
                                isRefreshing = false,
                                error = null
                            )
                        }
                        
                        // Frissítjük ugyanazon nap más scheduleit
                        if (schedule != null) {
                            loadDaySchedules(schedule.date)
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
                        // Frissítjük a listát is, hogy a változások tükröződjenek
                        scheduleRepository.refresh()
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
     * Notes frissítése
     * @param newNotes új jegyzet szöveg
     */
    fun updateNotes(newNotes: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUpdating = true, error = null) }

            scheduleRepository.updateSchedule(
                scheduleId = scheduleId,
                request = com.progress.habittracker.data.model.UpdateScheduleRequest(
                    notes = newNotes
                )
            ).collect { resource ->
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
                                error = resource.message ?: "Hiba történt a jegyzet frissítése közben"
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
     * UI állapot számítása a központosított kalkulátorral
     */
    fun getScheduleUiState(): com.progress.habittracker.util.ScheduleUiState {
        val schedule = _uiState.value.schedule ?: return com.progress.habittracker.util.ScheduleUiState(0f, false, false)
        return com.progress.habittracker.util.ScheduleStateCalculator.calculate(schedule)
    }

    /**
     * Összes eltöltött idő (completed progressek)
     */
    fun getTotalLoggedTime(): Int {
        return _uiState.value.schedule?.progress
            ?.distinctBy { it.id }
            ?.sumOf { it.loggedTime ?: 0 } ?: 0
    }
}
