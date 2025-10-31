package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.*
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Schedule Repository
 * 
 * Repository pattern implementáció a schedule-okkal kapcsolatos műveletekhez.
 * Ez a réteg kezeli az adatforrások (API, local storage) közötti kommunikációt.
 * 
 * @property tokenManager Token kezelő a hitelesítéshez
 */
class ScheduleRepository(
    private val tokenManager: TokenManager
) {
    
    /**
     * Schedule API service instance
     */
    private val scheduleApi = RetrofitClient.scheduleApiService
    
    /**
     * Schedule-ok lekérése egy adott napra
     * 
     * Flow-based API használatával reaktív adatkezelés.
     * 
     * @param date Dátum YYYY-MM-DD formátumban, null = mai nap
     * @return Flow<Resource<List<ScheduleResponseDto>>> - Schedule-ok listája Resource wrapper-ben
     * 
     * Resource állapotok:
     * - Loading: Betöltés folyamatban
     * - Success: Sikeres lekérés, schedule lista
     * - Error: Hiba történt (hibaüzenet)
     */
    fun getSchedulesByDay(date: String? = null): Flow<Resource<List<ScheduleResponseDto>>> = flow {
        try {
            // 1. Emit Loading state
            emit(Resource.Loading())
            
            // 2. Token lekérése
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                // Nincs token - felhasználó nincs bejelentkezve
                emit(Resource.Error("Nincs bejelentkezve. Kérlek jelentkezz be!"))
                return@flow
            }
            
            // 3. API hívás
            val response = scheduleApi.getSchedulesByDay(
                date = date,
                authorization = "Bearer $token"
            )
            
            // 4. Response feldolgozás
            if (response.isSuccessful) {
                val schedules = response.body() ?: emptyList()
                
                // Rendezés start_time szerint (időrendi sorrend)
                val sortedSchedules = schedules.sortedBy { it.startTime }
                
                emit(Resource.Success(sortedSchedules))
            } else {
                // HTTP hiba
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet. Kérlek jelentkezz be újra!"))
                    400 -> emit(Resource.Error("Hibás dátum formátum"))
                    404 -> emit(Resource.Error("A kért schedule-ok nem találhatók"))
                    500 -> emit(Resource.Error("Szerver hiba. Próbáld újra később!"))
                    else -> emit(Resource.Error("Hiba történt: ${'$'}{response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            // Hálózati hiba vagy más exception
            emit(Resource.Error(
                message = e.localizedMessage ?: "Ismeretlen hiba történt",
                data = null
            ))
        }
    }
    
    /**
     * Egy konkrét schedule lekérése ID alapján
     * 
     * @param scheduleId Schedule egyedi azonosítója
     * @return Flow<Resource<ScheduleResponseDto>> - Schedule adatai Resource wrapper-ben
     */
    fun getScheduleById(scheduleId: Int): Flow<Resource<ScheduleResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.getScheduleById(
                id = scheduleId,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val schedule = response.body()
                if (schedule != null) {
                    emit(Resource.Success(schedule))
                } else {
                    emit(Resource.Error("A schedule adatai nem találhatók"))
                }
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("A schedule nem található"))
                    403 -> emit(Resource.Error("Nincs jogosultságod megtekinteni ezt a schedule-t"))
                    else -> emit(Resource.Error("Hiba: ${'$'}{response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Schedule státuszának frissítése
     * 
     * Gyors státusz váltás (pl. Planned -> Completed checkbox-szal)
     * 
     * @param scheduleId Schedule ID
     * @param newStatus Új státusz ("Planned", "Completed", "Skipped")
     * @return Flow<Resource<ScheduleResponseDto>> - Frissített schedule
     */
    fun updateScheduleStatus(
        scheduleId: Int, 
        newStatus: String
    ): Flow<Resource<ScheduleResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val statusMap = mapOf("status" to newStatus)
            
            val response = scheduleApi.updateScheduleStatus(
                id = scheduleId,
                status = statusMap,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val updatedSchedule = response.body()
                if (updatedSchedule != null) {
                    emit(Resource.Success(updatedSchedule))
                } else {
                    emit(Resource.Error("Frissítés sikertelen"))
                }
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Schedule nem található"))
                    403 -> emit(Resource.Error("Nincs jogosultságod módosítani"))
                    else -> emit(Resource.Error("Hiba: ${'$'}{response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Schedule törlése
     * 
     * @param scheduleId Schedule ID
     * @return Flow<Resource<Boolean>> - Sikeres törlés = true
     */
    fun deleteSchedule(scheduleId: Int): Flow<Resource<Boolean>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.deleteSchedule(
                id = scheduleId,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                // 204 No Content - sikeres törlés
                emit(Resource.Success(true))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Schedule nem található"))
                    403 -> emit(Resource.Error("Nincs jogosultságod törölni"))
                    else -> emit(Resource.Error("Hiba: ${'$'}{response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Egyszeri (custom) schedule l�trehoz�sa
     * 
     * @param request CreateCustomScheduleRequest - Schedule adatai
     * @return Flow<Resource<ScheduleResponseDto>> - L�trehozott schedule
     */
    fun createCustomSchedule(request: CreateCustomScheduleRequest): Flow<Resource<ScheduleResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.createCustomSchedule(
                request = request,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val schedule = response.body()
                if (schedule != null) {
                    emit(Resource.Success(schedule))
                } else {
                    emit(Resource.Error("Schedule l�trehoz�sa sikertelen"))
                }
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lej�rt a munkamenet"))
                    400 -> emit(Resource.Error("Hib�s adatok"))
                    404 -> emit(Resource.Error("A habit nem tal�lhat�"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Ism�tl�d� schedule l�trehoz�sa
     * 
     * @param request CreateRecurringScheduleRequest - Ism�tl�d� schedule adatai
     * @return Flow<Resource<List<ScheduleResponseDto>>> - L�trehozott schedule-ok list�ja
     */
    fun createRecurringSchedule(request: CreateRecurringScheduleRequest): Flow<Resource<List<ScheduleResponseDto>>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.createRecurringSchedule(
                request = request,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val schedules = response.body()
                if (schedules != null) {
                    emit(Resource.Success(schedules))
                } else {
                    emit(Resource.Error("Schedule-ok l�trehoz�sa sikertelen"))
                }
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lej�rt a munkamenet"))
                    400 -> emit(Resource.Error("Hib�s adatok"))
                    404 -> emit(Resource.Error("A habit nem tal�lhat�"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
}
