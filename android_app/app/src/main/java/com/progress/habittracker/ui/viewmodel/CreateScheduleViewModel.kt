package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.*
import com.progress.habittracker.data.repository.HabitRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime

/**
 * Create Schedule ViewModel
 * 
 * State management a Create Schedule Screen-hez
 * 
 * Funkciók:
 * - Habit lista betöltése
 * - Új habit létrehozása
 * - Schedule létrehozása (custom vagy recurring)
 * - Form validáció
 */
class CreateScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val habitRepository: HabitRepository
) : ViewModel() {

    /**
     * UI State
     */
    data class CreateScheduleUiState(
        // Habit lista
        val habits: List<HabitResponseDto> = emptyList(),
        val isLoadingHabits: Boolean = false,
        
        // Selected habit
        val selectedHabit: HabitResponseDto? = null,
        
        // Schedule details
        val selectedDate: LocalDate = LocalDate.now(),
        val startTime: LocalTime = LocalTime.of(9, 0),
        val endTime: LocalTime? = null,
        val durationMinutes: Int? = 30,
        val notes: String = "",
        
        // Repeat pattern
        val repeatPattern: RepeatPattern = RepeatPattern.None,
        val repeatDays: Int = 30,
        
        // Weekday selection (for custom weekdays)
        val selectedWeekdays: List<Int> = emptyList(), // 1=Monday, 7=Sunday
        val numberOfWeeks: Int = 4,
        
        // States
        val isCreating: Boolean = false,
        val createSuccess: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(CreateScheduleUiState())
    val uiState: StateFlow<CreateScheduleUiState> = _uiState.asStateFlow()

    init {
        loadHabits()
    }

    /**
     * Habit lista betöltése
     */
    fun loadHabits() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHabits = true, error = null) }

            habitRepository.getHabits().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingHabits = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                habits = resource.data ?: emptyList(),
                                isLoadingHabits = false,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingHabits = false,
                                error = resource.message
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Habit kiválasztása
     */
    fun selectHabit(habit: HabitResponseDto) {
        _uiState.update { it.copy(selectedHabit = habit) }
    }

    /**
     * Dátum beállítása
     */
    fun setDate(date: LocalDate) {
        _uiState.update { it.copy(selectedDate = date) }
    }

    /**
     * Start time beállítása
     */
    fun setStartTime(time: LocalTime) {
        _uiState.update { it.copy(startTime = time) }
    }

    /**
     * End time beállítása
     */
    fun setEndTime(time: LocalTime?) {
        _uiState.update { it.copy(endTime = time) }
    }

    /**
     * Duration beállítása
     */
    fun setDuration(minutes: Int?) {
        _uiState.update { it.copy(durationMinutes = minutes) }
    }

    /**
     * Notes beállítása
     */
    fun setNotes(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    /**
     * Repeat pattern beállítása
     */
    fun setRepeatPattern(pattern: RepeatPattern) {
        _uiState.update { it.copy(repeatPattern = pattern) }
    }

    /**
     * Repeat days beállítása
     */
    fun setRepeatDays(days: Int) {
        _uiState.update { it.copy(repeatDays = days) }
    }

    /**
     * Weekday toggle (hét napja ki/be)
     */
    fun toggleWeekday(day: Int) {
        _uiState.update {
            val current = it.selectedWeekdays
            val updated = if (current.contains(day)) {
                current - day
            } else {
                current + day
            }
            it.copy(selectedWeekdays = updated.sorted())
        }
    }

    /**
     * Number of weeks beállítása
     */
    fun setNumberOfWeeks(weeks: Int) {
        _uiState.update { it.copy(numberOfWeeks = weeks) }
    }

    /**
     * Schedule létrehozása
     */
    fun createSchedule() {
        val state = _uiState.value
        
        // Validáció
        if (state.selectedHabit == null) {
            _uiState.update { it.copy(error = "Válassz ki egy habit-et!") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true, error = null) }

            when (state.repeatPattern) {
                RepeatPattern.None -> createCustomSchedule()
                else -> createRecurringSchedule()
            }
        }
    }

    /**
     * Egyedi (egyszeri) schedule létrehozása
     */
    private suspend fun createCustomSchedule() {
        val state = _uiState.value
        val habit = state.selectedHabit ?: return

        val request = CreateCustomScheduleRequest(
            habitId = habit.id,
            date = state.selectedDate.toString(), // YYYY-MM-DD
            startTime = state.startTime.toString(), // HH:mm:ss
            endTime = state.endTime?.toString(),
            durationMinutes = state.durationMinutes,
            isCustom = true,
            notes = state.notes.takeIf { it.isNotBlank() }
        )

        scheduleRepository.createCustomSchedule(request).collect { resource ->
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

    /**
     * Ismétlődő schedule létrehozása
     */
    private suspend fun createRecurringSchedule() {
        val state = _uiState.value
        val habit = state.selectedHabit ?: return

        val request = CreateRecurringScheduleRequest(
            habitId = habit.id,
            startTime = state.startTime.toString(),
            repeatPattern = state.repeatPattern,
            endTime = state.endTime?.toString(),
            durationMinutes = state.durationMinutes,
            repeatDays = state.repeatDays,
            isCustom = true,
            notes = state.notes.takeIf { it.isNotBlank() }
        )

        scheduleRepository.createRecurringSchedule(request).collect { resource ->
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

    /**
     * Hiba törlése
     */
    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }

    /**
     * Form reset
     */
    fun resetForm() {
        _uiState.update {
            CreateScheduleUiState(
                habits = it.habits // Megtartjuk a betöltött habit listát
            )
        }
    }
}
