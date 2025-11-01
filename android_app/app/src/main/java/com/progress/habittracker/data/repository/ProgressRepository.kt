package com.progress.habittracker.data.repository

import com.progress.habittracker.data.local.TokenManager
import com.progress.habittracker.data.model.CreateProgressRequest
import com.progress.habittracker.data.model.ProgressResponseDto
import com.progress.habittracker.data.remote.ProgressApiService
import com.progress.habittracker.data.remote.RetrofitClient
import com.progress.habittracker.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

/**
 * Progress Repository
 * 
 * Progress (haladás) kezeléshez szükséges üzleti logika
 * Repository pattern használatával elválasztja az API hívásokat
 * 
 * @property tokenManager Token manager a token-ek eléréséhez
 */
class ProgressRepository(
    private val tokenManager: TokenManager
) {
    private val progressApi: ProgressApiService = RetrofitClient.progressApi
    
    /**
     * Progress létrehozása egy schedule-hoz
     * 
     * @param request CreateProgressRequest
     * @return Flow<Resource<ProgressResponseDto>>
     */
    fun createProgress(request: CreateProgressRequest): Flow<Resource<ProgressResponseDto>> = flow {
        emit(Resource.Loading())
        
        try {
            val token = tokenManager.getAccessToken().first()
            if (token == null) {
                emit(Resource.Error("Nincs bejelentkezve"))
                return@flow
            }
            
            val response = progressApi.createProgress(
                authorization = "Bearer $token",
                request = request
            )
            
            when {
                response.isSuccessful && response.body() != null -> {
                    emit(Resource.Success(response.body()!!))
                }
                response.code() == 401 -> {
                    emit(Resource.Error("Hitelesítés szükséges"))
                }
                response.code() == 400 -> {
                    emit(Resource.Error("Hibás adatok"))
                }
                response.code() == 404 -> {
                    emit(Resource.Error("Schedule nem található"))
                }
                else -> {
                    emit(Resource.Error("Hiba történt: ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Ismeretlen hiba"))
        }
    }
}
