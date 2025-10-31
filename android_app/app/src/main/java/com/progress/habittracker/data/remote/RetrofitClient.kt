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
 * ⚠️ FONTOS: Az initialize() metódust kell hívni az Application onCreate()-ben!
 */
object RetrofitClient {
    
    /**
     * Backend API alap URL
     * 
     * Ez a változó dinamikusan kerül beállításra az initialize() metódusban.
     * Automatikusan detektálja a WiFi gateway IP címét, így nem kell kézzel frissíteni.
     */
    private var baseUrl: String = "http://10.0.2.2:8080/" // Fallback URL
    
    /**
     * Inicializálás - FONTOS: Hívd meg az Application onCreate()-ben!
     * 
     * @param context Application Context
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
     * - BASE_URL beállítása (dinamikusan az initialize()-ből)
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
    
    // TODO: Később hozzáadandó API szolgáltatások
    // val habitApiService: HabitApiService by lazy { ... }
    // val progressApiService: ProgressApiService by lazy { ... }
    // val profileApiService: ProfileApiService by lazy { ... }
}
