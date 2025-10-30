package com.progr3ss.androidapp.ui.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.progr3ss.androidapp.R
import com.progr3ss.androidapp.ui.auth.LoginActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Splash Screen Activity
 * 
 * Az alkalmazás indításakor megjelenő képernyő.
 * Feladatai:
 * - Indítási animáció megjelenítése
 * - Automatikus bejelentkezés ellenőrzése (token validálás)
 * - Navigáció a megfelelő képernyőre (Home vagy Login)
 * 
 * Jelenleg: Egyszerű késleltetés után átnavigál a Login képernyőre
 * TODO: Token validálás implementálása
 */
@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Splash screen megjelenítése 2 másodpercig
        lifecycleScope.launch {
            delay(2000)
            checkAuthenticationAndNavigate()
        }
    }

    /**
     * Ellenőrzi a felhasználó bejelentkezési státuszát és navigál a megfelelő képernyőre
     * 
     * TODO: Implementálni kell:
     * - Token lekérdezés a lokális tárolóból (DataStore)
     * - Token validálás a backenden (POST /auth/local/refresh)
     * - Sikeres validálás esetén: navigálás a Home képernyőre
     * - Sikertelen esetén: navigálás a Login képernyőre
     */
    private fun checkAuthenticationAndNavigate() {
        // TODO: Token validálás implementálása
        // Egyelőre mindig a Login képernyőre navigálunk
        navigateToLogin()
    }

    /**
     * Navigálás a Login képernyőre
     */
    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish() // Bezárjuk a Splash Activity-t, hogy ne lehessen visszalépni rá
    }

    /**
     * Navigálás a Home képernyőre (sikeres bejelentkezés után)
     * TODO: Implementálni, amikor a Home Activity elkészül
     */
    @Suppress("unused")
    private fun navigateToHome() {
        // TODO: Home Activity navigáció implementálása
        // val intent = Intent(this, HomeActivity::class.java)
        // startActivity(intent)
        // finish()
    }
}
