package com.progress.habittracker.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Color.kt - Az alkalmazás színpalettája
 * 
 * Ez a fájl tartalmazza az alkalmazás összes színdefinícióját.
 * A design képek alapján dark theme színséma.
 */

// ==================== PROGR3SS DESIGN COLORS ====================

// Background colors
val DarkBackground = Color(0xFF1E1E2E)        // Fő háttér - sötét navy
val DarkSurface = Color(0xFF2A2A3E)           // Kártyák, surface elemek
val DarkSurfaceVariant = Color(0xFF353548)    // Surface variant - világosabb

// Primary/Accent colors
val PrimaryPurple = Color(0xFF6C63FF)         // Fő lila szín - gombok, akciók
val PrimaryPurpleDark = Color(0xFF5A52D5)     // Sötétebb lila - pressed state
val PrimaryPurpleLight = Color(0xFF8B84FF)    // Világosabb lila - hover

// Success/Progress colors
val SuccessCyan = Color(0xFF00D9FF)           // Cyan - completed items, progress
val SuccessGreen = Color(0xFF00C9A7)          // Zöld - success states

// Text colors
val TextPrimary = Color(0xFFFFFFFF)           // Fehér - fő szövegek
val TextSecondary = Color(0xFFB0B3C1)         // Világos szürke - másodlagos szövegek
val TextTertiary = Color(0xFF7C7F93)          // Sötétebb szürke - hint szövegek

// Error/Warning colors
val ErrorRed = Color(0xFFFF6B6B)              // Piros - hibák, törlés
val WarningOrange = Color(0xFFFFAA4D)         // Narancs - figyelmeztetések
val DarkErrorContainer = Color(0xFF3D2626)

// Habit category icons colors (opcionális)
val HabitBlue = Color(0xFF4D9EFF)             // Kék - víz, futás
val HabitGreen = Color(0xFF5FD068)            // Zöld - egészség
val HabitPink = Color(0xFFFF6BA8)             // Pink - szociális
val HabitYellow = Color(0xFFFFD93D)           // Sárga - tanulás

// Legacy Material colors (compatibility)
val Purple80 = PrimaryPurpleLight
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = PrimaryPurpleDark
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)
