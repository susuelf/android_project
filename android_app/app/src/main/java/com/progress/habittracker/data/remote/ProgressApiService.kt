package com.progress.habittracker.data.remote

import com.progress.habittracker.data.model.CreateProgressRequest
import com.progress.habittracker.data.model.ProgressResponseDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Progress API Service
 * 
 * Progress (haladás) kezeléshez szükséges API végpontok
 * 
 * Backend endpoint: /progress
 */
interface ProgressApiService {
    
    /**
     * Progress létrehozása egy schedule-hoz
     * 
     * POST /progress
     * 
     * @param authorization Bearer token az authentikációhoz
     * @param request CreateProgressRequest - scheduleId, date, loggedTime, notes, isCompleted
     * @return ProgressResponseDto - Létrehozott progress adatai
     * 
     * Response: 201 Created
     */
    @POST("progress")
    suspend fun createProgress(
        @Header("Authorization") authorization: String,
        @Body request: CreateProgressRequest
    ): Response<ProgressResponseDto>
}
