package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.HabitResponseDto
import com.progress.habittracker.data.model.ProfileResponseDto
import com.progress.habittracker.data.model.UpdateProfileRequest
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Profile Repository
 * 
 * Repository pattern implementáció a profil-kezeléssel kapcsolatos műveletekhez.
 * Ez a réteg kezeli az adatforrások (API, local storage) közötti kommunikációt.
 * 
 * @property tokenManager Token kezelő a hitelesítéshez
 */
class ProfileRepository(
    private val tokenManager: TokenManager
) {
    
    /**
     * Profile API service instance
     */
    private val profileApi = RetrofitClient.profileApiService
    
    /**
     * Habit API service instance (user habits lekéréséhez)
     */
    private val habitApi = RetrofitClient.habitApiService
    
    /**
     * Saját profil lekérése
     * 
     * Flow-based API használatával reaktív adatkezelés.
     * 
     * @return Flow<Resource<ProfileResponseDto>> - Profil adatok Resource wrapper-ben
     * 
     * Resource állapotok:
     * - Loading: Betöltés folyamatban
     * - Success: Sikeres lekérés, profil adatok
     * - Error: Hiba történt (hibaüzenet)
     */
    fun getMyProfile(): Flow<Resource<ProfileResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = profileApi.getMyProfile(
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Profil nem található"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Profil frissítése
     * 
     * @param request UpdateProfileRequest - Frissítendő mezők (username, description)
     * @return Flow<Resource<ProfileResponseDto>> - Frissített profil
     */
    fun updateProfile(request: UpdateProfileRequest): Flow<Resource<ProfileResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = profileApi.updateProfile(
                request = request,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    400 -> emit(Resource.Error("Hibás adatok"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Profil kép feltöltése
     * 
     * @param imageFile Kép fájl (File objektum)
     * @return Flow<Resource<ProfileResponseDto>> - Frissített profil (új profileImageUrl-lel)
     */
    fun uploadProfileImage(imageFile: File): Flow<Resource<ProfileResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            // Multipart/form-data készítés
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "profileImage",
                imageFile.name,
                requestBody
            )
            
            val response = profileApi.uploadProfileImage(
                profileImage = multipartBody,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    400 -> emit(Resource.Error("Hibás fájl formátum"))
                    413 -> emit(Resource.Error("A fájl túl nagy"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Felhasználó habit-jeinek lekérése
     * 
     * @param userId User azonosító
     * @return Flow<Resource<List<HabitResponseDto>>> - User habit-jei
     */
    fun getUserHabits(userId: Int): Flow<Resource<List<HabitResponseDto>>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = habitApi.getHabitsByUserId(
                userId = userId,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val habits = response.body() ?: emptyList()
                emit(Resource.Success(habits))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Felhasználó nem található"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Kijelentkezés
     * 
     * @return Flow<Resource<Boolean>> - Sikeres logout = true
     */
    fun logout(): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                // Ha nincs token, már ki vagyunk jelentkezve
                emit(Resource.Success(true))
                return@flow
            }
            
            val response = profileApi.logout(
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                // Token-ek törlése local storage-ból
                tokenManager.clearTokens()
                
                emit(Resource.Success(true))
            } else {
                when (response.code()) {
                    401 -> {
                        // Token már invalid, törölhetjük
                        tokenManager.clearTokens()
                        emit(Resource.Success(true))
                    }
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
}
