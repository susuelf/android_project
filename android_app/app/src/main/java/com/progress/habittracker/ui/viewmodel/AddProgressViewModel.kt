package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.CreateProgressRequest
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Add Progress ViewModel
 * 
 * Progress (haladás) hozzáadásának state management-je
 * 
 * @property scheduleId Az a schedule, amihez progress-t adunk
 * @property scheduleRepository Schedule repository a duration lekérdezéshez
 * @property progressRepository Progress repository API hívásokhoz
 */
@Suppress("NewApi") // Java Time API is available via desugaring
class AddProgressViewModel(
    private val scheduleId: Int,
    private val scheduleRepository: ScheduleRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    /**
     * AddProgressUiState - UI állapot adatok
     * 
     * @property schedule Schedule adatok (duration eléréshez)
     * @property date Progress dátuma
     * @property loggedTime Eltöltött idő percben (kötelező)
     * @property notes Jegyzetek (opcionális)
     * @property maxAllowedTime Maximum hozzáadható idő (schedule duration - total logged time)
     * @property isLoading Schedule betöltés folyamatban
     * @property isCreating Progress létrehozás folyamatban
     * @property createSuccess Sikeres létrehozás flag
     * @property error Hibaüzenet
     */
    data class AddProgressUiState(
        val schedule: ScheduleResponseDto? = null,
        val date: LocalDate = LocalDate.now(),
        val loggedTime: String = "",
        val notes: String = "",
        val maxAllowedTime: Int = 0, // Dinamikusan számolt max
        val isLoading: Boolean = false,
        val isCreating: Boolean = false,
        val createSuccess: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(AddProgressUiState())
    val uiState: StateFlow<AddProgressUiState> = _uiState.asStateFlow()
    
    init {
        loadSchedule()
    }
    
    /**
     * Schedule betöltése (duration eléréshez)
     */
    private fun loadSchedule() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            scheduleRepository.getScheduleById(scheduleId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true) }
                    }
                    is Resource.Success -> {
                        _uiState.update { 
                            val schedule = resource.data
                            // FONTOS: Nem szűrünk isCompleted-re, minden progress számít
                            val totalLoggedTime = schedule?.progress
                                ?.sumOf { it.loggedTime ?: 0 } ?: 0
                            val scheduleDuration = schedule?.durationMinutes ?: 0
                            val maxAllowed = (scheduleDuration - totalLoggedTime).coerceAtLeast(0)
                            
                            it.copy(
                                schedule = schedule,
                                maxAllowedTime = maxAllowed,
                                isLoading = false
                            ) 
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Failed to load schedule"
                            ) 
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Dátum beállítása
     */
    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }
    
    /**
     * Logged time beállítása
     */
    fun setLoggedTime(time: String) {
        _uiState.update { it.copy(loggedTime = time) }
    }
    
    /**
     * Jegyzetek beállítása
     */
    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    /**
     * Progress létrehozása
     * Validálja hogy a time spent kötelező és pozitív érték
     */
    fun createProgress() {
        val state = _uiState.value
        
        // Validáció: time spent kötelező
        val timeValue = state.loggedTime.toIntOrNull()
        if (timeValue == null || timeValue <= 0) {
            _uiState.update { it.copy(error = "Time spent is required and must be greater than 0") }
            return
        }
        
        // Validáció: ne haladja meg a hátralévő időt
        if (timeValue > state.maxAllowedTime) {
            _uiState.update { it.copy(error = "Time spent cannot exceed remaining time (${state.maxAllowedTime} min)") }
            return
        }
        
        viewModelScope.launch {
            // Csak akkor jelöljük késznek a progress-t (és ezzel a schedule-t),
            // ha a hozzáadott idővel elérjük a teljes időtartamot.
            val isTaskCompleted = timeValue >= state.maxAllowedTime

            val request = CreateProgressRequest(
                scheduleId = scheduleId,
                date = state.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                loggedTime = timeValue,
                notes = state.notes.takeIf { it.isNotBlank() },
                isCompleted = isTaskCompleted
            )
            
            progressRepository.createProgress(request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isCreating = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                createSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isCreating = false,
                                error = resource.message ?: "Ismeretlen hiba"
                            )
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Hiba törlése
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
