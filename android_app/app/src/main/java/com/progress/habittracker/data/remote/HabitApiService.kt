package com.progress.habittracker.data.remote

import com.progress.habittracker.data.model.*
import retrofit2.Response
import retrofit2.http.*

/**
 * Habit API Service
 * 
 * Habit-ekhez kapcsolódó API végpontok
 * 
 * Funkciók:
 * - Habit lista lekérése
 * - Új habit létrehozása
 * - Habit kategóriák lekérése
 */
interface HabitApiService {
    
    /**
     * GET /habit
     * 
     * Összes habit lekérése
     * 
     * @param authorization Bearer token
     * @return HabitListResponse (List<HabitResponseDto>)
     */
    @GET("habit")
    suspend fun getHabits(
        @Header("Authorization") authorization: String
    ): Response<HabitListResponse>
    
    /**
     * POST /habit
     * 
     * Új habit létrehozása
     * 
     * @param authorization Bearer token
     * @param request CreateHabitRequest
     * @return HabitResponseDto
     */
    @POST("habit")
    suspend fun createHabit(
        @Header("Authorization") authorization: String,
        @Body request: CreateHabitRequest
    ): Response<HabitResponseDto>
    
    /**
     * GET /habit/categories
     * 
     * Habit kategóriák lekérése
     * 
     * @param authorization Bearer token
     * @return HabitCategoriesResponse (List<HabitCategoryResponseDto>)
     */
    @GET("habit/categories")
    suspend fun getCategories(
        @Header("Authorization") authorization: String
    ): Response<HabitCategoriesResponse>
}
