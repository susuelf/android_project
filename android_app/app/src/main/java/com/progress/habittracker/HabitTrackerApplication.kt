package com.progress.habittracker

import android.app.Application
import com.progress.habittracker.data.remote.RetrofitClient

/**
 * HabitTrackerApplication - Alkalmazás szintű Application osztály
 * 
 * Ez az osztály fut le először az alkalmazás indításakor.
 * Itt inicializálhatunk singleton objektumokat és globális konfigurációkat.
 */
class HabitTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        
        // RetrofitClient inicializálása - Dinamikus IP felismerés
        RetrofitClient.initialize(applicationContext)
        
        // TODO: További inicializálások (ha szükséges)
        // - Crash reporting (Firebase Crashlytics)
        // - Analytics
        // - WorkManager setup
    }
}
