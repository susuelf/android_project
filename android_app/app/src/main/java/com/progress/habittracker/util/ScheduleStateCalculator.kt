package com.progress.habittracker.util

import com.progress.habittracker.data.model.ScheduleResponseDto
import com.progress.habittracker.data.model.ScheduleStatus

/**
 * Schedule UI State
 * 
 * A schedule megjelenítési állapota a UI számára.
 * 
 * @property progressPercentage A progress bar értéke (0-100)
 * @property isChecked A checkbox állapota (true = bepipálva)
 * @property isEnabled A checkbox interakció engedélyezése (true = kattintható)
 */
data class ScheduleUiState(
    val progressPercentage: Float,
    val isChecked: Boolean,
    val isEnabled: Boolean
)

/**
 * Schedule State Calculator
 * 
 * Központosított logika a schedule állapotának számításához.
 * Ez biztosítja a konzisztenciát a különböző képernyők között.
 */
object ScheduleStateCalculator {
    
    /**
     * Kiszámolja a schedule UI állapotát a nyers adatok alapján.
     * 
     * Szabályok:
     * 1. Kész állapot (Strict): Ha loggedTime >= requiredTime
     *    -> Progress: 100%, Checkbox: True, Interakció: Disabled
     * 
     * 2. Manuális Kész állapot: Ha loggedTime < requiredTime, DE isManuallyChecked
     *    -> Progress: 100%, Checkbox: True, Interakció: Enabled (visszavonható)
     * 
     * 3. Folyamatban: Ha loggedTime < requiredTime ÉS !isManuallyChecked
     *    -> Progress: Számított %, Checkbox: False, Interakció: Enabled
     * 
     * @param schedule A schedule adatobjektum
     * @return ScheduleUiState A kiszámított UI állapot
     */
    fun calculate(schedule: ScheduleResponseDto): ScheduleUiState {
        // Összes eltöltött idő
        // Védelem duplikációk ellen: distinctBy { it.id }
        // FONTOS: Nem szűrünk isCompleted-re, mert a részleges progress (isCompleted=false) is számít az időbe!
        val totalLoggedTime = schedule.progress
            ?.distinctBy { it.id }
            ?.sumOf { it.loggedTime ?: 0 } ?: 0
        
        val requiredTime = schedule.durationMinutes ?: 0
        val isManuallyChecked = schedule.status == ScheduleStatus.Completed

        // 1. Strict Completion: loggedTime >= requiredTime
        // Csak akkor érvényes, ha van elvárt idő (> 0)
        if (requiredTime > 0 && totalLoggedTime >= requiredTime) {
            return ScheduleUiState(
                progressPercentage = 100f,
                isChecked = true,
                isEnabled = false // Letiltva, mert az idő alapján kész
            )
        }

        // Százalék számítás (In Progress vagy Manual Check esetén is ezt használjuk)
        val percentage = if (requiredTime > 0) {
            ((totalLoggedTime.toDouble() / requiredTime.toDouble()) * 100.0).coerceIn(0.0, 100.0).toFloat()
        } else {
            0f
        }

        // 2. Manual Completion: loggedTime < requiredTime BUT isManuallyChecked
        if (isManuallyChecked) {
            return ScheduleUiState(
                progressPercentage = percentage, // FONTOS: Nem 100%, hanem a valós idő!
                isChecked = true,
                isEnabled = true // Engedélyezve, visszavonható
            )
        }

        // 3. In Progress: loggedTime < requiredTime AND !isManuallyChecked
        return ScheduleUiState(
            progressPercentage = percentage,
            isChecked = false,
            isEnabled = true
        )
    }
}
