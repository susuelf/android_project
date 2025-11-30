package com.progress.habittracker.data.repository

// Helyi adattárolás és modellek
import com.progress.habittracker.data.local.TokenManager // Token kezelés (Auth token elérése)
import com.progress.habittracker.data.model.HabitResponseDto // Habit adatmodell
import com.progress.habittracker.data.model.ProfileResponseDto // Profil adatmodell
import com.progress.habittracker.data.model.UpdateProfileRequest // Profil frissítési kérés modell
// Hálózati kommunikáció
import com.progress.habittracker.data.remote.RetrofitClient // Retrofit kliens (API hívásokhoz)
import com.progress.habittracker.util.Resource // Eredmény wrapper (Loading, Success, Error)
// Kotlin Coroutines és Flow (Aszinkron adatkezelés)
import kotlinx.coroutines.flow.Flow // Adatfolyam interfész
import kotlinx.coroutines.flow.MutableSharedFlow // Megosztott adatfolyam (eseményekhez)
import kotlinx.coroutines.flow.collect // Adatfolyam gyűjtése
import kotlinx.coroutines.flow.first // Első elem lekérése (pl. token)
import kotlinx.coroutines.flow.flow // Flow builder
import kotlinx.coroutines.flow.onStart // Művelet indításkor
// OkHttp (Fájl feltöltéshez)
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

/**
 * Profile Repository
 * 
 * Ez az osztály felelős a profil adatok kezeléséért és az adatforrások (API) eléréséért.
 * A Repository Pattern-t valósítja meg, elrejtve az adatlekérés részleteit a ViewModel elől.
 * 
 * Főbb feladatai:
 * 1. Saját profil lekérése a szerverről.
 * 2. Profil adatainak frissítése.
 * 3. Profilkép feltöltése.
 * 4. Felhasználó szokásainak lekérése.
 * 
 * @property tokenManager A hitelesítési token eléréséhez szükséges, mivel minden API hívás hitelesített.
 */
class ProfileRepository(
    private val tokenManager: TokenManager
) {
    
    // API szolgáltatások inicializálása a Retrofit kliensen keresztül
    private val profileApi = RetrofitClient.profileApiService
    private val habitApi = RetrofitClient.habitApiService

    // Trigger a frissítéshez (opcionális, ha manuálisan akarjuk frissíteni az adatokat)
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    /**
     * Frissítés kérése
     * Eseményt küld a _refreshTrigger-re, ami kiválthatja az adatok újratöltését.
     */
    suspend fun refresh() {
        _refreshTrigger.emit(Unit)
    }
    
    /**
     * Saját profil lekérése
     * 
     * Aszinkron művelet, amely Flow-t ad vissza.
     * A Flow folyamatosan küldheti az állapotokat (Loading -> Success/Error).
     * 
     * Működés:
     * 1. Loading állapot küldése.
     * 2. Token lekérése a TokenManager-ből.
     * 3. API hívás végrehajtása (getMyProfile).
     * 4. Válasz feldolgozása és Success vagy Error állapot küldése.
     * 
     * @return Flow<Resource<ProfileResponseDto>> - A profil adatok vagy hibaüzenet.
     */
    fun getMyProfile(): Flow<Resource<ProfileResponseDto>> = flow {
        try {
            // 1. Jelezzük, hogy a betöltés elkezdődött
            emit(Resource.Loading())
            
            // 2. Token lekérése (felfüggesztett hívás)
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            // 3. API hívás
            val response = profileApi.getMyProfile(
                authorization = "Bearer $token"
            )
            
            // 4. Válasz kezelése
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                // Hibakódok szerinti kezelés
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Profil nem található"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            // Hálózati vagy egyéb kivétel kezelése
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Profil frissítése
     * 
     * Elküldi a módosított adatokat a szervernek.
     * 
     * @param request UpdateProfileRequest - A módosított adatok (pl. új név, leírás).
     * @return Flow<Resource<ProfileResponseDto>> - A frissített profil adatok.
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
     * A kiválasztott képet Multipart formátumban küldi el a szervernek.
     * 
     * @param imageFile A feltöltendő kép fájl (File objektum).
     * @return Flow<Resource<ProfileResponseDto>> - Frissített profil (új profileImageUrl-lel).
     */
    fun uploadProfileImage(imageFile: File): Flow<Resource<ProfileResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            // Multipart/form-data készítés a fájl feltöltéshez
            val requestBody = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
            val multipartBody = MultipartBody.Part.createFormData(
                "profileImage", // A szerver ezt a mezőnevet várja
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
     * Felhasználó szokásainak (Habits) lekérése
     * 
     * Ez a függvény figyeli a _refreshTrigger eseményt is, így ha meghívjuk a refresh()-t,
     * automatikusan újratölti az adatokat.
     * 
     * @param userId A felhasználó azonosítója.
     * @return Flow<Resource<List<HabitResponseDto>>> - A felhasználó szokásainak listája.
     */
    fun getUserHabits(userId: Int): Flow<Resource<List<HabitResponseDto>>> = flow {
        val token = tokenManager.accessToken.first()
        
        if (token.isNullOrEmpty()) {
            emit(Resource.Error("Nincs bejelentkezve"))
            return@flow
        }

        // Trigger figyelése: Azonnal lefut, majd minden refresh() híváskor újra
        val triggerFlow = flow {
            emit(Unit) // Azonnali első futtatás
            _refreshTrigger.collect { emit(Unit) }
        }

        triggerFlow.collect {
            try {
                // emit(Resource.Loading()) // Opcionális, ha nem akarunk villogást minden frissítésnél

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
    }
    
    /**
     * Kijelentkezés
     * 
     * Jelzi a szervernek a kijelentkezési szándékot (opcionális, token invalidálás),
     * majd törli a helyi tokeneket.
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
                tokenManager.clearAll()
                
                emit(Resource.Success(true))
            } else {
                when (response.code()) {
                    401 -> {
                        // Token már invalid, törölhetjük
                        tokenManager.clearAll()
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
