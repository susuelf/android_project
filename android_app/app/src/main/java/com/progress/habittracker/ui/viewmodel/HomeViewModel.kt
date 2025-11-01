package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.CreateProgressRequest
import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus
import com.progress.habittracker.data.repository.ProgressRepository
import com.progress.habittracker.data.repository.ScheduleRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Home View Model
 * 
 * ViewModel a Home Screen state management-jéhez.
 * Kezeli a napi schedule-ok betöltését és a UI state-et.
 * 
 * @property scheduleRepository Schedule repository az API kommunikációhoz
 * @property progressRepository Progress repository az auto-completion-höz
 */
class HomeViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val progressRepository: ProgressRepository
) : ViewModel() {
    
    /**
     * UI State a Home Screen-hez
     * 
     * @property schedules Schedule-ok listája
     * @property isLoading Betöltés folyamatban flag
     * @property error Hibaüzenet (null ha nincs hiba)
     * @property selectedDate Kiválasztott dátum (default: mai nap)
     * @property isRefreshing Pull-to-refresh állapot
     */
    data class HomeUiState(
        val schedules: List<ScheduleResponseDto> = emptyList(),
        val isLoading: Boolean = false,
        val error: String? = null,
        val selectedDate: LocalDate = LocalDate.now(),
        val isRefreshing: Boolean = false
    )
    
    // Belső mutable state
    private val _uiState = MutableStateFlow(HomeUiState())
    
    /**
     * Publikus UI State Flow
     * A UI komponensek ezt figyelik (collect)
     */
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    /**
     * Dátum formázó (YYYY-MM-DD formátum a backend-nek)
     */
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    
    /**
     * Init blokk - első betöltés
     * Automatikusan betölti a mai napi schedule-okat
     */
    init {
        loadSchedules()
    }
    
    /**
     * Schedule-ok betöltése a kiválasztott napra
     * 
     * Lekéri a schedule-okat a backend-től és frissíti a UI state-et.
     * Pull-to-refresh esetén is ez fut le.
     * 
     * @param date Opcionális dátum (null = aktuális selectedDate használata)
     */
    fun loadSchedules(date: LocalDate? = null) {
        viewModelScope.launch {
            // Ha van új dátum, azt állítjuk be
            val targetDate = date ?: _uiState.value.selectedDate
            
            // Dátum formázása backend formátumba
            val dateString = targetDate.format(dateFormatter)
            
            // Repository Flow collect
            scheduleRepository.getSchedulesByDay(dateString).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        // Betöltés állapot
                        _uiState.value = _uiState.value.copy(
                            isLoading = true,
                            error = null,
                            selectedDate = targetDate
                        )
                    }
                    
                    is Resource.Success -> {
                        // Sikeres betöltés
                        _uiState.value = _uiState.value.copy(
                            schedules = resource.data ?: emptyList(),
                            isLoading = false,
                            error = null,
                            isRefreshing = false
                        )
                    }
                    
                    is Resource.Error -> {
                        // Hiba történt
                        _uiState.value = _uiState.value.copy(
                            error = resource.message,
                            isLoading = false,
                            isRefreshing = false
                        )
                    }
                }
            }
        }
    }
    
    /**
     * Pull-to-refresh kezelése
     * 
     * A felhasználó lehúzza a képernyőt -> újra betölti az adatokat
     */
    fun refreshSchedules() {
        _uiState.value = _uiState.value.copy(isRefreshing = true)
        loadSchedules()
    }
    
    /**
     * Dátum váltás
     * 
     * A felhasználó kiválaszt egy új dátumot -> betölti az adott nap schedule-jait
     * 
     * @param newDate Új dátum
     */
    fun selectDate(newDate: LocalDate) {
        if (newDate != _uiState.value.selectedDate) {
            loadSchedules(newDate)
        }
    }
    
    /**
     * Előző napra lépés
     */
    fun goToPreviousDay() {
        val previousDay = _uiState.value.selectedDate.minusDays(1)
        selectDate(previousDay)
    }
    
    /**
     * Következő napra lépés
     */
    fun goToNextDay() {
        val nextDay = _uiState.value.selectedDate.plusDays(1)
        selectDate(nextDay)
    }
    
    /**
     * Mai napra ugrás
     */
    fun goToToday() {
        selectDate(LocalDate.now())
    }
    
    /**
     * Schedule státusz váltása (Checkbox toggle)
     *
     * Követelmények:
     * - Két esetben lehet 100% a progress bar:
     *   1) Ha a felhasználó manuálisan kipipálja a schedule-t a Home screenen (visszavonható, amíg a progress nem éri el a maxot)
     *   2) Ha a progress természetesen eléri a maximumot (össz-idő >= duration) – ekkor véglegesen teljesített és NEM vonható vissza
     *
     * Megvalósítás:
     * - Planned -> Completed: CSAK státusz váltás (NEM hozunk létre 100% progress rekordot), így a progress alatta megmarad az eddig bevitt időn
     * - Completed -> Planned: csak akkor engedélyezett, ha a progress NEM érte el a maximumot (különben elutasítjuk)
     *
     * @param scheduleId A schedule ID-ja
     * @param currentStatus Jelenlegi státusz
     */
    fun toggleScheduleStatus(scheduleId: Int, currentStatus: ScheduleStatus) {
        viewModelScope.launch {
            // Keressük meg a schedule-t
            val schedule = _uiState.value.schedules.find { it.id == scheduleId }
            
            if (schedule == null) return@launch

            // Összes completed logged time és a duration kiszámítása
            val totalLoggedTime = schedule.progress?.filter { it.isCompleted }?.sumOf { it.loggedTime ?: 0 } ?: 0
            val duration = schedule.durationMinutes ?: 0
            val isProgressComplete = duration > 0 && totalLoggedTime >= duration

            when (currentStatus) {
                ScheduleStatus.Planned -> {
                    // Planned -> Completed: csak státusz váltás (nem hozunk létre progress-t)
                    updateScheduleStatus(scheduleId, "Completed")
                }

                ScheduleStatus.Completed -> {
                    // Completed -> Planned: csak akkor engedjük, ha a progress még nem 100%
                    if (isProgressComplete) {
                        // Nem engedjük visszavonni, mert a progress már tele van
                        _uiState.value = _uiState.value.copy(
                            error = _uiState.value.error // opcionálisan adhatnánk üzenetet
                        )
                        // Semmit nem csinálunk
                    } else {
                        updateScheduleStatus(scheduleId, "Planned")
                    }
                }

                ScheduleStatus.Skipped -> {
                    // Skipped -> Planned
                    updateScheduleStatus(scheduleId, "Planned")
                }
            }
        }
    }
    
    /**
     * Schedule státusz frissítése (helper)
     */
    private suspend fun updateScheduleStatus(scheduleId: Int, newStatus: String) {
        scheduleRepository.updateScheduleStatus(scheduleId, newStatus).collect { resource ->
            when (resource) {
                is Resource.Success -> {
                    // Sikeres frissítés -> újra betöltjük a listát
                    loadSchedules()
                }
                
                is Resource.Error -> {
                    // Hiba történt
                    _uiState.value = _uiState.value.copy(
                        error = resource.message
                    )
                }
                
                is Resource.Loading -> {
                    // Betöltés - nem kell kezelni itt
                }
            }
        }
    }
    
    /**
     * Hiba törlése
     * 
     * A felhasználó bezárta a hiba üzenetet
     */
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
