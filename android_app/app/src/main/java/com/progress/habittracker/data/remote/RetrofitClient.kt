package com.progress.habittracker.data.remote

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * RetrofitClient - Retrofit kliens konfiguráció
 * 
 * Ez az object felelős a Retrofit instance létrehozásáért és konfigurálásáért.
 * Singleton pattern-t használunk, hogy csak egy instance legyen.
 */
object RetrofitClient {
    
    /**
     * Backend API alap URL
     * 
     * FONTOS: Ezt módosítsd a saját backend címedre!
     * - Android emulatorhoz Docker backend: használd a számítógép IP címét (WiFi/Ethernet)
     *   Példa: http://192.168.1.100:8080/ (cseréld le a saját IP-dre!)
     * - Fizikai Android eszközhöz: http://YOUR_LOCAL_IP:8080/
     * - Production: https://your-backend-url.com/
     * 
     * MEGJEGYZÉS: A 10.0.2.2 NEM működik Docker Desktop-pal Windows-on,
     * mert a Docker egy virtuális hálózatban fut. Használd a gép tényleges IP címét!
     */
    private const val BASE_URL = "http://192.168.197.132:8080/"
    
    /**
     * Gson instance - JSON <-> Kotlin object konverzióhoz
     * 
     * setLenient(): Megengedőbb JSON parsing
     */
    private val gson: Gson by lazy {
        GsonBuilder()
            .setLenient()
            .create()
    }
    
    /**
     * HTTP Logging Interceptor - API hívások logolásához
     * 
     * Development környezetben BODY szintű logolás
     * Production környezetben ezt érdemes NONE-ra állítani
     */
    private val loggingInterceptor: HttpLoggingInterceptor by lazy {
        HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }
    
    /**
     * OkHttpClient - HTTP kliens konfiguráció
     * 
     * - Logging interceptor hozzáadása
     * - Timeout beállítások (30 mp kapcsolódási, olvasási, írási timeout)
     */
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }
    
    /**
     * Retrofit instance
     * 
     * - BASE_URL beállítása
     * - OkHttpClient használata
     * - Gson converter használata JSON kezeléshez
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }
    
    /**
     * AuthApiService instance létrehozása
     * 
     * @return AuthApiService implementáció
     */
    val authApiService: AuthApiService by lazy {
        retrofit.create(AuthApiService::class.java)
    }
    
    // TODO: Később hozzáadandó API szolgáltatások
    // val scheduleApiService: ScheduleApiService by lazy { ... }
    // val habitApiService: HabitApiService by lazy { ... }
    // val progressApiService: ProgressApiService by lazy { ... }
    // val profileApiService: ProfileApiService by lazy { ... }
}
