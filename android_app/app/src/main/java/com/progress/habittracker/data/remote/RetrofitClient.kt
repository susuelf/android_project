package com.progress.habittracker.data.remote

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.progress.habittracker.util.NetworkUtils
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
 * 
 * FONTOS: Az initialize(context) metódust meg kell hívni az Application onCreate()-ben,
 * hogy a dinamikus IP felismerés működjön!
 */
object RetrofitClient {
    
    /**
     * Backend API alap URL - Dinamikusan generálva
     * 
     * A NetworkUtils automatikusan megtalálja a gateway IP címet:
     * - WiFi DHCP gateway lekérése
     * - Fallback: device IP alapján becslés
     * - Fallback: emulator default (10.0.2.2)
     */
    private var baseUrl: String = "http://10.0.2.2:8080/" // Default fallback
    
    /**
     * Inicializálás - Android Context szükséges a dinamikus IP felismeréshez
     * 
     * FONTOS: Ezt meg kell hívni az Application osztály onCreate() metódusában!
     * 
     * @param context Application context
     */
    fun initialize(context: Context) {
        baseUrl = NetworkUtils.getBackendBaseUrl(context)
        android.util.Log.d("RetrofitClient", "Backend URL initialized: $baseUrl")
    }
    
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
     * - baseUrl használata (dinamikusan generált)
     * - OkHttpClient használata
     * - Gson converter használata JSON kezeléshez
     */
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(baseUrl)
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
    
    /**
     * ScheduleApiService instance létrehozása
     * 
     * Schedule-okkal kapcsolatos API műveletek
     * 
     * @return ScheduleApiService implementáció
     */
    val scheduleApiService: ScheduleApiService by lazy {
        retrofit.create(ScheduleApiService::class.java)
    }
    
    /**
     * HabitApiService instance létrehozása
     * 
     * Habit-ekhez kapcsolatos API műveletek
     * 
     * @return HabitApiService implementáció
     */
    val habitApiService: HabitApiService by lazy {
        retrofit.create(HabitApiService::class.java)
    }
    
    /**
     * ProgressApiService instance létrehozása
     * 
     * Progress (haladás) kezeléshez kapcsolatos API műveletek
     * 
     * @return ProgressApiService implementáció
     */
    val progressApi: ProgressApiService by lazy {
        retrofit.create(ProgressApiService::class.java)
    }
    
    // TODO: Később hozzáadandó API szolgáltatások
    // val profileApiService: ProfileApiService by lazy { ... }
}
