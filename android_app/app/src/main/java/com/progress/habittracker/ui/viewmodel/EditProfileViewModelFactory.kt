package com.progress.habittracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.progress.habittracker.data.repository.ProfileRepository

/**
 * Factory az EditProfileViewModel létrehozásához
 * 
 * Szükséges, mert a ViewModel konstruktorban paramétereket vár:
 * - profileRepository: Repository a profil műveletek végrehajtásához
 */
class EditProfileViewModelFactory(
    private val profileRepository: ProfileRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditProfileViewModel::class.java)) {
            return EditProfileViewModel(profileRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
