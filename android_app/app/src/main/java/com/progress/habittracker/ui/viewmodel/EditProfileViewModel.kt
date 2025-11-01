package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.model.ProfileResponseDto
import com.progress.habittracker.data.model.UpdateProfileRequest
import com.progress.habittracker.data.repository.ProfileRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

/**
 * ViewModel az Edit Profile Screen-hez
 * 
 * Funkciók:
 * - Profil adatok betöltése
 * - Username szerkesztése
 * - Description szerkesztése
 * - Profilkép feltöltése
 * - Profil mentése
 */
class EditProfileViewModel(
    private val profileRepository: ProfileRepository
) : ViewModel() {

    /**
     * UI State data class
     */
    data class EditProfileUiState(
        val profile: ProfileResponseDto? = null,
        val username: String = "",
        val description: String = "",
        val selectedImageFile: File? = null,
        val isLoading: Boolean = false,
        val isUpdating: Boolean = false,
        val isUploadingImage: Boolean = false,
        val updateSuccess: Boolean = false,
        val error: String? = null
    )

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

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
                                username = profile?.username ?: "",
                                description = profile?.description ?: "",
                                isLoading = false,
                                error = null
                            )
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
     * Username beállítása
     */
    fun setUsername(username: String) {
        _uiState.update { it.copy(username = username) }
    }

    /**
     * Description beállítása
     */
    fun setDescription(description: String) {
        _uiState.update { it.copy(description = description) }
    }

    /**
     * Profilkép kiválasztása
     */
    fun selectImage(imageFile: File) {
        _uiState.update { it.copy(selectedImageFile = imageFile) }
    }

    /**
     * Profil adatok mentése
     * 
     * Ha van kiválasztott kép, először azt feltölti, majd frissíti az adatokat.
     * Ha nincs kép, csak az adatokat frissíti.
     */
    fun saveProfile() {
        val currentState = _uiState.value
        
        // Validáció
        if (currentState.username.isBlank()) {
            _uiState.update { it.copy(error = "A felhasználónév nem lehet üres") }
            return
        }

        viewModelScope.launch {
            // Ha van kiválasztott kép, először azt feltöltjük
            if (currentState.selectedImageFile != null) {
                uploadProfileImage(currentState.selectedImageFile)
            }
            
            // Majd frissítjük a profil adatokat
            updateProfileData()
        }
    }

    /**
     * Profilkép feltöltése
     */
    private suspend fun uploadProfileImage(imageFile: File) {
        _uiState.update { it.copy(isUploadingImage = true, error = null) }

        profileRepository.uploadProfileImage(imageFile).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isUploadingImage = true) }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            profile = resource.data,
                            isUploadingImage = false,
                            selectedImageFile = null
                        )
                    }
                }

                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isUploadingImage = false,
                            error = resource.message ?: "Hiba történt a kép feltöltése közben"
                        )
                    }
                }
            }
        }
    }

    /**
     * Profil adatok frissítése (username, description)
     */
    private suspend fun updateProfileData() {
        val currentState = _uiState.value
        
        val request = UpdateProfileRequest(
            username = currentState.username.takeIf { it != currentState.profile?.username },
            description = currentState.description.takeIf { it != currentState.profile?.description }
        )
        
        // Ha semmi nem változott, ne küldjünk API hívást
        if (request.username == null && request.description == null) {
            _uiState.update { it.copy(updateSuccess = true) }
            return
        }

        _uiState.update { it.copy(isUpdating = true, error = null) }

        profileRepository.updateProfile(request).collect { resource ->
            when (resource) {
                is Resource.Loading -> {
                    _uiState.update { it.copy(isUpdating = true) }
                }

                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            profile = resource.data,
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
                            error = resource.message ?: "Hiba történt a profil frissítése közben"
                        )
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
