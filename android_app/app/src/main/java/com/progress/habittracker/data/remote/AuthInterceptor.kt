package com.progress.habittracker.data.remote

import android.content.Context
import android.content.Intent
import com.progress.habittracker.MainActivity
import com.progress.habittracker.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

/**
 * AuthInterceptor - HTTP válaszok figyelése és automatikus kijelentkeztetés
 * 
 * Ez az interceptor figyeli a bejövő HTTP válaszokat.
 * Ha 401 (Unauthorized) vagy 403 (Forbidden) hibakódot észlel,
 * akkor elindítja a kijelentkeztetési folyamatot.
 * 
 * @param context Application context a navigációhoz és adattörléshez
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        if (response.code == 401 || response.code == 403) {
            // Token és felhasználói adatok törlése
            val tokenManager = TokenManager(context)
            
            // Mivel az interceptor szinkron fut, de a DataStore aszinkron,
            // runBlocking-ot használunk a törlés megvárására.
            // Ez biztonságos, mert a hálózati hívások már háttérszálon futnak.
            runBlocking {
                tokenManager.clearAll()
            }

            // Navigáció a Login képernyőre (MainActivity újraindítása)
            // A FLAG_ACTIVITY_NEW_TASK és FLAG_ACTIVITY_CLEAR_TASK
            // biztosítja, hogy a back stack törlődjön.
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        return response
    }
}
