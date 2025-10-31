package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.*
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Habit Repository
 * 
 * Repository pattern implementáció a habit műveletekhez.
 * Kezeli az API hívásokat és a token management-et.
 */
class HabitRepository(
    private val tokenManager: TokenManager
) {
    
    private val habitApiService = RetrofitClient.habitApiService
    
    /**
     * Összes habit lekérése
     * 
     * @return Flow<Resource<List<HabitResponseDto>>>
     */
    fun getHabits(): Flow<Resource<List<HabitResponseDto>>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = tokenManager.accessToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = habitApiService.getHabits("Bearer $token")
            
            when {
                response.isSuccessful && response.body() != null -> {
                    val habits = response.body()!!
                    emit(Resource.Success(habits))
                }
                response.code() == 401 -> {
                    emit(Resource.Error("Lejárt a session, kérlek jelentkezz be újra"))
                }
                response.code() == 404 -> {
                    emit(Resource.Success(emptyList())) // Nincs habit
                }
                else -> {
                    emit(Resource.Error("Hiba történt: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Új habit létrehozása
     * 
     * @param request CreateHabitRequest
     * @return Flow<Resource<HabitResponseDto>>
     */
    fun createHabit(request: CreateHabitRequest): Flow<Resource<HabitResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = tokenManager.accessToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = habitApiService.createHabit("Bearer $token", request)
            
            when {
                response.isSuccessful && response.body() != null -> {
                    emit(Resource.Success(response.body()!!))
                }
                response.code() == 401 -> {
                    emit(Resource.Error("Lejárt a session, kérlek jelentkezz be újra"))
                }
                response.code() == 400 -> {
                    emit(Resource.Error("Hibás adatok"))
                }
                else -> {
                    emit(Resource.Error("Hiba történt: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba"))
        }
    }
    
    /**
     * Habit kategóriák lekérése
     * 
     * @return Flow<Resource<List<HabitCategoryResponseDto>>>
     */
    fun getCategories(): Flow<Resource<List<HabitCategoryResponseDto>>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = tokenManager.accessToken.first()
            if (token.isNullOrEmpty()) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = habitApiService.getCategories("Bearer $token")
            
            when {
                response.isSuccessful && response.body() != null -> {
                    emit(Resource.Success(response.body()!!))
                }
                response.code() == 401 -> {
                    emit(Resource.Error("Lejárt a session, kérlek jelentkezz be újra"))
                }
                else -> {
                    emit(Resource.Error("Hiba történt: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Ismeretlen hiba"))
        }
    }
}
