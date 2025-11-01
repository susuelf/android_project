package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.HabitResponseDto
import com.progress.habittracker.data.model.ProfileResponseDto
import com.progress.habittracker.data.repository.ProfileRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel a Profile Screen-hez
 * 
 * Funkciók:
 * - Profil adatok betöltése
 * - User habit-jei lekérése
 * - Logout funkció
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /**
     * UI State data class
     */
    data class ProfileUiState(
        val profile: ProfileResponseDto? = null,
        val habits: List<HabitResponseDto> = emptyList(),
        val isLoading: Boolean = false,
        val isLoadingHabits: Boolean = false,
        val isLoggingOut: Boolean = false,
        val logoutSuccess: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    /**
     * Profil adatok betöltése
     */
    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            profileRepository.getMyProfile().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }

                    is Resource.Success -> {
                        val profile = resource.data
                        _uiState.update {
                            it.copy(
                                profile = profile,
                                isLoading = false,
                                error = null
                            )
                        }
                        
                        // Ha sikeres a profil betöltés, lekérjük a habit-eket is
                        if (profile != null) {
                            loadUserHabits(profile.id)
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                error = resource.message ?: "Hiba történt a profil betöltése közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * User habit-jeinek betöltése
     * 
     * @param userId User azonosító
     */
    private fun loadUserHabits(userId: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHabits = true) }

            profileRepository.getUserHabits(userId).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoadingHabits = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                habits = resource.data ?: emptyList(),
                                isLoadingHabits = false
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoadingHabits = false,
                                error = resource.message ?: "Hiba történt a habit-ek betöltése közben"
                            )
                        }
                    }
                }
            }
        }
    }

    /**
     * Logout funkció
     * 
     * Kijelentkezés és token-ek törlése
     */
    fun logout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggingOut = true, error = null) }

            profileRepository.logout().collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _uiState.update { it.copy(isLoggingOut = true) }
                    }

                    is Resource.Success -> {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                logoutSuccess = true,
                                error = null
                            )
                        }
                    }

                    is Resource.Error -> {
                        _uiState.update {
                            it.copy(
                                isLoggingOut = false,
                                error = resource.message ?: "Hiba történt a kijelentkezés közben"
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
}
