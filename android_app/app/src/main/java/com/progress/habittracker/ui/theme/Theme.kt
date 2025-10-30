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
 * Sötét téma színséma.
 * 
 * Definiálja a Material Design 3 színeket sötét módban.
 */
private val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    secondary = DarkSecondary,
    background = DarkBackground,
    surface = DarkSurface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = DarkOnBackground,
    onSurface = DarkOnSurface,
    error = Error,
    onError = OnError
)

/**
 * Világos téma színséma.
 * 
 * Definiálja a Material Design 3 színeket világos módban.
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    secondary = Secondary,
    background = Background,
    surface = Surface,
    onPrimary = OnPrimary,
    onSecondary = OnSecondary,
    onBackground = OnBackground,
    onSurface = OnSurface,
    error = Error,
    onError = OnError
)

/**
 * A fő téma komponens az alkalmazáshoz.
 * 
 * Ez a Composable függvény alkalmazza a Material Design 3 témát
 * az egész alkalmazásra, beleértve a színeket, tipográfiát.
 * 
 * @param darkTheme Boolean - ha true, sötét témát használ, egyébként világos témát.
 *                  Alapértelmezett: rendszer beállítás szerint.
 * @param dynamicColor Boolean - ha true és Android 12+ eszközön fut, dinamikus színeket használ.
 * @param content A téma alatt megjelenítendő tartalom.
 */
@Composable
fun Progr3SSTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dinamikus szín Android 12+ eszközökön elérhető
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Színséma kiválasztása
    val colorScheme = when {
        // Dinamikus színek használata Android 12+ esetén
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Sötét téma
        darkTheme -> DarkColorScheme
        // Világos téma
        else -> LightColorScheme
    }
    
    // Statusbar színének beállítása
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    // Material Theme alkalmazása a tartalomra
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
