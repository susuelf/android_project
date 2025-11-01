package com.progress.habittracker.data.model

import com.google.gson.annotations.SerializedName

/**
 * Profile Response DTO
 * 
 * Felhasználói profil teljes adatai
 * Backend endpoint: GET /profile
 * 
 * @property id User egyedi azonosítója
 * @property email Email cím
 * @property username Felhasználónév
 * @property description Profil leírás (opcionális)
 * @property profileImageUrl Profil kép URL (opcionális)
 * @property createdAt Regisztráció időpontja
 */
data class ProfileResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?,
    
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Update Profile Request
 * 
 * Profil adatok módosítása
 * Backend endpoint: PATCH /profile
 * 
 * @property username Új felhasználónév (opcionális)
 * @property description Új leírás (opcionális)
 */
data class UpdateProfileRequest(
    @SerializedName("username")
    val username: String? = null,
    
    @SerializedName("description")
    val description: String? = null
)

/**
 * Logout Response
 * 
 * Logout művelet válasza
 * 
 * @property message Sikeres logout üzenet
 */
data class LogoutResponse(
    @SerializedName("message")
    val message: String
)
