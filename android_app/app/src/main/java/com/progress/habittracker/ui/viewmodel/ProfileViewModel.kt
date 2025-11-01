package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.local.TokenManager
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
 * Profile ViewModel
 * 
 * Profil képernyő üzleti logikája
 * 
 * Funkciók:
 * - Profil betöltése
 * - Habit-ek betöltése
 * - Kijelentkezés
 */
class ProfileViewModel(
    private val profileRepository: ProfileRepository,
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
    private val tokenManager: TokenManager
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(profileRepository, tokenManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
