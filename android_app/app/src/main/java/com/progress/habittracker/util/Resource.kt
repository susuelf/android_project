package com.progress.habittracker.util

/**
 * Resource - Generikus wrapper osztály API válaszokhoz
 * 
 * Ez az osztály három állapotot képvisel:
 * - Success: Sikeres API hívás eredménnyel
 * - Error: Hiba történt (hibaüzenettel)
 * - Loading: Folyamatban van az API hívás
 * 
 * @param T Az adatok típusa
 */
sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null
) {
    /**
     * Sikeres állapot
     * 
     * @param data A válasz adatai
     */
    class Success<T>(data: T) : Resource<T>(data)
    
    /**
     * Hiba állapot
     * 
     * @param message Hibaüzenet
     * @param data Opcionális adat (pl. cache-ből)
     */
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    
    /**
     * Töltés állapot
     * 
     * @param data Opcionális adat (pl. cache-ből töltés közben)
     */
    class Loading<T>(data: T? = null) : Resource<T>(data)
}
