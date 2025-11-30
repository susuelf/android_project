package com.progress.habittracker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * TokenManager - Helyi adattárolás kezelése (DataStore)
 * 
 * Ez az osztály felelős a hitelesítési tokenek (Access, Refresh) és az alapvető
 * felhasználói adatok (ID, név, email) biztonságos és perzisztens tárolásáért.
 * 
 * Technológiák:
 * - Jetpack DataStore Preferences: A SharedPreferences modern, aszinkron utódja.
 * - Kotlin Coroutines & Flow: Az adatok aszinkron írása és olvasása.
 * 
 * @param context Az alkalmazás kontextusa, szükséges a DataStore eléréséhez.
 */
class TokenManager(private val context: Context) {
    
    companion object {
        // A DataStore fájl neve
        private const val DATASTORE_NAME = "auth_prefs"
        
        // DataStore példány létrehozása (Singleton minta a Context kiterjesztésével)
        // Ez biztosítja, hogy csak egy DataStore példány létezzen az alkalmazásban.
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
        
        // Kulcsok definíciója az adatok tárolásához
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token") // JWT Access Token
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token") // JWT Refresh Token
        private val USER_ID_KEY = intPreferencesKey("user_id") // Felhasználó egyedi azonosítója
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email") // Felhasználó email címe
        private val USER_NAME_KEY = stringPreferencesKey("user_name") // Felhasználónév
    }
    
    /**
     * Access token lekérése Flow-ként
     * 
     * Folyamatosan figyeli a DataStore változásait. Ha a token frissül,
     * a Flow új értéket bocsát ki.
     * 
     * @return Flow<String?> A token értéke, vagy null ha nincs mentve.
     */
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }
    
    /**
     * Refresh token lekérése Flow-ként
     * 
     * @return Flow<String?> A refresh token értéke, vagy null.
     */
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }
    
    /**
     * Felhasználó ID lekérése Flow-ként
     * 
     * @return Flow<Int?> A felhasználó ID-ja, vagy null.
     */
    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    /**
     * Felhasználó email lekérése Flow-ként
     * 
     * @return Flow<String?> Az email cím, vagy null.
     */
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    /**
     * Felhasználó név lekérése Flow-ként
     * 
     * @return Flow<String?> A felhasználónév, vagy null.
     */
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    /**
     * Token-ek és felhasználói adatok mentése
     * 
     * Ezt a függvényt hívjuk meg sikeres bejelentkezés vagy regisztráció után.
     * Aszinkron művelet (suspend).
     * 
     * @param accessToken A kapott JWT access token.
     * @param refreshToken A kapott JWT refresh token.
     * @param userId A felhasználó azonosítója.
     * @param userEmail A felhasználó email címe.
     * @param userName A felhasználó neve.
     */
    suspend fun saveTokens(
        accessToken: String,
        refreshToken: String,
        userId: Int,
        userEmail: String,
        userName: String
    ) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
            preferences[USER_ID_KEY] = userId
            preferences[USER_EMAIL_KEY] = userEmail
            preferences[USER_NAME_KEY] = userName
        }
    }
    
    /**
     * Access token frissítése
     * 
     * Akkor hívjuk meg, ha a token lejárt, és a refresh token segítségével
     * újat kértünk a szervertől.
     * 
     * @param accessToken Az új access token.
     * @param refreshToken Az új refresh token.
     */
    suspend fun updateTokens(accessToken: String, refreshToken: String) {
        context.dataStore.edit { preferences ->
            preferences[ACCESS_TOKEN_KEY] = accessToken
            preferences[REFRESH_TOKEN_KEY] = refreshToken
        }
    }
    
    /**
     * Összes tárolt adat törlése
     * 
     * Kijelentkezéskor (Logout) hívjuk meg, hogy eltávolítsuk a felhasználó
     * hitelesítési adatait az eszközről.
     */
    suspend fun clearAll() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
    
    /**
     * Ellenőrzi, hogy van-e érvényes access token
     * 
     * @return Boolean true ha van token, false egyébként
     */
    suspend fun hasAccessToken(): Boolean {
        var hasToken = false
        context.dataStore.data.map { preferences ->
            hasToken = !preferences[ACCESS_TOKEN_KEY].isNullOrEmpty()
        }
        return hasToken
    }
}
