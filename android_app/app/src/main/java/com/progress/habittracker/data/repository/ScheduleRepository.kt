package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.*
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.util.Resource
import com.progress.habittracker.util.ScheduleStateCalculator
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.collect
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Helper class for weekly stats
 */
data class HabitStat(
    val habitId: Int,
    val averageProgress: Float
)

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

    // Trigger a frissítéshez (mivel nincs DAO/Room, ezzel szimuláljuk a reaktív adatfolyamot)
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 1)

    /**
     * Frissítés kérése
     */
    suspend fun refresh() {
        _refreshTrigger.emit(Unit)
    }
    
    /**
     * Schedule-ok lekérése egy adott napra
     * 
     * Flow-based API használatával reaktív adatkezelés.
     * A Flow automatikusan frissül, ha a refresh() metódust meghívják.
     * 
     * @param date Dátum YYYY-MM-DD formátumban, null = mai nap
     * @return Flow<Resource<List<ScheduleResponseDto>>> - Schedule-ok listája Resource wrapper-ben
     */
    fun getSchedulesByDay(date: String? = null): Flow<Resource<List<ScheduleResponseDto>>> = flow {
        // Token lekérése egyszer
        val token = tokenManager.accessToken.first()
        
        if (token.isNullOrEmpty()) {
            emit(Resource.Error("Nincs bejelentkezve. Kérlek jelentkezz be!"))
            return@flow
        }

        // Trigger figyelése és API hívás végrehajtása minden triggerre
        // A flow { ... } blokkban a collect felfüggeszti a futást, így "élő" marad a flow
        val triggerFlow = flow {
            emit(Unit) // Azonnali első futtatás
            _refreshTrigger.collect { emit(Unit) } // Későbbi frissítések
        }

        triggerFlow.collect {
            try {
                // Loading state minden frissítésnél (opcionális, de jó visszajelzés)
                // emit(Resource.Loading()) 
                // Ha túl sok a villogás, ezt ki lehet venni, vagy csak az elsőnél hagyni

                val response = scheduleApi.getSchedulesByDay(
                    date = date,
                    authorization = "Bearer $token"
                )
                
                if (response.isSuccessful) {
                    val schedules = response.body() ?: emptyList()
                    val sortedSchedules = schedules.sortedBy { it.startTime }
                    emit(Resource.Success(sortedSchedules))
                } else {
                    when (response.code()) {
                        401 -> emit(Resource.Error("Lejárt a munkamenet. Kérlek jelentkezz be újra!"))
                        400 -> emit(Resource.Error("Hibás dátum formátum"))
                        404 -> emit(Resource.Error("A kért schedule-ok nem találhatók"))
                        500 -> emit(Resource.Error("Szerver hiba. Próbáld újra később!"))
                        else -> emit(Resource.Error("Hiba történt: ${'$'}{response.message()}"))
                    }
                }
            } catch (e: Exception) {
                emit(Resource.Error(
                    message = e.localizedMessage ?: "Ismeretlen hiba történt",
                    data = null
                ))
            }
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
     * Schedule-ok lekérése habit ID alapján
     * 
     * @param habitId Habit egyedi azonosítója
     * @return Flow<Resource<List<ScheduleResponseDto>>>
     */
    fun getSchedulesByHabitId(habitId: Int): Flow<Resource<List<ScheduleResponseDto>>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.getSchedulesByHabitId(
                habitId = habitId,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful) {
                val schedules = response.body() ?: emptyList()
                emit(Resource.Success(schedules))
            } else {
                emit(Resource.Error("Hiba a schedule-ok lekérésekor: ${'$'}{response.message()}"))
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
     * Schedule teljes frissítése
     * 
     * @param scheduleId Schedule ID
     * @param request UpdateScheduleRequest - Frissítendő mezők
     * @return Flow<Resource<ScheduleResponseDto>> - Frissített schedule
     */
    fun updateSchedule(
        scheduleId: Int,
        request: UpdateScheduleRequest
    ): Flow<Resource<ScheduleResponseDto>> = flow {
        try {
            emit(Resource.Loading())
            
            val token = tokenManager.accessToken.first()
            
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = scheduleApi.updateSchedule(
                id = scheduleId,
                request = request,
                authorization = "Bearer $token"
            )
            
            if (response.isSuccessful && response.body() != null) {
                emit(Resource.Success(response.body()!!))
            } else {
                when (response.code()) {
                    401 -> emit(Resource.Error("Lejárt a munkamenet"))
                    404 -> emit(Resource.Error("Schedule nem található"))
                    400 -> emit(Resource.Error("Hibás adatok"))
                    403 -> emit(Resource.Error("Nincs jogosultságod módosítani"))
                    else -> emit(Resource.Error("Hiba: ${response.message()}"))
                }
            }
            
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Egyszeri (custom) schedule létrehozása
     * 
     * @param request CreateCustomScheduleRequest - Schedule adatai
     * @return Flow<Resource<ScheduleResponseDto>> - Létrehozott schedule
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

    /**
     * Heti statisztikák lekérése
     * 
     * Lekéri a megadott habit-ekhez tartozó schedule-okat, szűri őket a megadott dátumtartományra,
     * és kiszámolja az átlagos teljesítettséget.
     * 
     * @param habitIds Habit ID-k listája
     * @param startOfWeek Hét kezdő napja (Hétfő)
     * @param endOfWeek Hét utolsó napja (Vasárnap)
     * @return Flow<List<HabitStat>>
     */
    fun getWeeklyStats(
        habitIds: List<Int>,
        startOfWeek: LocalDate,
        endOfWeek: LocalDate
    ): Flow<List<HabitStat>> = flow {
        val token = tokenManager.accessToken.first()
        if (token.isNullOrEmpty()) {
            emit(emptyList())
            return@flow
        }

        // Trigger figyelése
        val triggerFlow = flow {
            emit(Unit) // Azonnali első futtatás
            _refreshTrigger.collect { emit(Unit) }
        }

        triggerFlow.collect {
            val stats = mutableListOf<HabitStat>()
            val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            // Mivel nincs batch endpoint, minden habit-re külön lekérjük (ez nem optimális, de az API ezt engedi)
            // Párhuzamosítás nélkül, egyszerűen
            for (habitId in habitIds) {
                try {
                    val response = scheduleApi.getSchedulesByHabitId(
                        habitId = habitId,
                        authorization = "Bearer $token"
                    )

                    if (response.isSuccessful) {
                        val allSchedules = response.body() ?: emptyList()
                        
                        // Szűrés dátumra
                        val weeklySchedules = allSchedules.filter { schedule ->
                            try {
                                // A backend dátum formátuma: YYYY-MM-DDT... vagy YYYY-MM-DD
                                // Feltételezzük, hogy a date mező string, parse-oljuk
                                val scheduleDate = LocalDate.parse(schedule.date.take(10), dateFormatter)
                                !scheduleDate.isBefore(startOfWeek) && !scheduleDate.isAfter(endOfWeek)
                            } catch (e: Exception) {
                                false
                            }
                        }

                        if (weeklySchedules.isNotEmpty()) {
                            val totalScore = weeklySchedules.sumOf { schedule ->
                                val uiState = ScheduleStateCalculator.calculate(schedule)
                                // Ha Completed, akkor 1.0, egyébként a százalék
                                if (schedule.status == ScheduleStatus.Completed) {
                                    1.0
                                } else {
                                    (uiState.progressPercentage.toDouble() / 100.0)
                                }
                            }
                            val average = totalScore / weeklySchedules.size.toDouble()
                            stats.add(HabitStat(habitId, average.toFloat()))
                        } else {
                            // Ha nincs schedule a héten, akkor nem adjuk hozzá (vagy 0-val)
                            // A specifikáció szerint: "Ha NEM szerepel (mert ezen a héten nem volt beütemezve), akkor a progress legyen 0."
                            // Itt most nem adjuk hozzá, a ViewModel majd kezeli a hiányzókat 0-ként
                        }
                    }
                } catch (e: Exception) {
                    // Hiba esetén skip
                }
            }
            emit(stats)
        }
    }
}
