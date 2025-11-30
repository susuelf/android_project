# Részletes Android Alkalmazás Dokumentáció - Progress Habit Tracker

Ez a dokumentum a **Progress Habit Tracker** Android kliensének mélyreható technikai leírása. Célja, hogy fejlesztői szintű betekintést nyújtson az alkalmazás belső működésébe, az architektúrális döntésekbe, a kódstruktúrába és az egyes komponensek közötti interakciókba.

---

## 1. Architektúra és Tervezési Minták

Az alkalmazás a Google által ajánlott **MVVM (Model-View-ViewModel)** architektúrát követi, kiegészítve a **Repository Pattern**-nel és a **Unidirectional Data Flow (UDF)** elvével.

### 1.1. Rétegek (Layers)

Az alkalmazás három fő rétegre oszlik:

1.  **UI Layer (Presentation)**:
    *   **Felelősség**: Adatok megjelenítése a képernyőn és felhasználói interakciók kezelése.
    *   **Komponensek**: Jetpack Compose `Screen`-ek (View) és `ViewModel`-ek.
    *   **Állapotkezelés**: A UI állapotot (State) a ViewModel tárolja `StateFlow` formájában. A View csak "megfigyeli" ezt az állapotot.

2.  **Domain / Data Layer (Repository)**:
    *   **Felelősség**: Az üzleti logika központosítása és az adatok közvetítése a UI és az adatforrások között.
    *   **Komponensek**: `Repository` osztályok (pl. `AuthRepository`, `ScheduleRepository`).
    *   **Működés**: Döntést hoz arról, hogy az adatot a helyi tárolóból vagy a hálózatról kell-e lekérni.

3.  **Data Source Layer (Network & Local)**:
    *   **Felelősség**: Nyers adatok elérése.
    *   **Komponensek**:
        *   **Remote**: Retrofit API interfészek (`ApiService`), `RetrofitClient`.
        *   **Local**: `TokenManager` (DataStore).

### 1.2. Adatfolyam (Unidirectional Data Flow)

Az adatok mindig egy irányba áramlanak:
1.  **Esemény (Event)**: A felhasználó interakcióba lép a UI-val (pl. gombnyomás).
2.  **Action**: A UI meghív egy függvényt a ViewModel-ben.
3.  **Operation**: A ViewModel utasítja a Repository-t egy műveletre (pl. `login()`).
4.  **Data Update**: A Repository frissíti az adatokat (API hívás), és visszaadja az eredményt.
5.  **State Change**: A ViewModel frissíti a `UiState`-et az új adattal.
6.  **Render**: A UI érzékeli az állapotváltozást és újrarajzolja magát.

---

## 2. Részletes Kódmagyarázat és Komponensek

### 2.1. Adatréteg (Data Layer)

#### **Hálózati Kommunikáció (`data/remote`)**

*   **`RetrofitClient.kt`**:
    *   Ez az osztály a hálózati réteg belépési pontja. Singletonként működik (`object`).
    *   **Dinamikus URL**: A `NetworkUtils.getBackendBaseUrl(context)` segítségével futásidőben dönti el, hova csatlakozzon. Ez kritikus a fejlesztéshez, mivel az emulátor (`10.0.2.2`), a fizikai eszköz (`192.168.x.x`) és a production környezet más-más címet igényel.
    *   **OkHttpClient konfiguráció**:
        *   `connectTimeout`, `readTimeout`: 30 másodperc, hogy lassú hálózat esetén se fagyjon le azonnal.
        *   `HttpLoggingInterceptor`: Fejlesztés közben logolja a kérések testét (BODY level).
        *   `AuthInterceptor`: Hozzáadva a klienshez.

*   **`AuthInterceptor.kt`**:
    *   Egy `okhttp3.Interceptor` implementáció.
    *   **Feladata**: Minden bejövő HTTP választ megvizsgál.
    *   **Logika**: Ha a válasz kódja `401 Unauthorized` vagy `403 Forbidden`, az azt jelenti, hogy a token lejárt vagy érvénytelen.
    *   **Reakció**:
        1.  Törli a tárolt tokeneket a `TokenManager`-ből (`runBlocking` blokkban, mivel az interceptor szinkron, a DataStore pedig aszinkron).
        2.  Elindítja a `MainActivity`-t `FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK` flagekkel, ami visszadobja a felhasználót a Login képernyőre és törli a back stack-et.

*   **API Interfészek**:
    *   `AuthApiService`: Login, Register, Refresh Token, Google Login.
    *   `HabitApiService`: Szokások (Habits) CRUD műveletei.
    *   `ScheduleApiService`: Napi beosztások lekérése, státusz módosítása.
    *   `ProfileApiService`: Profil adatok és képfeltöltés (`Multipart`).

#### **Helyi Adattárolás (`data/local`)**

*   **`TokenManager.kt`**:
    *   A `SharedPreferences` modern utódját, a **Jetpack DataStore (Preferences)**-t használja.
    *   **Miért DataStore?**: Aszinkron (Kotlin Flow alapú), biztonságosabb szálkezelés, és nem blokkolja a UI szálat.
    *   **Tárolt adatok**:
        *   `access_token`: A backendhez szükséges JWT token.
        *   `refresh_token`: A token megújításához.
        *   `user_id`, `user_name`, `user_email`: Alapvető profil adatok cache-elése.
    *   **Működés**: A `saveTokens` függvény ír, az `accessToken` property pedig egy `Flow<String?>`, ami azonnal értesíti a feliratkozókat (pl. `AuthRepository`), ha változik a token.

#### **Repository Réteg (`data/repository`)**

Ez a réteg "csomagolja be" az API hívásokat `Resource` objektumokba.

*   **`Resource.kt` (`util` csomag)**:
    *   Egy `sealed class`, amely három állapotot kezel:
        *   `Success(data)`: Sikeres művelet, tartalmazza a választ.
        *   `Error(message)`: Hiba történt, tartalmazza a hibaüzenetet.
        *   `Loading`: Folyamatban lévő művelet.

*   **`AuthRepository.kt`**:
    *   Kezeli a bejelentkezést (`signIn`). Siker esetén azonnal elmenti a tokeneket a `TokenManager`-be.
    *   Kezeli a kijelentkezést (`logout`): Törli a helyi adatokat.
    *   `isLoggedIn()`: Egy `Flow<Boolean>`-t ad vissza, ami a `TokenManager`-t figyeli. Ez vezérli a Splash Screen-t.

*   **`ScheduleRepository.kt`**:
    *   Különlegessége a `_refreshTrigger` (`MutableSharedFlow`). Mivel nincs helyi adatbázis (Room), ami automatikusan frissülne, manuálisan kell jelezni, ha az adatok elavultak.
    *   A `getSchedulesByDay` függvény kombinálja a `_refreshTrigger`-t az API hívással. Ha meghívjuk a `refresh()`-t, a Flow újra lefut és frissíti a listát.

---

### 2.2. UI Réteg (View & ViewModel)

#### **Állapotkezelés (State Management)**

Minden képernyőhöz tartozik egy `UiState` data class (pl. `HomeUiState`). Ez tartalmaz minden adatot, ami a képernyő kirajzolásához kell:
*   Listák (pl. `schedules: List<ScheduleResponseDto>`)
*   Betöltés jelző (`isLoading: Boolean`)
*   Hibaüzenet (`error: String?`)
*   Beviteli mezők értékei (pl. `email: String`, `password: String`)

#### **ViewModel Implementáció**

A ViewModel-ek felelősek az állapot tárolásáért és módosításáért.

*   **Példa: `HomeViewModel`**:
    *   `_uiState` (`MutableStateFlow`): Belső, módosítható állapot.
    *   `uiState` (`StateFlow`): Publikus, csak olvasható állapot a UI számára.
    *   **Függvények**: Pl. `toggleScheduleStatus`. Ez nem csak átbillenti a kapcsolót, hanem meghívja a `ScheduleStateCalculator`-t, ellenőrzi a logikát, majd szól a Repository-nak, hogy küldje el a változást a szervernek.

#### **ViewModel Factory (`ViewModelFactory` osztályok)**

Mivel a ViewModel-eknek paraméterekre van szükségük (Repository-k), nem lehet őket egyszerűen példányosítani.
*   Az alkalmazás **Manual Dependency Injection**-t használ (nem Hilt/Dagger).
*   Minden ViewModel-hez tartozik egy Factory (pl. `HomeViewModelFactory`), ami megkapja a függőségeket (Repository-kat) a `NavGraph`-tól, és létrehozza a ViewModel-t.

#### **Képernyők (Screens)**

A `ui/screens` mappában találhatók a Composable függvények.
*   **Struktúra**: Általában egy `Scaffold`-ot használnak (TopBar, FAB, Content).
*   **Interakció**: A ViewModel-ből kapott állapotot (`state by viewModel.uiState.collectAsStateWithLifecycle()`) használják a kirajzoláshoz.
*   **Események**: A gombnyomásokat továbbítják a ViewModel felé (pl. `viewModel.signIn(...)`).

#### **Navigáció (`navigation`)**

*   **`Screen.kt`**:
    *   `Sealed class`-ként definiálja az útvonalakat.
    *   Paraméteres útvonalak kezelése: pl. `ScheduleDetails` objektum `createRoute(id)` segédfüggvénye.
*   **`NavGraph.kt`**:
    *   Ez az alkalmazás "gerince". Itt történik a ViewModel-ek és Repository-k példányosítása (Composition Root).
    *   Itt dől el, melyik URL (`route`) melyik Composable-t tölti be.

---

## 3. Kiemelt Logikai Folyamatok

### 3.1. Bejelentkezési Folyamat (Login Flow)

1.  **User**: Beírja az adatokat és rányom a "Bejelentkezés" gombra.
2.  **LoginScreen**: Meghívja a `authViewModel.signIn(email, password)`-t.
3.  **AuthViewModel**:
    *   Beállítja a `uiState`-et `Loading`-ra.
    *   Meghívja az `authRepository.signIn()`-t.
4.  **AuthRepository**:
    *   Meghívja az `authApiService.signIn()`-t.
    *   **Siker (200)**: Megkapja a tokent. Meghívja a `tokenManager.saveTokens()`-t. Visszaad `Resource.Success`-t.
    *   **Hiba**: Visszaad `Resource.Error`-t.
5.  **AuthViewModel**:
    *   Siker esetén `AuthState.Success`-re vált.
6.  **LoginScreen**:
    *   Figyeli az állapotot. Ha `Success`, meghívja a navigációt: `navController.navigate(Screen.Home.route)`.

### 3.2. Schedule Státusz Számítás (`ScheduleStateCalculator`)

Ez egy kritikus üzleti logika, ami a `util` csomagban található. A probléma: Egy feladat mikor számít "Kész"-nek?
1.  **Idő alapú**: Ha a felhasználó rögzített annyi haladást (Progress), ami eléri a tervezett időtartamot (pl. 30 perc futásból 30 perc kész). Ekkor a státusz automatikusan `Completed`, és a checkbox nem kattintható (nem vonható vissza).
2.  **Manuális**: A felhasználó bepipálja a feladatot, bár még nem telt le az idő. Ekkor a státusz `Completed`, de a checkbox kattintható marad (visszavonható).

A `ScheduleStateCalculator.calculate(schedule)` függvény megkapja a nyers adatot, és visszaad egy `ScheduleUiState`-et (`isChecked`, `isEnabled`, `progressPercentage`), amit a UI közvetlenül fel tud használni.

---

## 4. Fájlok és Mappák Kommunikációja (Összefoglaló Térkép)

```mermaid
graph TD
    User((Felhasználó)) --> UI[UI Layer (Screens)]
    UI --> VM[ViewModel Layer]
    VM --> Repo[Repository Layer]
    
    subgraph Data Layer
        Repo --> Remote[Remote Data Source (Retrofit)]
        Repo --> Local[Local Data Source (TokenManager)]
        Remote --> API[Backend API]
        Local --> DS[DataStore File]
    end
    
    subgraph Utils
        Calc[ScheduleStateCalculator]
        Net[NetworkUtils]
    end
    
    VM -.-> Calc : Használja állapot számításhoz
    Remote -.-> Net : Használja IP címhez
```

### Fájl Kapcsolatok Példája:
*   `MainActivity.kt` -> inicializálja -> `NavGraph.kt`
*   `NavGraph.kt` -> létrehozza -> `HomeViewModelFactory` -> létrehozza -> `HomeViewModel`
*   `HomeViewModel` -> használja -> `ScheduleRepository`
*   `ScheduleRepository` -> használja -> `ScheduleApiService` ÉS `TokenManager`
*   `ScheduleApiService` -> használja -> `RetrofitClient`

---

## 5. Hibakezelés és Biztonság

*   **Token Kezelés**: A tokenek soha nem jelennek meg a UI rétegben nyíltan. A Repository automatikusan illeszti be őket a kérések fejlécébe (`Authorization: Bearer ...`).
*   **Hálózati Hibák**: A Repository `try-catch` blokkokkal védi a hívásokat. Ha nincs internet vagy a szerver nem elérhető, `Resource.Error`-t ad vissza, amit a ViewModel egy felhasználóbarát hibaüzenetté ("Snackbar" vagy hiba szöveg) alakít.
*   **Input Validáció**: A ViewModel-ek (pl. `AuthViewModel`) tartalmazzák a validációs logikát (pl. email formátum, jelszó hossza) még az API hívás előtt.

Ez a dokumentáció átfogó képet ad a rendszer működéséről, segítve a fejlesztést, hibakeresést és a későbbi bővítést.