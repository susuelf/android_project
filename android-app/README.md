# Progr3ss Android App

Ez a **Progr3ss** Android alkalmazás frontend része, amely egy szokáskövető (habit tracker) alkalmazás.

## 📱 Projekt Struktúra

```
android-app/
├── app/
│   └── src/
│       └── main/
│           ├── java/com/progr3ss/androidapp/
│           │   ├── ui/                      # UI komponensek
│           │   │   ├── splash/             # Splash Screen
│           │   │   ├── auth/               # Login, Register
│           │   │   └── home/               # Home Screen (TODO)
│           │   ├── data/                   # Data modellek (TODO)
│           │   ├── network/                # API kommunikáció (TODO)
│           │   └── util/                   # Utility osztályok
│           └── res/                        # Erőforrások (layout, drawable, values)
├── build.gradle                            # Projekt szintű build konfig
└── app/build.gradle                        # App szintű build konfig
```

## 🚀 Technológiák

- **Nyelv**: Kotlin
- **Min SDK**: 24 (Android 7.0+)
- **Target SDK**: 34 (Android 14)
- **Architektúra**: MVVM (tervezés alatt)

### Használt Library-k:

- **AndroidX Core & AppCompat**: Alapvető Android komponensek
- **Material Design 3**: Modern UI komponensek
- **ViewBinding & DataBinding**: Layout binding
- **Navigation Component**: Képernyők közötti navigáció
- **Lifecycle Components**: ViewModel, LiveData
- **Coroutines**: Aszinkron műveletekhez
- **Retrofit**: REST API kommunikáció
- **Room**: Lokális adatbázis
- **DataStore**: Token és beállítások tárolása
- **Glide**: Képbetöltés

## 📋 Jelenlegi Állapot

### ✅ Elkészült:
- Projekt alap struktúra
- Splash Screen
- Login Screen (UI + validáció)
- Register Screen (UI + validáció)
- Alap színpaletta és témák
- String erőforrások (magyar nyelven)

### 🔨 Folyamatban / TODO:
- Backend API integráció
- Token management (DataStore)
- Home Screen
- Schedule Management
- Profile Screen
- Habit Management
- Progress Tracking

## 🏗️ Build és Futtatás

### Előfeltételek:
- Android Studio Hedgehog vagy újabb
- JDK 17
- Android SDK 34

### Lépések:
1. Projekt megnyitása Android Studio-ban
2. Gradle szinkronizálás
3. Emulátor vagy fizikai eszköz csatlakoztatása
4. Run 'app'

## 📝 Fejlesztési Terv

A fejlesztés alpontonként halad, minden funkcióhoz külön branch készül:

1. **Alapvető UI és navigáció** ✅ (jelenlegi)
2. **Backend integráció** (következő)
3. **Home Screen és Schedule Management**
4. **Habit Management**
5. **Profile Management**
6. **Progress Tracking**
7. **Notification System**
8. **UI/UX finomítások**

## 🔗 Backend API

A backend dokumentáció és API endpoints a projekt gyökerében található `PROJECT_SPECIFICATION.md` fájlban találhatók.

### Teszt Credentials (fejlesztéshez):
- Email: `test@test.com`
- Jelszó: `test123`

## 📄 Licenc

Ez egy egyetemi projekt.


