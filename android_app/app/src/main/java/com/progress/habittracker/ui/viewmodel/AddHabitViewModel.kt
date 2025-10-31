package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.HabitCategoryResponseDto
import com.progress.habittracker.data.repository.HabitRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * AddHabitViewModel - Add Habit Screen állapot kezelés
 *
 * Felelősségek:
 * - Habit kategóriák betöltése
 * - Form input kezelés (név, leírás, goal, kategória)
 * - Új habit létrehozása
 * - UI state management
 */
class AddHabitViewModel(
    private val habitRepository: HabitRepository
) : ViewModel() {

    /**
     * UI State - AddHabitScreen állapot
     */
    private val _uiState = MutableStateFlow(AddHabitUiState())
    val uiState: StateFlow<AddHabitUiState> = _uiState.asStateFlow()

    /**
     * Inicializálás - kategóriák betöltése
     */
    init {
        loadCategories()
    }

    /**
     * Habit kategóriák betöltése
     *
     * GET /habit/categories hívás a HabitRepository-n keresztül
     */
    fun loadCategories() {
        viewModelScope.launch {
            habitRepository.getCategories().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingCategories = true, error = null) }
                    }
                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                categories = resource.data ?: emptyList(),
                                isLoadingCategories = false,
                                error = null
                            )
                        }
                    }
                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingCategories = false,
                                error = resource.message ?: "Kategóriák betöltése sikertelen"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Habit név beállítása
     */
    fun setName(name: String) {
        _uiState.update { it.copy(name = name) }
    }

    /**
     * Habit leírás beállítása
     */
    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Habit goal beállítása
     */
    fun setGoal(goal: String) {
        _uiState.update { it.copy(goal = goal) }
    }

    /**
     * Kategória kiválasztása
     */
    fun selectCategory(category: HabitCategoryResponseDto) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    /**
     * Habit létrehozása
     *
     * Validáció:
     * - Név nem lehet üres
     * - Goal nem lehet üres
     * - Kategória ki kell legyen választva
     *
     * POST /habit hívás a HabitRepository-n keresztül
     */
    fun createHabit() {
        val state = _uiState.value

        // Validáció
        if (state.name.isBlank()) {
            _uiState.update { it.copy(error = "A habit neve kötelező") }
            return
        }

        if (state.goal.isBlank()) {
            _uiState.update { it.copy(error = "A cél megadása kötelező") }
            return
        }

        if (state.selectedCategory == null) {
            _uiState.update { it.copy(error = "Válassz egy kategóriát") }
            return
        }

        // Habit létrehozása
        viewModelScope.launch {
            val request = com.progress.habittracker.data.model.CreateHabitRequest(
                name = state.name.trim(),
                categoryId = state.selectedCategory.id,
                goal = state.goal.trim(),
                description = state.description.trim().takeIf { it.isNotBlank() }
            )

            habitRepository.createHabit(request).collect { resource ->
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
                                error = resource.message ?: "Habit létrehozása sikertelen"
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

/**
 * AddHabitUiState - UI állapot adatok
 *
 * @property categories Elérhető habit kategóriák
 * @property name Habit neve
 * @property description Habit leírása (opcionális)
 * @property goal Habit célja
 * @property selectedCategory Kiválasztott kategória
 * @property isLoadingCategories Kategóriák betöltése folyamatban
 * @property isCreating Habit létrehozás folyamatban
 * @property createSuccess Sikeres létrehozás flag
 * @property error Hibaüzenet
 */
data class AddHabitUiState(
    val categories: List<HabitCategoryResponseDto> = emptyList(),
    val name: String = "",
    val description: String = "",
    val goal: String = "",
    val selectedCategory: HabitCategoryResponseDto? = null,
    val isLoadingCategories: Boolean = false,
    val isCreating: Boolean = false,
    val createSuccess: Boolean = false,
    val error: String? = null
)
