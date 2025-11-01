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

// ==================== CREATE SCHEDULE REQUEST DTOs ====================

/**
 * Repeat Pattern - Schedule ismétlődési minta
 */
enum class RepeatPattern {
    @SerializedName("none")
    None,
    
    @SerializedName("daily")
    Daily,
    
    @SerializedName("weekdays")
    Weekdays,
    
    @SerializedName("weekends")
    Weekends
}

/**
 * Create Custom Schedule Request
 * 
 * Egyedi (egyszeri) schedule létrehozása
 * 
 * @property habitId Habit azonosító
 * @property date Dátum (YYYY-MM-DD)
 * @property startTime Kezdési időpont (HH:mm:ss)
 * @property endTime Befejezési időpont (HH:mm:ss, opcionális)
 * @property durationMinutes Időtartam percben (opcionális)
 * @property isCustom Egyedi schedule-e (default: true)
 * @property participantIds Résztvevők ID-i (opcionális)
 * @property notes Jegyzetek (opcionális)
 */
data class CreateCustomScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    
    @SerializedName("date")
    val date: String, // YYYY-MM-DD
    
    @SerializedName("start_time")
    val startTime: String, // HH:mm:ss
    
    @SerializedName("end_time")
    val endTime: String? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * Create Recurring Schedule Request
 * 
 * Ismétlődő schedule létrehozása (daily, weekdays, weekends)
 * 
 * @property habitId Habit azonosító
 * @property startTime Kezdési időpont (HH:mm:ss)
 * @property repeatPattern Ismétlődési minta (none, daily, weekdays, weekends)
 * @property endTime Befejezési időpont (HH:mm:ss, opcionális)
 * @property durationMinutes Időtartam percben (opcionális)
 * @property repeatDays Hány napra ismétlődjön (default: 30)
 * @property isCustom Egyedi schedule-e (default: true)
 * @property participantIds Résztvevők ID-i (opcionális)
 * @property notes Jegyzetek (opcionális)
 */
data class CreateRecurringScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    
    @SerializedName("start_time")
    val startTime: String, // HH:mm:ss
    
    @SerializedName("repeatPattern")
    val repeatPattern: RepeatPattern = RepeatPattern.None,
    
    @SerializedName("end_time")
    val endTime: String? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    
    @SerializedName("repeatDays")
    val repeatDays: Int = 30,
    
    @SerializedName("is_custom")
    val isCustom: Boolean = true,
    
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * Create Weekday Recurring Schedule Request
 * 
 * Ismétlődő schedule létrehozása megadott napokra
 * 
 * @property habitId Habit azonosító
 * @property startTime Kezdési időpont (HH:mm:ss)
 * @property daysOfWeek Hét napjai (1=Monday ... 7=Sunday)
 * @property numberOfWeeks Hány hétre (default: 4)
 * @property endTime Befejezési időpont (HH:mm:ss, opcionális)
 * @property durationMinutes Időtartam percben (opcionális)
 * @property participantIds Résztvevők ID-i (opcionális)
 * @property notes Jegyzetek (opcionális)
 */
data class CreateWeekdayRecurringScheduleRequest(
    @SerializedName("habitId")
    val habitId: Int,
    
    @SerializedName("start_time")
    val startTime: String, // HH:mm:ss
    
    @SerializedName("daysOfWeek")
    val daysOfWeek: List<Int>, // 1=Monday, 7=Sunday
    
    @SerializedName("numberOfWeeks")
    val numberOfWeeks: Int = 4,
    
    @SerializedName("end_time")
    val endTime: String? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)

/**
 * Create Progress Request
 * 
 * Progress (haladás) létrehozása egy schedule-hoz
 * 
 * @property scheduleId Schedule azonosító
 * @property date Dátum (YYYY-MM-DD formátum)
 * @property loggedTime Eltöltött idő percben (opcionális)
 * @property notes Jegyzetek (opcionális)
 * @property isCompleted Befejezett-e (default: true)
 */
data class CreateProgressRequest(
    @SerializedName("scheduleId")
    val scheduleId: Int,
    
    @SerializedName("date")
    val date: String, // YYYY-MM-DD
    
    @SerializedName("logged_time")
    val loggedTime: Int? = null,
    
    @SerializedName("notes")
    val notes: String? = null,
    
    @SerializedName("is_completed")
    val isCompleted: Boolean = true
)

/**
 * Update Schedule Request
 * 
 * Schedule módosítása
 * 
 * @property startTime Kezdési időpont (ISO 8601 format, opcionális)
 * @property endTime Befejezési időpont (ISO 8601 format, opcionális)
 * @property durationMinutes Időtartam percben (opcionális)
 * @property status Schedule státusza (Planned/Completed/Skipped, opcionális)
 * @property date Dátum (ISO 8601 format, opcionális)
 * @property isCustom Egyedi schedule-e (opcionális)
 * @property participantIds Résztvevők ID listája (opcionális)
 * @property notes Jegyzetek (opcionális)
 */
data class UpdateScheduleRequest(
    @SerializedName("start_time")
    val startTime: String? = null,
    
    @SerializedName("end_time")
    val endTime: String? = null,
    
    @SerializedName("duration_minutes")
    val durationMinutes: Int? = null,
    
    @SerializedName("status")
    val status: String? = null, // "Planned", "Completed", "Skipped"
    
    @SerializedName("date")
    val date: String? = null,
    
    @SerializedName("is_custom")
    val isCustom: Boolean? = null,
    
    @SerializedName("participantIds")
    val participantIds: List<Int>? = null,
    
    @SerializedName("notes")
    val notes: String? = null
)
