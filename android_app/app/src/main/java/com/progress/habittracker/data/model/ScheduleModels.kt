package com.progress.habittracker.data.model

import com.google.gson.annotations.SerializedName

/**
 * Schedule Status - A schedule lehetséges állapotai
 * 
 * @property Planned Tervezett - még nem történt meg
 * @property Completed Befejezett - sikeresen teljesítve
 * @property Skipped Kihagyott - szándékosan kihagyva
 */
enum class ScheduleStatus {
    @SerializedName("Planned")
    Planned,
    
    @SerializedName("Completed")
    Completed,
    
    @SerializedName("Skipped")
    Skipped
}

/**
 * Habit Category Response DTO
 * 
 * A habit kategóriák alapadatai (pl. Sport, Tanulás, stb.)
 * 
 * @property id Kategória egyedi azonosítója
 * @property name Kategória neve
 * @property iconUrl Kategória ikon URL-je (opcionális)
 */
data class HabitCategoryResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("iconUrl")
    val iconUrl: String?
)

/**
 * Habit Response DTO
 * 
 * Egy szokás (habit) részletes adatai
 * 
 * @property id Habit egyedi azonosítója
 * @property name Habit neve (pl. "Reggeli futás")
 * @property description Habit leírása (pl. "2km futás minden reggel")
 * @property goal Habit célja (pl. "10 alkalommal 2 hét alatt")
 * @property category Habit kategória adatai
 * @property userId A habit tulajdonosának user ID-ja
 * @property createdAt Létrehozás időpontja (ISO 8601 formátum)
 */
data class HabitResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("goal")
    val goal: String?,
    
    @SerializedName("category")
    val category: HabitCategoryResponseDto,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Progress Response DTO
 * 
 * Egy schedule-hoz tartozó előrehaladás/progress rekord
 * 
 * @property id Progress egyedi azonosítója
 * @property scheduleId A kapcsolódó schedule ID-ja
 * @property date Progress dátuma (YYYY-MM-DD formátum)
 * @property loggedTime Eltöltött idő percekben
 * @property notes Opcionális megjegyzések
 * @property isCompleted Teljesítve van-e
 * @property createdAt Létrehozás időpontja
 */
data class ProgressResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("scheduleId")
    val scheduleId: Int,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("logged_time")
    val loggedTime: Int?,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("is_completed")
    val isCompleted: Boolean,
    
    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * Participant Response DTO
 * 
 * Schedule-hoz rendelt résztvevő/partner adatai
 * 
 * @property id User ID
 * @property username Felhasználónév
 * @property email Email cím
 * @property profileImageUrl Profil kép URL (opcionális)
 */
data class ParticipantResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("username")
    val username: String,
    
    @SerializedName("email")
    val email: String,
    
    @SerializedName("profileImageUrl")
    val profileImageUrl: String?
)

/**
 * Schedule Response DTO
 * 
 * Egy schedule (ütemezett habit esemény) teljes adatai
 * Backend endpoint: GET /schedule/{id} vagy GET /schedule/day
 * 
 * @property id Schedule egyedi azonosítója
 * @property habitId Kapcsolódó habit ID-ja
 * @property habit Kapcsolódó habit részletes adatai
 * @property userId Schedule tulajdonosának user ID-ja
 * @property date Schedule dátuma (YYYY-MM-DD formátum)
 * @property startTime Kezdés időpontja (HH:mm formátum vagy teljes ISO 8601)
 * @property endTime Befejezés időpontja (opcionális)
 * @property durationMinutes Tervezett időtartam percekben (opcionális)
 * @property status Schedule állapota (Planned, Completed, Skipped)
 * @property isCustom Egyedi schedule-e (nem ismétlődő)
 * @property notes Opcionális megjegyzések
 * @property participants Résztvevők listája (opcionális)
 * @property progress Progress rekordok listája (opcionális)
 * @property createdAt Létrehozás időpontja
 */
data class ScheduleResponseDto(
    @SerializedName("id")
    val id: Int,
    
    @SerializedName("habitId")
    val habitId: Int,
    
    @SerializedName("habit")
    val habit: HabitResponseDto,
    
    @SerializedName("userId")
    val userId: Int,
    
    @SerializedName("date")
    val date: String,
    
    @SerializedName("start_time")
    val startTime: String,
    
    @SerializedName("end_time")
    val endTime: String?,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int?,
    
    @SerializedName("status")
    val status: ScheduleStatus,
    
    @SerializedName("is_custom")
    val isCustom: Boolean,
    
    @SerializedName("notes")
    val notes: String?,
    
    @SerializedName("participants")
    val participants: List<ParticipantResponseDto>?,
    
    @SerializedName("progress")
    val progress: List<ProgressResponseDto>?,
    
    @SerializedName("createdAt")
    val createdAt: String
)
