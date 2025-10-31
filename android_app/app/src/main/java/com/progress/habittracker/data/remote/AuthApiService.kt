package com.progress.habittracker.data.remote

import com.progress.habittracker.data.model.AuthResponse
import com.progress.habittracker.data.model.GoogleSignInRequest
import com.progress.habittracker.data.model.RefreshTokenResponse
import com.progress.habittracker.data.model.ResetPasswordRequest
import com.progress.habittracker.data.model.SignInRequest
import com.progress.habittracker.data.model.SignUpRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * AuthApiService - Authentikációs API végpontok
 * 
 * Ez az interface definiálja az összes authentikációval kapcsolatos
 * API végpontot amit a backend biztosít.
 * 
 * Retrofit automatikusan implementálja ezt az interface-t.
 */
interface AuthApiService {
    
    /**
     * Bejelentkezés email és jelszó alapján
     * 
     * Endpoint: POST /auth/local/signin
     * 
     * @param request SignInRequest (email, password)
     * @return AuthResponse (user, tokens)
     */
    @POST("auth/local/signin")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<AuthResponse>
    
    /**
     * Regisztráció email és jelszó alapján
     * 
     * Endpoint: POST /auth/local/signup
     * 
     * @param request SignUpRequest (username, email, password)
     * @return AuthResponse (user, tokens)
     */
    @POST("auth/local/signup")
    suspend fun signUp(
        @Body request: SignUpRequest
    ): Response<AuthResponse>
    
    /**
     * Jelszó visszaállítása email alapján
     * 
     * Endpoint: POST /auth/reset-password-via-email
     * 
     * @param request ResetPasswordRequest (email)
     * @return Response üzenettel
     */
    @POST("auth/reset-password-via-email")
    suspend fun resetPassword(
        @Body request: ResetPasswordRequest
    ): Response<Map<String, String>>
    
    /**
     * Google bejelentkezés
     * 
     * Endpoint: POST /auth/google
     * 
     * @param request GoogleSignInRequest (idToken)
     * @return AuthResponse (user, tokens)
     */
    @POST("auth/google")
    suspend fun googleSignIn(
        @Body request: GoogleSignInRequest
    ): Response<AuthResponse>
    
    /**
     * Token frissítése
     * 
     * Endpoint: POST /auth/local/refresh
     * 
     * @param refreshToken A refresh token amit az Authorization headerben küldünk
     * @return RefreshTokenResponse (új accessToken, refreshToken)
     */
    @POST("auth/local/refresh")
    suspend fun refreshToken(
        @Header("Authorization") refreshToken: String
    ): Response<RefreshTokenResponse>
    
    /**
     * Kijelentkezés
     * 
     * Endpoint: POST /auth/local/logout
     * 
     * @param accessToken A felhasználó access token-je
     * @return Response üzenettel
     */
    @POST("auth/local/logout")
    suspend fun logout(
        @Header("Authorization") accessToken: String
    ): Response<Map<String, String>>
}
