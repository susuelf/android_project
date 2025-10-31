package com.progress.habittracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Theme.kt - Téma konfiguráció
 * 
 * Ez a fájl tartalmazza az alkalmazás témáját (világos/sötét mód)
 * és a színsémákat.
 */

// Sötét téma színséma
private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

// Világos téma színséma
private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40

    /* További színek felülírhatók szükség szerint:
    background = Color(0xFFFFFBFE),
    surface = Color(0xFFFFFBFE),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color(0xFF1C1B1F),
    onSurface = Color(0xFF1C1B1F),
    */
)

/**
 * Progr3SSTheme - Az alkalmazás fő témája
 * 
 * Ez a Composable függvény alkalmazza a témát az egész alkalmazásra.
 * Támogatja a világos/sötét módot és az Android 12+ dinamikus színeket.
 * 
 * @param darkTheme Boolean - Sötét téma használata (alapértelmezett: rendszerbeállítás szerint)
 * @param dynamicColor Boolean - Dinamikus színek használata Android 12+ esetén
 * @param content Composable lambda - A téma alatt megjelenítendő tartalom
 */
@Composable
fun Progr3SSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dinamikus színek elérhetők Android 12+ verzióktól
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Színséma kiválasztása a beállítások alapján
    val colorScheme = when {
        // Android 12+ és dinamikus színek engedélyezve
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Sötét téma
        darkTheme -> DarkColorScheme
        // Világos téma
        else -> LightColorScheme
    }
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        // SideEffect használata az állapotváltozáskor történő egyszer futó kód futtatására
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Material 3 téma alkalmazása
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
