package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.model.UpdateScheduleRequest
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Edit Schedule ViewModel
 * 
 * Schedule szerkesztésének state management-je
 * 
 * @property scheduleId Szerkesztendő schedule azonosítója
 * @property scheduleRepository Schedule repository API hívásokhoz
 */
@Suppress("NewApi") // Java Time API is available via desugaring
class EditScheduleViewModel(
    private val scheduleId: Int,
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    /**
     * EditScheduleUiState - UI állapot adatok
     * 
     * @property schedule Eredeti schedule adatok
     * @property date Dátum
     * @property startTime Kezdési időpont
     * @property endTime Befejezési időpont
     * @property durationMinutes Időtartam percben
     * @property status Schedule státusza
     * @property notes Jegyzetek
     * @property isLoading Betöltés állapot
     * @property isUpdating Frissítés folyamatban
     * @property updateSuccess Sikeres frissítés flag
     * @property error Hibaüzenet
     */
    data class EditScheduleUiState(
        val schedule: ScheduleResponseDto? = null,
        val date: LocalDate = LocalDate.now(),
        val startTime: LocalTime = LocalTime.of(9, 0),
        val endTime: LocalTime? = null,
        val durationMinutes: Int = 30,
        val status: ScheduleStatus = ScheduleStatus.Planned,
        val notes: String = "",
        val isLoading: Boolean = false,
        val isUpdating: Boolean = false,
        val updateSuccess: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(EditScheduleUiState())
    val uiState: StateFlow<EditScheduleUiState> = _uiState.asStateFlow()
    
    init {
        loadScheduleDetails()
    }
    
    /**
     * Schedule részletek betöltése
     */
    private fun loadScheduleDetails() {
        viewModelScope.launch {
            scheduleRepository.getScheduleById(scheduleId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is Resource.Success -> {
                        val schedule = resource.data!!
                        _uiState.update {
                            it.copy(
                                schedule = schedule,
                                date = parseDate(schedule.date),
                                startTime = parseTime(schedule.startTime),
                                endTime = schedule.endTime?.let { time -> parseTime(time) },
                                durationMinutes = schedule.durationMinutes ?: 30,
                                status = schedule.status,
                                notes = schedule.notes ?: "",
                                isLoading = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Nem sikerült betölteni"
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
     * Kezdési időpont beállítása
     */
    fun setStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }
    
    /**
     * Befejezési időpont beállítása
     */
    fun setEndTime(time: LocalTime?) {
        _uiState.update { it.copy(endTime = time) }
    }
    
    /**
     * Időtartam beállítása
     */
    fun setDuration(minutes: Int) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }
    
    /**
     * Státusz beállítása
     */
    fun setStatus(status: ScheduleStatus) {
        _uiState.update { it.copy(status = status) }
    }
    
    /**
     * Jegyzetek beállítása
     */
    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }
    
    /**
     * Schedule frissítése
     */
    fun updateSchedule() {
        val state = _uiState.value
        
        viewModelScope.launch {
            // ISO 8601 formátum: "2025-11-01T14:30:00"
            val startDateTime = "${state.date}T${state.startTime}"
            val endDateTime = state.endTime?.let { "${state.date}T${it}" }
            
            val request = UpdateScheduleRequest(
                startTime = startDateTime,
                endTime = endDateTime,
                durationMinutes = state.durationMinutes,
                status = state.status.name,
                date = "${state.date}T00:00:00",
                notes = state.notes.takeIf { it.isNotBlank() }
            )
            
            scheduleRepository.updateSchedule(scheduleId, request).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isUpdating = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
                                updateSuccess = true,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isUpdating = false,
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
    
    /**
     * Dátum parsing helper
     */
    private fun parseDate(dateString: String): LocalDate {
        return try {
            // Format: "2025-10-31T14:30:00.000Z" vagy "2025-10-31"
            LocalDate.parse(dateString.take(10))
        } catch (e: Exception) {
            LocalDate.now()
        }
    }
    
    /**
     * Időpont parsing helper
     */
    private fun parseTime(timeString: String): LocalTime {
        return try {
            // Format: "2025-10-31T14:30:00.000Z" vagy "14:30:00"
            if (timeString.contains('T')) {
                val timePart = timeString.substringAfter('T').take(8)
                LocalTime.parse(timePart)
            } else {
                LocalTime.parse(timeString.take(8))
            }
        } catch (e: Exception) {
            LocalTime.of(9, 0)
        }
    }
}
