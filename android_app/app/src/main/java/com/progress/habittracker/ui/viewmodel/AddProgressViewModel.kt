package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.CreateProgressRequest
import com.progress.habittracker.data.repository.ProgressRepository
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
 * @property progressRepository Progress repository API hívásokhoz
 */
@Suppress("NewApi") // Java Time API is available via desugaring
class AddProgressViewModel(
    private val scheduleId: Int,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    /**
     * AddProgressUiState - UI állapot adatok
     * 
     * @property date Progress dátuma
     * @property loggedTime Eltöltött idő percben (opcionális)
     * @property notes Jegyzetek (opcionális)
     * @property isCompleted Befejezett-e
     * @property isCreating Progress létrehozás folyamatban
     * @property createSuccess Sikeres létrehozás flag
     * @property error Hibaüzenet
     */
    data class AddProgressUiState(
        val date: LocalDate = LocalDate.now(),
        val loggedTime: String = "",
        val notes: String = "",
        val isCompleted: Boolean = true,
        val isCreating: Boolean = false,
        val createSuccess: Boolean = false,
        val error: String? = null
    )
    
    private val _uiState = MutableStateFlow(AddProgressUiState())
    val uiState: StateFlow<AddProgressUiState> = _uiState.asStateFlow()
    
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
     * Completed checkbox toggle
     */
    fun toggleCompleted() {
        _uiState.update { it.copy(isCompleted = !it.isCompleted) }
    }
    
    /**
     * Progress létrehozása
     */
    fun createProgress() {
        val state = _uiState.value
        
        viewModelScope.launch {
            val request = CreateProgressRequest(
                scheduleId = scheduleId,
                date = state.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                loggedTime = state.loggedTime.toIntOrNull(),
                notes = state.notes.takeIf { it.isNotBlank() },
                isCompleted = state.isCompleted
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
