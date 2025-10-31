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
 * TokenManager - Token és felhasználói adatok kezelése
 * 
 * Ez az osztály felelős a token-ek biztonságos tárolásáért és kezeléséért
 * DataStore Preferences használatával.
 * 
 * @param context Alkalmazás kontextus
 */
class TokenManager(private val context: Context) {
    
    companion object {
        // DataStore neve
        private const val DATASTORE_NAME = "auth_prefs"
        
        // DataStore extension property létrehozása
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
            name = DATASTORE_NAME
        )
        
        // Preferences kulcsok
        private val ACCESS_TOKEN_KEY = stringPreferencesKey("access_token")
        private val REFRESH_TOKEN_KEY = stringPreferencesKey("refresh_token")
        private val USER_ID_KEY = intPreferencesKey("user_id")
        private val USER_EMAIL_KEY = stringPreferencesKey("user_email")
        private val USER_NAME_KEY = stringPreferencesKey("user_name")
    }
    
    /**
     * Access token lekérése Flow-ként
     * 
     * @return Flow<String?> access token vagy null
     */
    val accessToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[ACCESS_TOKEN_KEY]
    }
    
    /**
     * Refresh token lekérése Flow-ként
     * 
     * @return Flow<String?> refresh token vagy null
     */
    val refreshToken: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[REFRESH_TOKEN_KEY]
    }
    
    /**
     * Felhasználó ID lekérése Flow-ként
     * 
     * @return Flow<Int?> user ID vagy null
     */
    val userId: Flow<Int?> = context.dataStore.data.map { preferences ->
        preferences[USER_ID_KEY]
    }
    
    /**
     * Felhasználó email lekérése Flow-ként
     * 
     * @return Flow<String?> email vagy null
     */
    val userEmail: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_EMAIL_KEY]
    }
    
    /**
     * Felhasználó név lekérése Flow-ként
     * 
     * @return Flow<String?> username vagy null
     */
    val userName: Flow<String?> = context.dataStore.data.map { preferences ->
        preferences[USER_NAME_KEY]
    }
    
    /**
     * Token-ek és felhasználói adatok mentése
     * 
     * Sikeres login/register után hívjuk meg
     * 
     * @param accessToken Access token
     * @param refreshToken Refresh token
     * @param userId Felhasználó ID
     * @param userEmail Felhasználó email
     * @param userName Felhasználó név
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
     * Token refresh után hívjuk meg
     * 
     * @param accessToken Új access token
     * @param refreshToken Új refresh token
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
     * Logout esetén hívjuk meg
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
