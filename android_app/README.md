# Progr3SS - Android Frontend

Ez az Android alkalmazás a Progr3SS Habit Tracker projekt frontend része.

## Technológiák

- **Kotlin** - Programozási nyelv
- **Jetpack Compose** - Modern UI framework
- **Material Design 3** - Design rendszer
- **Hilt** - Dependency Injection
- **Retrofit** - REST API kommunikáció
- **Coroutines & Flow** - Aszinkron programozás
- **Navigation Compose** - Képernyők közötti navigáció
- **DataStore** - Token tárolás
- **Coil** - Képbetöltés

## Projekt struktúra

```
app/src/main/java/com/progress/habittracker/
├── ui/
│   ├── theme/          # Téma (színek, tipográfia)
│   ├── screens/        # Képernyők (Login, Home, Profile, stb.)
│   ├── components/     # Újrafelhasználható UI komponensek
│   └── navigation/     # Navigation graph
├── data/
│   ├── remote/         # API szolgáltatások
│   ├── local/          # Helyi adattárolás (DataStore)
│   ├── repository/     # Repository implementációk
│   └── model/          # Data modellek (DTO-k)
├── domain/
│   ├── model/          # Domain modellek
│   ├── repository/     # Repository interfészek
│   └── usecase/        # Use case-ek (üzleti logika)
├── di/                 # Dependency Injection modulok
├── util/               # Segéd osztályok és extension-ök
└── MainActivity.kt     # Fő Activity

```

## Fordítás és futtatás

### Előfeltételek

- Android Studio Ladybug | 2024.2.1 vagy újabb
- JDK 11 vagy újabb
- Android SDK 24+ (API level 24)

### Backend elindítása

Az Android alkalmazás a NestJS backend-del kommunikál. Először indítsd el a backend-et:

```bash
# A projekt root könyvtárából
npm install
npm run start:dev
```

A backend alapértelmezetten a `http://localhost:3000` címen fog futni.

### Android app futtatása

1. Nyisd meg az `android_app` mappát Android Studio-ban
2. Várj amíg a Gradle sync befejeződik
3. Csatlakoztass egy Android eszközt vagy indíts el egy emulátort
4. Kattints a Run gombra (vagy nyomj Shift+F10)

### API Base URL konfiguráció

Az API base URL a `app/build.gradle.kts` fájlban van definiálva:

- **Debug build**: `http://10.0.2.2:3000` (Android emulátor localhost)
- **Release build**: `https://api.example.com` (Éles szerver URL)

> Megjegyzés: Ha fizikai eszközön tesztelsz, módosítsd a debug build URL-t a számítógéped lokális IP címére.

## Fejlesztési folyamat

A projekt feature branch alapú fejlesztést követ:

1. Minden új feature külön branch-en készül
2. A branch neve: `feature/feature-name`
3. Fejlesztés után merge a main branch-be

## Hozzájárulás

A kód kommentezése magyar nyelven történik a jobb érthetőség érdekében.
