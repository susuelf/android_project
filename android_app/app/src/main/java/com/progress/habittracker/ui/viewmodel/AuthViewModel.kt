package com.progress.habittracker.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.AuthResponse
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.data.repository.AuthRepository
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * AuthViewModel - Authentikációs ViewModel
 * 
 * Ez a ViewModel kezeli az összes authentikációval kapcsolatos UI state-et és üzleti logikát.
 * MVVM pattern szerint kommunikál az AuthRepository-val.
 * 
 * @param context Alkalmazás kontextus (TokenManager inicializálásához)
 */
class AuthViewModel(context: Context) : ViewModel() {
    
    // Repository és TokenManager inicializálása
    private val tokenManager = TokenManager(context)
    private val authRepository = AuthRepository(
        authApiService = RetrofitClient.authApiService,
        tokenManager = tokenManager
    )
    
    // ==================== STATE MANAGEMENT ====================
    
    /**
     * AuthState - Authentikációs állapot
     * 
     * Sealed class az összes lehetséges auth állapothoz
     */
    sealed class AuthState {
        /** Kezdeti állapot - nincs művelet folyamatban */
        data object Idle : AuthState()
        
        /** Töltés állapot - API hívás folyamatban */
        data object Loading : AuthState()
        
        /** Sikeres authentikáció */
        data class Success(val authResponse: AuthResponse) : AuthState()
        
        /** Hiba történt */
        data class Error(val message: String) : AuthState()
    }
    
    // Privát mutable state
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    
    // Publikus immutable state (UI-nak)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    // ==================== LOGIN ====================
    
    /**
     * Bejelentkezés email és jelszó alapján
     * 
     * @param email Felhasználó email címe
     * @param password Felhasználó jelszava
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            authRepository.signIn(email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _authState.value = AuthState.Loading
                    }
                    is Resource.Success -> {
                        _authState.value = AuthState.Success(resource.data!!)
                    }
                    is Resource.Error -> {
                        _authState.value = AuthState.Error(
                            resource.message ?: "Bejelentkezési hiba"
                        )
                    }
                }
            }
        }
    }
    
    // ==================== REGISTER ====================
    
    /**
     * Regisztráció username, email és jelszó alapján
     * 
     * @param username Felhasználónév
     * @param email Email cím
     * @param password Jelszó
     */
    fun signUp(username: String, email: String, password: String) {
        viewModelScope.launch {
            authRepository.signUp(username, email, password).collect { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        _authState.value = AuthState.Loading
                    }
                    is Resource.Success -> {
                        _authState.value = AuthState.Success(resource.data!!)
                    }
                    is Resource.Error -> {
                        _authState.value = AuthState.Error(
                            resource.message ?: "Regisztrációs hiba"
                        )
                    }
                }
            }
        }
    }
    
    // ==================== AUTO LOGIN CHECK ====================
    
    /**
     * Ellenőrzi, hogy van-e érvényes token (auto-login)
     * 
     * Splash Screen használja
     * 
     * @return Boolean - true ha van érvényes token
     */
    fun checkAutoLogin(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            authRepository.isLoggedIn().collect { isLoggedIn ->
                onResult(isLoggedIn)
            }
        }
    }
    
    // ==================== VALIDATION ====================
    
    /**
     * Email validáció
     * 
     * @param email Email cím
     * @return Boolean - true ha valid
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
    
    /**
     * Jelszó validáció
     * 
     * Minimális követelmény: legalább 6 karakter
     * 
     * @param password Jelszó
     * @return Boolean - true ha valid
     */
    fun isValidPassword(password: String): Boolean {
        return password.length >= 6
    }
    
    /**
     * Jelszó egyezés ellenőrzése
     * 
     * @param password Jelszó
     * @param confirmPassword Jelszó megerősítés
     * @return Boolean - true ha egyeznek
     */
    fun passwordsMatch(password: String, confirmPassword: String): Boolean {
        return password == confirmPassword && password.isNotEmpty()
    }
    
    // ==================== STATE RESET ====================
    
    /**
     * Állapot visszaállítása Idle-ra
     * 
     * Hívd meg amikor elhagyod a képernyőt vagy új művelet előtt
     */
    fun resetState() {
        _authState.value = AuthState.Idle
    }
}
