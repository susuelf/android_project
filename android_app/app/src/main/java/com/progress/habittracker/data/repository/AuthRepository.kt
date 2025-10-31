package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.AuthResponse
import com.progress.habittracker.data.model.GoogleSignInRequest
import com.progress.habittracker.data.model.ResetPasswordRequest
import com.progress.habittracker.data.model.SignInRequest
import com.progress.habittracker.data.model.SignUpRequest
import com.progress.habittracker.data.remote.AuthApiService
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.Response

/**
 * AuthRepository - Authentikációs repository
 * 
 * Ez az osztály felelős az authentikációval kapcsolatos üzleti logikáért.
 * Repository pattern-t használ: elválasztja az adatforrásokat (API, local storage)
 * a UI rétegtől.
 * 
 * @param authApiService API szolgáltatás
 * @param tokenManager Token kezelő
 */
class AuthRepository(
    private val authApiService: AuthApiService,
    private val tokenManager: TokenManager
) {
    
    /**
     * Bejelentkezés
     * 
     * @param email Felhasználó email címe
     * @param password Felhasználó jelszava
     * @return Flow<Resource<AuthResponse>> - A bejelentkezés eredménye
     */
    fun signIn(email: String, password: String): Flow<Resource<AuthResponse>> = flow {
        try {
            // Töltés állapot kibocsátása
            emit(Resource.Loading())
            
            // API hívás
            val request = SignInRequest(email, password)
            val response = authApiService.signIn(request)
            
            // Válasz feldolgozása
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Token-ek és felhasználói adatok mentése
                tokenManager.saveTokens(
                    accessToken = authResponse.tokens.accessToken,
                    refreshToken = authResponse.tokens.refreshToken,
                    userId = authResponse.user.id,
                    userEmail = authResponse.user.email,
                    userName = authResponse.user.username
                )
                
                // Sikeres állapot kibocsátása
                emit(Resource.Success(authResponse))
            } else {
                // Hiba állapot kibocsátása
                val errorMessage = response.errorBody()?.string() ?: "Bejelentkezési hiba"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            // Kivétel kezelése
            emit(Resource.Error(e.message ?: "Ismeretlen hiba történt"))
        }
    }
    
    /**
     * Regisztráció
     * 
     * @param username Felhasználó neve
     * @param email Felhasználó email címe
     * @param password Felhasználó jelszava
     * @return Flow<Resource<AuthResponse>> - A regisztráció eredménye
     */
    fun signUp(
        username: String,
        email: String,
        password: String
    ): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = SignUpRequest(username, email, password)
            val response = authApiService.signUp(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                // Token-ek és felhasználói adatok mentése
                tokenManager.saveTokens(
                    accessToken = authResponse.tokens.accessToken,
                    refreshToken = authResponse.tokens.refreshToken,
                    userId = authResponse.user.id,
                    userEmail = authResponse.user.email,
                    userName = authResponse.user.username
                )
                
                emit(Resource.Success(authResponse))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Regisztrációs hiba"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba történt"))
        }
    }
    
    /**
     * Jelszó visszaállítása
     * 
     * @param email Felhasználó email címe
     * @return Flow<Resource<String>> - A művelet eredménye üzenettel
     */
    fun resetPassword(email: String): Flow<Resource<String>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = ResetPasswordRequest(email)
            val response = authApiService.resetPassword(request)
            
            if (response.isSuccessful) {
                val message = response.body()?.get("message") ?: "Sikeres jelszó visszaállítás"
                emit(Resource.Success(message))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Jelszó visszaállítási hiba"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba történt"))
        }
    }
    
    /**
     * Google bejelentkezés
     * 
     * @param idToken Google ID token
     * @return Flow<Resource<AuthResponse>> - A bejelentkezés eredménye
     */
    fun googleSignIn(idToken: String): Flow<Resource<AuthResponse>> = flow {
        try {
            emit(Resource.Loading())
            
            val request = GoogleSignInRequest(idToken)
            val response = authApiService.googleSignIn(request)
            
            if (response.isSuccessful && response.body() != null) {
                val authResponse = response.body()!!
                
                tokenManager.saveTokens(
                    accessToken = authResponse.tokens.accessToken,
                    refreshToken = authResponse.tokens.refreshToken,
                    userId = authResponse.user.id,
                    userEmail = authResponse.user.email,
                    userName = authResponse.user.username
                )
                
                emit(Resource.Success(authResponse))
            } else {
                val errorMessage = response.errorBody()?.string() ?: "Google bejelentkezési hiba"
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba történt"))
        }
    }
    
    /**
     * Token frissítése
     * 
     * @return Flow<Resource<Boolean>> - A frissítés sikeressége
     */
    fun refreshToken(): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            // Refresh token lekérése
            var refreshToken: String? = null
            tokenManager.refreshToken.collect { token ->
                refreshToken = token
            }
            
            if (refreshToken == null) {
                emit(Resource.Error("Nincs refresh token"))
                return@flow
            }
            
            // API hívás
            val response = authApiService.refreshToken("Bearer $refreshToken")
            
            if (response.isSuccessful && response.body() != null) {
                val tokens = response.body()!!
                
                // Új token-ek mentése
                tokenManager.updateTokens(
                    accessToken = tokens.accessToken,
                    refreshToken = tokens.refreshToken
                )
                
                emit(Resource.Success(true))
            } else {
                emit(Resource.Error("Token frissítési hiba"))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba történt"))
        }
    }
    
    /**
     * Kijelentkezés
     * 
     * @return Flow<Resource<Boolean>> - A kijelentkezés sikeressége
     */
    fun logout(): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            // Access token lekérése
            var accessToken: String? = null
            tokenManager.accessToken.collect { token ->
                accessToken = token
            }
            
            if (accessToken != null) {
                // API hívás (opcionális - akkor is töröljük a helyi token-eket ha ez sikertelen)
                authApiService.logout("Bearer $accessToken")
            }
            
            // Helyi adatok törlése
            tokenManager.clearAll()
            
            emit(Resource.Success(true))
        } catch (e: Exception) {
            // Hiba esetén is töröljük a helyi adatokat
            tokenManager.clearAll()
            emit(Resource.Success(true))
        }
    }
    
    /**
     * Ellenőrzi, hogy a felhasználó be van-e jelentkezve
     * 
     * @return Flow<Boolean> - true ha van érvényes token
     */
    fun isLoggedIn(): Flow<Boolean> = flow {
        tokenManager.accessToken.collect { token ->
            emit(!token.isNullOrEmpty())
        }
    }
}
