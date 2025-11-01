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
 * Edit Profile ViewModel
 *
 * - Profil betöltése
 * - Kép kiválasztása és feltöltése
 * - Username/description mentése
 */
class EditProfileViewModel(
	private val profileRepository: ProfileRepository
) : ViewModel() {

	data class EditProfileUiState(
		val isLoading: Boolean = false,
		val isUpdating: Boolean = false,
		val isUploadingImage: Boolean = false,
		val updateSuccess: Boolean = false,
		val error: String? = null,

		val profile: ProfileResponseDto? = null,
		val username: String = "",
		val description: String = "",
		val selectedImageFile: File? = null
	)

	private val _uiState = MutableStateFlow(EditProfileUiState())
	val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

	init {
		loadProfile()
	}

	fun loadProfile() {
		viewModelScope.launch {
			profileRepository.getMyProfile().collect { resource ->
				when (resource) {
					is Resource.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
					is Resource.Success -> {
						val profile = resource.data
						_uiState.update {
							it.copy(
								isLoading = false,
								profile = profile,
								username = profile?.username ?: "",
								description = profile?.description ?: "",
								error = null
							)
						}
					}
					is Resource.Error -> _uiState.update {
						it.copy(isLoading = false, error = resource.message ?: "Hiba történt")
					}
				}
			}
		}
	}

	fun setUsername(value: String) {
		_uiState.update { it.copy(username = value) }
	}

	fun setDescription(value: String) {
		_uiState.update { it.copy(description = value) }
	}

	/**
	 * Kép kiválasztása és azonnali feltöltése a szerverre
	 * Siker esetén a szerver visszaadja az új profileImageUrl-t
	 */
	fun selectImage(file: File) {
		// Először lokálisan állítsuk be, hogy azonnal látszódjon a preview
		_uiState.update { it.copy(selectedImageFile = file) }

		viewModelScope.launch {
			profileRepository.uploadProfileImage(file).collect { resource ->
				when (resource) {
					is Resource.Loading -> _uiState.update { it.copy(isUploadingImage = true, error = null) }
					is Resource.Success -> {
						val updated = resource.data
						_uiState.update {
							it.copy(
								isUploadingImage = false,
								profile = updated,
								// Feltöltés sikerült -> törölhetjük a lokális file referenciát, a szerver URL él
								selectedImageFile = null,
								// reseteljük a success flag-et, itt nem navigálunk
								updateSuccess = false,
								error = null
							)
						}
					}
					is Resource.Error -> _uiState.update {
						it.copy(isUploadingImage = false, error = resource.message ?: "Kép feltöltési hiba")
					}
				}
			}
		}
	}

	/**
	 * Username/description mentése
	 */
	fun saveProfile() {
		val username = _uiState.value.username
		val description = _uiState.value.description

		viewModelScope.launch {
			profileRepository.updateProfile(
				UpdateProfileRequest(
					username = username,
					description = description
				)
			).collect { resource ->
				when (resource) {
					is Resource.Loading -> _uiState.update { it.copy(isUpdating = true, error = null, updateSuccess = false) }
					is Resource.Success -> {
						val updated = resource.data
						_uiState.update {
							it.copy(
								isUpdating = false,
								profile = updated,
								updateSuccess = true,
								error = null
							)
						}
					}
					is Resource.Error -> _uiState.update {
						it.copy(isUpdating = false, updateSuccess = false, error = resource.message ?: "Mentési hiba")
					}
				}
			}
		}
	}

	fun clearError() {
		_uiState.update { it.copy(error = null) }
	}
}

