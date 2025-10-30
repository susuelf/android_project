package com.progress.habittracker

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

/**
 * Alkalmazás osztály a Hilt dependency injection inicializálásához.
 * 
 * A @HiltAndroidApp annotáció triggers Hilt komponensek generálását,
 * amelyek az egész alkalmazásban elérhetőek lesznek.
 */
@HiltAndroidApp
class HabitTrackerApplication : Application() {
    
    override fun onCreate() {
        super.onCreate()
        // Itt lehetne inicializálni egyéb library-ket (pl. Firebase, Crashlytics, stb.)
    }
}
