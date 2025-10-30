# Progr3SS - Android Frontend UI

Ez az Android alkalmazás a Progr3SS Habit Planner & Tracker projekt frontend része.

## Technológiai Stack

- **Nyelv**: Kotlin
- **UI Framework**: Jetpack Compose
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Build Tool**: Gradle (Kotlin DSL)

## Projekt Struktúra

```
android_app/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/progress/habittracker/
│   │   │   │   ├── MainActivity.kt          # Fő Activity
│   │   │   │   └── ui/
│   │   │   │       └── theme/               # Téma fájlok
│   │   │   │           ├── Color.kt         # Színpalétta
│   │   │   │           ├── Theme.kt         # Téma konfiguráció
│   │   │   │           └── Type.kt          # Tipográfia
│   │   │   ├── res/                         # Erőforrások (layouts, strings, stb.)
│   │   │   └── AndroidManifest.xml          # Manifest fájl
│   │   ├── androidTest/                     # Eszköz tesztek
│   │   └── test/                            # Unit tesztek
│   └── build.gradle.kts                     # App szintű build konfiguráció
├── gradle/
│   └── libs.versions.toml                   # Verzió katalógus
├── build.gradle.kts                         # Projekt szintű build konfiguráció
└── settings.gradle.kts                      # Gradle beállítások
```

## Fejlesztési Terv

A fejlesztés a PROJECT_SPECIFICATION.md szerint történik, alpontonként külön branchekben:

1. **Alapstruktúra** (jelenlegi branch) ✅
   - Projekt inicializálás
   - Alapvető package struktúra
   - Téma beállítások

2. **Következő lépések**:
   - Authentikáció képernyők (Login, Register, Reset Password)
   - Home Screen és navigáció
   - Habit kezelés
   - Schedule kezelés
   - Profil kezelés
   - Progress követés

## Build és Futtatás

### Követelmények
- Android Studio (legújabb verzió)
- JDK 11 vagy újabb
- Android SDK 36

### Build parancsok

```bash
# Debug build készítése
./gradlew assembleDebug

# Release build készítése
./gradlew assembleRelease

# Tesztek futtatása
./gradlew test

# Eszköz tesztek futtatása
./gradlew connectedAndroidTest
```

### Futtatás Android Studio-ban

1. Nyisd meg a projektet Android Studio-ban
2. Várj, amíg a Gradle sync befejeződik
3. Válassz egy emulatort vagy csatlakoztass egy eszközt
4. Kattints a Run gombra (vagy Shift+F10)

## Backend Kapcsolat

Az alkalmazás a projekt gyökerében található NestJS backend-del kommunikál.
Backend URL: `http://localhost:3000` (development)

## Függőségek

Főbb függőségek:
- AndroidX Core KTX
- AndroidX AppCompat
- Material Components
- Jetpack Compose BOM
- Jetpack Compose UI
- Jetpack Compose Material3
- Lifecycle Runtime KTX
- Activity Compose

Részletes verzió információk: `gradle/libs.versions.toml`

## Megjegyzések

- A kód magyar nyelvű kommentekkel van ellátva a könnyebb megértés érdekében
- A projekt branch-based fejlesztést követ
- Minden feature külön branchben készül és merge után kerül a main-be
