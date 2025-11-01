package com.progress.habittracker.data.remote

import com.progress.habittracker.data.model.LogoutResponse
import com.progress.habittracker.data.model.ProfileResponseDto
import com.progress.habittracker.data.model.UpdateProfileRequest
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Profile API Service
 * 
 * Retrofit interface a profil-kezeléssel kapcsolatos API endpoint-okhoz.
 * Backend BASE_URL: http://{gateway_ip}:3000
 * 
 * Támogatott műveletek:
 * - Profil lekérése (GET /profile)
 * - Profil frissítése (PATCH /profile)
 * - Profil kép feltöltése (POST /profile/upload-profile-image)
 * - Logout (POST /auth/local/logout)
 */
interface ProfileApiService {
    
    /**
     * Saját profil lekérése
     * 
     * Endpoint: GET /profile
     * 
     * @param authorization Bearer token ("Bearer {access_token}")
     * @return Response<ProfileResponseDto> - Profil adatok
     * 
     * Response kódok:
     * - 200: Sikeres lekérés
     * - 401: Nincs bejelentkezve / lejárt token
     */
    @GET("profile")
    suspend fun getMyProfile(
        @Header("Authorization") authorization: String
    ): Response<ProfileResponseDto>
    
    /**
     * Profil frissítése
     * 
     * Endpoint: PATCH /profile
     * 
     * @param request UpdateProfileRequest - Frissítendő adatok (username, description)
     * @param authorization Bearer token
     * @return Response<ProfileResponseDto> - Frissített profil
     * 
     * Response kódok:
     * - 200: Sikeres frissítés
     * - 400: Hibás adatok
     * - 401: Nincs bejelentkezve
     */
    @PATCH("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequest,
        @Header("Authorization") authorization: String
    ): Response<ProfileResponseDto>
    
    /**
     * Profil kép feltöltése
     * 
     * Endpoint: POST /profile/upload-profile-image
     * Content-Type: multipart/form-data
     * 
     * @param profileImage MultipartBody.Part - Kép fájl
     * @param authorization Bearer token
     * @return Response<ProfileResponseDto> - Frissített profil (új profileImageUrl-lel)
     * 
     * Response kódok:
     * - 200: Sikeres feltöltés
     * - 400: Hibás fájl
     * - 401: Nincs bejelentkezve
     */
    @Multipart
    @POST("profile/upload-profile-image")
    suspend fun uploadProfileImage(
        @Part profileImage: MultipartBody.Part,
        @Header("Authorization") authorization: String
    ): Response<ProfileResponseDto>
    
    /**
     * Kijelentkezés
     * 
     * Endpoint: POST /auth/local/logout
     * 
     * @param authorization Bearer token
     * @return Response<LogoutResponse> - Logout megerősítő üzenet
     * 
     * Response kódok:
     * - 200: Sikeres kijelentkezés
     * - 401: Nincs bejelentkezve
     */
    @POST("auth/local/logout")
    suspend fun logout(
        @Header("Authorization") authorization: String
    ): Response<LogoutResponse>
}
