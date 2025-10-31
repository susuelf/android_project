package com.progress.habittracker.data.model

import com.google.gson.annotations.SerializedName

/**
 * AuthModels.kt - Authentikációhoz kapcsolódó adatmodellek
 * 
 * Ez a fájl tartalmazza az összes DTO-t (Data Transfer Object) amit az authentikáció
 * során használunk a backend API-val való kommunikációhoz.
 */

// ==================== REQUEST DTO-K ====================

/**
 * Bejelentkezési kérés DTO
 * 
 * @param email A felhasználó email címe
 * @param password A felhasználó jelszava
 */
data class SignInRequest(
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * Regisztrációs kérés DTO
 * 
 * @param username A felhasználó neve
 * @param email A felhasználó email címe
 * @param password A felhasználó jelszava
 */
data class SignUpRequest(
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("password")
    val password: String
)

/**
 * Jelszó visszaállítási kérés DTO
 * 
 * @param email A felhasználó email címe
 */
data class ResetPasswordRequest(
    @SerializedName("email")
    val email: String
)

/**
 * Google bejelentkezési kérés DTO
 * 
 * @param idToken A Google ID token
 */
data class GoogleSignInRequest(
    @SerializedName("idToken")
    val idToken: String
)

// ==================== RESPONSE DTO-K ====================

/**
 * Authentikációs válasz DTO
 * 
 * Ezt a backend küldi vissza sikeres login/register után
 * 
 * @param message Üzenet a műveletről
 * @param user A felhasználó adatai
 * @param tokens A token-ek (access és refresh)
 */
data class AuthResponse(
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("user")
    val user: User,
    
    @SerializedName("tokens")
    val tokens: Tokens
)

/**
 * Felhasználó DTO
 * 
 * A felhasználó alapvető adatait tartalmazza
 * 
 * @param id Felhasználó azonosító
 * @param email Email cím
 * @param username Felhasználónév
 * @param profileImageUrl Profilkép URL (opcionális)
 * @param description Leírás (opcionális)
 * @param createdAt Létrehozás dátuma
 * @param updatedAt Utolsó módosítás dátuma
 */
data class User(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("profileImageUrl")
    val profileImageUrl: String? = null,
    
    @SerializedName("description")
    val description: String? = null,
    
    @SerializedName("createdAt")
    val createdAt: String? = null,
    
    @SerializedName("updatedAt")
    val updatedAt: String? = null
)

/**
 * Token-ek DTO
 * 
 * A backend által visszaküldött token-eket tartalmazza
 * 
 * @param accessToken Hozzáférési token (rövid élettartamú)
 * @param refreshToken Frissítési token (hosszú élettartamú)
 */
data class Tokens(
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String
)

/**
 * Token frissítési válasz DTO
 * 
 * @param accessToken Új hozzáférési token
 * @param refreshToken Új frissítési token
 */
data class RefreshTokenResponse(
    @SerializedName("accessToken")
    val accessToken: String,
    
    @SerializedName("refreshToken")
    val refreshToken: String
)
