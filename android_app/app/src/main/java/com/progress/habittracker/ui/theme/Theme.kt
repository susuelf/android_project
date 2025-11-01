package com.progress.habittracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Theme.kt - Téma konfiguráció
 * 
 * Progr3SS Dark Theme - a design képek alapján
 */

// Progr3SS Dark Color Scheme - design képek szerinti színek
private val Progr3SSDarkColorScheme = darkColorScheme(
    // Primary colors
    primary = PrimaryPurple,              // Fő lila - gombok, akciók
    onPrimary = TextPrimary,              // Fehér szöveg primary-n
    primaryContainer = DarkSurface,        // Container lila háttérrel
    onPrimaryContainer = TextPrimary,      // Szöveg primary container-en
    
    // Secondary colors
    secondary = SuccessCyan,               // Cyan - completed items
    onSecondary = DarkBackground,          // Sötét szöveg secondary-n
    secondaryContainer = DarkSurface,      
    onSecondaryContainer = TextSecondary,
    
    // Tertiary colors
    tertiary = SuccessGreen,               // Zöld - success states
    onTertiary = DarkBackground,
    tertiaryContainer = DarkSurface,
    onTertiaryContainer = TextSecondary,
    
    // Background colors
    background = DarkBackground,           // Fő háttér - dark navy
    onBackground = TextPrimary,            // Fehér szöveg háttéren
    
    // Surface colors
    surface = DarkSurface,                 // Kártyák, elevated surface
    onSurface = TextPrimary,               // Fehér szöveg surface-ön
    surfaceVariant = DarkSurfaceVariant,   // Világosabb surface
    onSurfaceVariant = TextSecondary,      // Szürke szöveg surface variant-on
    
    // Error colors
    error = ErrorRed,                      // Piros - hibák
    onError = TextPrimary,
    errorContainer = DarkErrorContainer,
    onErrorContainer = ErrorRed,
    
    // Outline colors
    outline = TextTertiary,           // Borderек, dividerek
    outlineVariant = DarkSurfaceVariant,
)

/**
 * Progr3SSTheme - Az alkalmazás fő témája
 * 
 * Dark theme design a képek alapján.
 * Mindig dark theme-et használunk (design követelmény).
 */
@Composable
fun Progr3SSTheme(
    darkTheme: Boolean = true,  // Mindig dark theme
    dynamicColor: Boolean = false,  // Kikapcsoljuk a dinamikus színeket
    content: @Composable () -> Unit
) {
    // Mindig a Progr3SS Dark Color Scheme-et használjuk
    val colorScheme = Progr3SSDarkColorScheme
    
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            // Status bar színe dark background
            window.statusBarColor = DarkBackground.toArgb()
            // Világos ikonok a dark status bar-on
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    // Material 3 téma alkalmazása
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
