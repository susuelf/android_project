package com.progress.habittracker.data.model

import com.google.gson.annotations.SerializedName

/**
 * Habit List Response DTO
 * 
 * GET /habit végpontból kapott válasz
 * Tartalmazza az összes habit-et
 */
typealias HabitListResponse = List<HabitResponseDto>

/**
 * Create Habit Request
 * 
 * POST /habit - Új habit létrehozása
 * 
 * @property name Habit neve (pl. "Reggeli futás")
 * @property categoryId Kategória azonosító
 * @property goal Cél leírása (pl. "10 alkalommal 2 hét alatt")
 * @property description Habit leírása (opcionális)
 */
data class CreateHabitRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("categoryId")
    val categoryId: Int,
    
    @SerializedName("goal")
    val goal: String,
    
    @SerializedName("description")
    val description: String? = null
)

/**
 * Habit Categories Response
 * 
 * GET /habit/categories végpontból kapott válasz
 */
typealias HabitCategoriesResponse = List<HabitCategoryResponseDto>
