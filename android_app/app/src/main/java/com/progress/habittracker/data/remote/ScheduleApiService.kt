package com.progress.habittracker.data.remote

import com.progress.habittracker.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Schedule API Service
 * 
 * Retrofit interface a schedule-okkal kapcsolatos API végpontokhoz.
 * Az összes endpoint bearer token authentikációt igényel.
 */
interface ScheduleApiService {
    
    /**
     * Schedule-ok lekérése adott napra
     * 
     * Backend endpoint: GET /schedule/day
     * 
     * Visszaadja az adott napi összes schedule-t a bejelentkezett felhasználó számára.
     * Ha nincs megadva dátum, akkor a mai napot használja.
     * 
     * @param date Dátum YYYY-MM-DD formátumban (pl. "2025-10-31")
     *             null esetén a mai nap
     * @param authorization Bearer token az Authorization header-ben
     * @return Response<List<ScheduleResponseDto>> - Schedule-ok listája
     * 
     * Response kódok:
     * - 200: Sikeres lekérés, üres lista ha nincs schedule
     * - 401: Nincs authentikáció vagy lejárt a token
     * - 400: Hibás dátum formátum
     */
    @GET("schedule/day")
    suspend fun getSchedulesByDay(
        @Query("date") date: String? = null,
        @Header("Authorization") authorization: String
    ): Response<List<ScheduleResponseDto>>
    
    /**
     * Egy konkrét schedule lekérése ID alapján
     * 
     * Backend endpoint: GET /schedule/{id}
     * 
     * @param id Schedule egyedi azonosítója
     * @param authorization Bearer token
     * @return Response<ScheduleResponseDto> - A schedule részletes adatai
     * 
     * Response kódok:
     * - 200: Sikeres lekérés
     * - 401: Nincs authentikáció
     * - 404: Schedule nem található vagy nincs hozzáférés
     */
    @GET("schedule/{id}")
    suspend fun getScheduleById(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String
    ): Response<ScheduleResponseDto>
    
    /**
     * Schedule státuszának frissítése
     * 
     * Backend endpoint: PATCH /schedule/{id}
     * 
     * Gyors státusz frissítés (Planned -> Completed, stb.)
     * 
     * @param id Schedule ID
     * @param status Új státusz (Planned, Completed, Skipped)
     * @param authorization Bearer token
     * @return Response<ScheduleResponseDto> - Frissített schedule
     * 
     * Response kódok:
     * - 200: Sikeres frissítés
     * - 401: Nincs authentikáció
     * - 404: Schedule nem található
     * - 403: Nincs jogosultság a módosításra
     */
    @PATCH("schedule/{id}")
    suspend fun updateScheduleStatus(
        @Path("id") id: Int,
        @Body status: Map<String, String>,
        @Header("Authorization") authorization: String
    ): Response<ScheduleResponseDto>
    
    /**
     * Schedule törlése
     * 
     * Backend endpoint: DELETE /schedule/{id}
     * 
     * @param id Schedule ID
     * @param authorization Bearer token
     * @return Response<Unit> - Nincs response body
     * 
     * Response kódok:
     * - 204: Sikeres törlés
     * - 401: Nincs authentikáció
     * - 404: Schedule nem található
     * - 403: Nincs jogosultság a törlésre
     */
    @DELETE("schedule/{id}")
    suspend fun deleteSchedule(
        @Path("id") id: Int,
        @Header("Authorization") authorization: String
    ): Response<Unit>
    
    // ==================== CREATE SCHEDULE ENDPOINTS ====================
    
    /**
     * Egyedi (egyszeri) schedule létrehozása
     * 
     * Backend endpoint: POST /schedule/custom
     * 
     * @param request CreateCustomScheduleRequest
     * @param authorization Bearer token
     * @return Response<ScheduleResponseDto> - Létrehozott schedule
     * 
     * Response kódok:
     * - 201: Sikeres létrehozás
     * - 401: Nincs authentikáció
     * - 400: Hibás request body
     */
    @POST("schedule/custom")
    suspend fun createCustomSchedule(
        @Body request: CreateCustomScheduleRequest,
        @Header("Authorization") authorization: String
    ): Response<ScheduleResponseDto>
    
    /**
     * Ismétlődő schedule-ok létrehozása (daily, weekdays, weekends)
     * 
     * Backend endpoint: POST /schedule/recurring
     * 
     * @param request CreateRecurringScheduleRequest
     * @param authorization Bearer token
     * @return Response<List<ScheduleResponseDto>> - Létrehozott schedule-ok listája
     * 
     * Response kódok:
     * - 201: Sikeres létrehozás
     * - 401: Nincs authentikáció
     * - 400: Hibás request body
     */
    @POST("schedule/recurring")
    suspend fun createRecurringSchedule(
        @Body request: CreateRecurringScheduleRequest,
        @Header("Authorization") authorization: String
    ): Response<List<ScheduleResponseDto>>
    
    /**
     * Ismétlődő schedule-ok létrehozása megadott napokra
     * 
     * Backend endpoint: POST /schedule/recurring/weekdays
     * 
     * @param request CreateWeekdayRecurringScheduleRequest
     * @param authorization Bearer token
     * @return Response<List<ScheduleResponseDto>> - Létrehozott schedule-ok listája
     * 
     * Response kódok:
     * - 201: Sikeres létrehozás
     * - 401: Nincs authentikáció
     * - 400: Hibás request body
     */
    @POST("schedule/recurring/weekdays")
    suspend fun createWeekdayRecurringSchedule(
        @Body request: CreateWeekdayRecurringScheduleRequest,
        @Header("Authorization") authorization: String
    ): Response<List<ScheduleResponseDto>>
}
