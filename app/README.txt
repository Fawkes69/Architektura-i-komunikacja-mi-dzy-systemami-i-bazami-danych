================================================================================
  SYSTEM REZERWACJI MIEJSC W PRZESTRZENIACH CO-WORKINGOWYCH
  Instrukcja krok po kroku — przeczytaj od początku do końca przed działaniem
================================================================================

SPIS TREŚCI
-----------
  1. Wymagania wstępne (co musisz mieć zainstalowane)
  2. Struktura plików — gdzie co umieścić
  3. Konfiguracja backendu (pliki .env)
  4. Uruchomienie backendu (Docker)
  5. Weryfikacja backendu (test API)
  6. Konfiguracja projektu Android
  7. Uruchomienie aplikacji Android
  8. Pierwsze kroki w aplikacji (logowanie, tworzenie danych)
  9. Opis wszystkich endpointów API
 10. Rozwiązywanie problemów



================================================================================
  1. WYMAGANIA WSTĘPNE
================================================================================

Zanim zaczniesz, upewnij się, że masz zainstalowane:

  [BACKEND]
  - Docker Desktop (https://www.docker.com/products/docker-desktop)
    Wersja minimum: 24.x
    Sprawdź: docker --version

  - Docker Compose (wbudowany w Docker Desktop)
    Sprawdź: docker compose version

  [ANDROID]
  - Android Studio (https://developer.android.com/studio)
    Wersja minimum: Hedgehog 2023.1.1 lub nowsza
    Pobierz najnowszą stabilną wersję

  - JDK 17 (zazwyczaj dostarczany z Android Studio)
    Sprawdź: java -version

  - Emulator Android lub fizyczne urządzenie
    API Level minimum: 26 (Android 8.0 Oreo)

  [OPCJONALNIE - do testowania API]
  - curl (wbudowany w macOS/Linux, dla Windows: Git Bash lub WSL)
  - Bruno / Postman / Insomnia — klient REST API



================================================================================
  2. STRUKTURA PLIKÓW — GDZIE CO UMIEŚCIĆ
================================================================================

Utwórz katalog główny projektu i umieść pliki DOKŁADNIE w tej strukturze:

  coworking-system/                        ← KATALOG GŁÓWNY (możesz go nazwać jak chcesz)
  │
  ├── docker-compose.yml                   ← Plik: docker-compose.yml
  ├── README.txt                           ← Ten plik
  │
  ├── auth-service/                        ← SERWIS AUTORYZACJI
  │   ├── Dockerfile                       ← Plik: auth-service/Dockerfile
  │   ├── requirements.txt                 ← Plik: auth-service/requirements.txt
  │   ├── .env.example                     ← Plik: auth-service/.env.example
  │   ├── .env                             ← STWORZYSZ SAM w kroku 3 (nie wgrywaj do git!)
  │   └── app/
  │       ├── __init__.py                  ← Pusty plik
  │       ├── main.py                      ← Plik: auth-service/app/main.py
  │       ├── api/
  │       │   ├── __init__.py
  │       │   └── v1/
  │       │       ├── __init__.py
  │       │       └── endpoints/
  │       │           ├── __init__.py
  │       │           ├── auth.py          ← Plik: auth-service/app/api/v1/endpoints/auth.py
  │       │           └── users.py         ← Plik: auth-service/app/api/v1/endpoints/users.py
  │       ├── core/
  │       │   ├── __init__.py
  │       │   ├── config.py               ← Plik: auth-service/app/core/config.py
  │       │   └── security.py             ← Plik: auth-service/app/core/security.py
  │       ├── db/
  │       │   ├── __init__.py
  │       │   └── session.py              ← Plik: auth-service/app/db/session.py
  │       ├── models/
  │       │   ├── __init__.py
  │       │   └── user.py                 ← Plik: auth-service/app/models/user.py
  │       ├── schemas/
  │       │   ├── __init__.py
  │       │   └── user.py                 ← Plik: auth-service/app/schemas/user.py
  │       └── services/
  │           ├── __init__.py
  │           └── user_service.py         ← Plik: auth-service/app/services/user_service.py
  │
  ├── reservation-service/                 ← SERWIS REZERWACJI
  │   ├── Dockerfile                       ← Plik: reservation-service/Dockerfile
  │   ├── requirements.txt                 ← Plik: reservation-service/requirements.txt
  │   ├── .env.example                     ← Plik: reservation-service/.env.example
  │   ├── .env                             ← STWORZYSZ SAM w kroku 3 (nie wgrywaj do git!)
  │   └── app/
  │       ├── __init__.py
  │       ├── main.py                      ← Plik: reservation-service/app/main.py
  │       ├── api/
  │       │   ├── __init__.py
  │       │   └── v1/
  │       │       ├── __init__.py
  │       │       └── endpoints/
  │       │           ├── __init__.py
  │       │           ├── spaces.py        ← Plik: reservation-service/app/api/v1/endpoints/spaces.py
  │       │           └── reservations.py  ← Plik: reservation-service/app/api/v1/endpoints/reservations.py
  │       ├── core/
  │       │   ├── __init__.py
  │       │   ├── config.py               ← Plik: reservation-service/app/core/config.py
  │       │   └── deps.py                 ← Plik: reservation-service/app/core/deps.py
  │       ├── db/
  │       │   ├── __init__.py
  │       │   └── session.py              ← Plik: reservation-service/app/db/session.py
  │       ├── models/
  │       │   ├── __init__.py
  │       │   └── space.py                ← Plik: reservation-service/app/models/space.py
  │       ├── schemas/
  │       │   ├── __init__.py
  │       │   └── reservation.py          ← Plik: reservation-service/app/schemas/reservation.py
  │       └── services/
  │           ├── __init__.py
  │           ├── space_service.py        ← Plik: reservation-service/app/services/space_service.py
  │           └── reservation_service.py  ← Plik: reservation-service/app/services/reservation_service.py
  │
  └── android-app/                         ← APLIKACJA ANDROID
      ├── build.gradle.kts                 ← Plik: android-app/build.gradle.kts
      ├── gradle/
      │   └── libs.versions.toml           ← Plik: android-app/gradle/libs.versions.toml
      └── app/
          ├── build.gradle.kts             ← Plik: android-app/app/build.gradle.kts
          └── src/
              └── main/
                  ├── AndroidManifest.xml  ← STWORZYSZ SAM w kroku 6
                  ├── res/
                  │   ├── values/
                  │   │   └── themes.xml   ← STWORZYSZ SAM w kroku 6
                  │   └── (inne zasoby)
                  └── java/
                      └── com/
                          └── coworking/
                              ├── api/
                              │   ├── ApiModels.kt         ← Plik
                              │   ├── AuthApiService.kt    ← Plik
                              │   └── ReservationApiService.kt ← Plik
                              ├── data/
                              │   ├── local/
                              │   │   ├── CoworkingDatabase.kt ← Plik
                              │   │   ├── TokenManager.kt      ← Plik
                              │   │   ├── dao/
                              │   │   │   └── Daos.kt          ← Plik
                              │   │   └── entities/
                              │   │       └── Entities.kt      ← Plik
                              │   └── repository/
                              │       ├── AuthRepository.kt    ← Plik
                              │       ├── SpaceRepository.kt   ← Plik
                              │       └── ReservationRepository.kt ← Plik
                              ├── di/
                              │   └── AppModule.kt             ← Plik
                              └── ui/
                                  ├── Navigation.kt            ← Plik
                                  ├── auth/
                                  │   ├── AuthViewModel.kt     ← Plik
                                  │   ├── LoginScreen.kt       ← Plik
                                  │   └── RegisterScreen.kt    ← Plik
                                  ├── home/
                                  │   ├── HomeViewModel.kt     ← Plik
                                  │   └── HomeScreen.kt        ← Plik
                                  ├── booking/
                                  │   ├── BookingViewModel.kt  ← Plik
                                  │   ├── BookingScreen.kt     ← Plik
                                  │   └── MyReservationsScreen.kt ← Plik
                                  ├── admin/
                                  │   ├── AdminViewModel.kt    ← Plik
                                  │   └── AdminScreen.kt       ← Plik
                                  └── profile/
                                      └── ProfileScreen.kt     ← Plik



================================================================================
  3. KONFIGURACJA BACKENDU — PLIKI .ENV
================================================================================

Musisz stworzyć pliki .env na podstawie .env.example dla każdego serwisu.
Pliki .env zawierają sekrety — NIGDY nie wgrywaj ich do repozytorium git!

--- KROK 3a: Auth Service ---

Przejdź do katalogu auth-service i skopiuj plik example:

  cd auth-service
  cp .env.example .env

Otwórz plik auth-service/.env w edytorze i ustaw wartości:

  DATABASE_URL=postgresql://auth_user:auth_password@auth_db:5432/auth_db
  SECRET_KEY=wpisz_tutaj_losowy_ciag_min_64_znakow_np_abcdef1234567890abcdef1234567890abcdef12
  ALGORITHM=HS256
  ACCESS_TOKEN_EXPIRE_MINUTES=30
  REFRESH_TOKEN_EXPIRE_DAYS=7

  WAŻNE: SECRET_KEY musi być unikalny i trudny do zgadnięcia.
  Możesz wygenerować go komendą:
    python3 -c "import secrets; print(secrets.token_hex(32))"

--- KROK 3b: Reservation Service ---

Przejdź do katalogu reservation-service i skopiuj plik example:

  cd ../reservation-service
  cp .env.example .env

Otwórz plik reservation-service/.env i ustaw wartości:

  DATABASE_URL=postgresql://res_user:res_password@reservation_db:5432/reservation_db
  SECRET_KEY=TEN_SAM_SECRET_KEY_CO_W_AUTH_SERVICE
  ALGORITHM=HS256
  AUTH_SERVICE_URL=http://auth_service:8000

  WAŻNE: SECRET_KEY musi być IDENTYCZNY jak w auth-service/.env
  Reservation service weryfikuje tokeny JWT samodzielnie tym samym kluczem.



================================================================================
  4. URUCHOMIENIE BACKENDU (DOCKER)
================================================================================

Wróć do katalogu głównego projektu (gdzie jest docker-compose.yml):

  cd ..
  (lub cd coworking-system, jeśli jesteś wyżej)

--- KROK 4a: Zbuduj i uruchom wszystkie kontenery ---

  docker compose up --build

  Co się dzieje:
  - Docker pobiera obrazy PostgreSQL 15 i Python 3.11
  - Buduje obrazy dla auth-service i reservation-service
  - Uruchamia 4 kontenery: auth_db, reservation_db, auth_service, reservation_service
  - Bazy danych tworzą się automatycznie przy pierwszym uruchomieniu
  - FastAPI tworzy tabele automatycznie przy starcie (SQLAlchemy)

  Poczekaj aż zobaczysz w logach:
    auth_service     | INFO:     Application startup complete.
    reservation_service | INFO:  Application startup complete.

  Czas oczekiwania: 30-60 sekund przy pierwszym uruchomieniu.

--- KROK 4b: Uruchomienie w tle (opcjonalnie) ---

Jeśli nie chcesz blokować terminala:

  docker compose up --build -d

Sprawdzenie logów:
  docker compose logs -f auth_service
  docker compose logs -f reservation_service

Zatrzymanie:
  docker compose down

Zatrzymanie z usunięciem danych (RESET):
  docker compose down -v



================================================================================
  5. WERYFIKACJA BACKENDU — TEST API
================================================================================

Otwórz przeglądarkę i sprawdź dokumentację Swagger:

  Auth Service:        http://localhost:8000/docs
  Reservation Service: http://localhost:8001/docs

Jeśli strony się otwierają — backend działa poprawnie!

--- TEST PRZEZ CURL ---

KROK 5a: Rejestracja użytkownika

  curl -X POST http://localhost:8000/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"email": "test@test.pl", "full_name": "Jan Kowalski", "password": "haslo123"}'

  Oczekiwana odpowiedź (201 Created):
  {"id":1,"email":"test@test.pl","full_name":"Jan Kowalski","is_active":true,"is_admin":false,...}

KROK 5b: Logowanie i pobranie tokenu

  curl -X POST http://localhost:8000/api/v1/auth/login \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=test@test.pl&password=haslo123"

  Oczekiwana odpowiedź:
  {"access_token":"eyJ...","refresh_token":"eyJ...","token_type":"bearer"}

  Skopiuj wartość "access_token" — będziesz jej używał dalej.
  Zastąp TWOJ_TOKEN poniżej rzeczywistym tokenem.

KROK 5c: Sprawdzenie profilu

  curl http://localhost:8000/api/v1/users/me \
    -H "Authorization: Bearer TWOJ_TOKEN"

KROK 5d: Dodanie miejsca (najpierw trzeba mieć admina — patrz niżej)

Aby nadać uprawnienia admina, wejdź bezpośrednio do bazy:

  docker exec -it auth_db psql -U auth_user -d auth_db \
    -c "UPDATE users SET is_admin = true WHERE email = 'test@test.pl';"

  Po tej komendzie zaloguj się ponownie (stary token nie zawiera flagi is_admin w tym projekcie
  — token weryfikowany jest lokalnie; flaga is_admin jest kodowana przy logowaniu do payloadu
  przez serwis auth jeśli dodasz ją do create_access_token).

  UWAGA: W obecnej implementacji flaga is_admin jest pobierana z modelu przy weryfikacji
  tokenu po stronie reservation-service — użyj nowo wygenerowanego tokenu po aktualizacji DB.

KROK 5e: Dodanie miejsca (jako admin)

  curl -X POST http://localhost:8001/api/v1/spaces/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer TWOJ_TOKEN" \
    -d '{
      "name": "Biurko A1",
      "description": "Przy oknie, piętro 1",
      "space_type": "desk",
      "floor": 1,
      "capacity": 1,
      "pos_x": 0.2,
      "pos_y": 0.3
    }'

KROK 5f: Lista miejsc

  curl http://localhost:8001/api/v1/spaces/ \
    -H "Authorization: Bearer TWOJ_TOKEN"

KROK 5g: Rezerwacja miejsca

  curl -X POST http://localhost:8001/api/v1/reservations/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer TWOJ_TOKEN" \
    -d '{
      "space_id": 1,
      "start_time": "2026-05-10T09:00:00Z",
      "end_time": "2026-05-10T17:00:00Z",
      "notes": "Praca projektowa"
    }'

KROK 5h: Moje rezerwacje

  curl http://localhost:8001/api/v1/reservations/ \
    -H "Authorization: Bearer TWOJ_TOKEN"

KROK 5i: Anulowanie rezerwacji (zastąp 1 numerem ID rezerwacji)

  curl -X DELETE http://localhost:8001/api/v1/reservations/1 \
    -H "Authorization: Bearer TWOJ_TOKEN"



================================================================================
  6. KONFIGURACJA PROJEKTU ANDROID
================================================================================

--- KROK 6a: Otwórz projekt w Android Studio ---

1. Uruchom Android Studio
2. Kliknij: File → Open
3. Wybierz folder: coworking-system/android-app
4. Kliknij OK i poczekaj na synchronizację Gradle (może trwać 2-5 minut)

--- KROK 6b: Utwórz brakujące pliki Android ---

Android Studio potrzebuje kilku plików, których nie wygenerował ten projekt.
Musisz je stworzyć ręcznie.

PLIK 1: AndroidManifest.xml
Ścieżka: android-app/app/src/main/AndroidManifest.xml

Utwórz plik z następującą zawartością:

  <?xml version="1.0" encoding="utf-8"?>
  <manifest xmlns:android="http://schemas.android.com/apk/res/android">

      <uses-permission android:name="android.permission.INTERNET" />

      <application
          android:name=".CoworkingApp"
          android:allowBackup="true"
          android:label="CoWorking"
          android:theme="@style/Theme.Coworking"
          android:supportsRtl="true">

          <activity
              android:name=".MainActivity"
              android:exported="true"
              android:theme="@style/Theme.Coworking">
              <intent-filter>
                  <action android:name="android.intent.action.MAIN" />
                  <category android:name="android.intent.category.LAUNCHER" />
              </intent-filter>
          </activity>

      </application>

  </manifest>


PLIK 2: res/values/themes.xml
Ścieżka: android-app/app/src/main/res/values/themes.xml

  <?xml version="1.0" encoding="utf-8"?>
  <resources>
      <style name="Theme.Coworking" parent="android:Theme.Material.Light.NoActionBar" />
  </resources>


PLIK 3: MainActivity.kt
Ścieżka: android-app/app/src/main/java/com/coworking/MainActivity.kt

  package com.coworking

  import android.os.Bundle
  import androidx.activity.ComponentActivity
  import androidx.activity.compose.setContent
  import androidx.activity.enableEdgeToEdge
  import androidx.compose.material3.MaterialTheme
  import com.coworking.ui.CoworkingNavHost
  import dagger.hilt.android.AndroidEntryPoint

  @AndroidEntryPoint
  class MainActivity : ComponentActivity() {
      override fun onCreate(savedInstanceState: Bundle?) {
          super.onCreate(savedInstanceState)
          enableEdgeToEdge()
          setContent {
              MaterialTheme {
                  CoworkingNavHost()
              }
          }
      }
  }


PLIK 4: CoworkingApp.kt
Ścieżka: android-app/app/src/main/java/com/coworking/CoworkingApp.kt

  package com.coworking

  import android.app.Application
  import dagger.hilt.android.HiltAndroidApp

  @HiltAndroidApp
  class CoworkingApp : Application()


PLIK 5: settings.gradle.kts
Ścieżka: android-app/settings.gradle.kts

  pluginManagement {
      repositories {
          google {
              content {
                  includeGroupByRegex("com\\.android.*")
                  includeGroupByRegex("com\\.google.*")
                  includeGroupByRegex("androidx.*")
              }
          }
          mavenCentral()
          gradlePluginPortal()
      }
  }
  dependencyResolutionManagement {
      repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
      repositories {
          google()
          mavenCentral()
      }
  }

  rootProject.name = "CoworkingApp"
  include(":app")


--- KROK 6c: Sprawdź połączenie z backendem ---

W pliku android-app/app/build.gradle.kts upewnij się, że adresy IP są poprawne:

  buildConfigField("String", "AUTH_BASE_URL", "\"http://10.0.2.2:8000\"")
  buildConfigField("String", "RESERVATION_BASE_URL", "\"http://10.0.2.2:8001\"")

  WYJAŚNIENIE ADRESÓW:
  - 10.0.2.2 to specjalny adres, który emulator Android tłumaczy na "localhost" komputera
  - Jeśli używasz fizycznego urządzenia przez USB, zmień 10.0.2.2 na IP swojego komputera
    np. "http://192.168.1.100:8000"
  - IP komputera sprawdzisz komendą: ip addr show (Linux/Mac) lub ipconfig (Windows)



================================================================================
  7. URUCHOMIENIE APLIKACJI ANDROID
================================================================================

--- KROK 7a: Skonfiguruj emulator ---

1. W Android Studio kliknij: Tools → Device Manager
2. Kliknij "Create Device"
3. Wybierz: Phone → Pixel 6 (lub dowolny)
4. Wybierz system: API 34, Android 14 (lub nowszy)
5. Kliknij Finish i uruchom emulator przyciskiem ▶

--- KROK 7b: Uruchom aplikację ---

1. Upewnij się, że backend działa (docker compose up)
2. W Android Studio kliknij zielony przycisk ▶ Run (Shift+F10)
3. Wybierz swój emulator
4. Poczekaj na build i instalację (1-3 minuty przy pierwszym uruchomieniu)

--- KROK 7c: Co zobaczysz ---

Przy pierwszym uruchomieniu pojawi się ekran logowania.
Jeśli backend działa, możesz:
  - Kliknąć "Zarejestruj się" i stworzyć konto
  - Zalogować się
  - Przeglądać miejsca (będzie pusta lista dopóki admin nie doda miejsc)
  - Dokonywać rezerwacji



================================================================================
  8. PIERWSZE KROKI W APLIKACJI
================================================================================

Poniżej kompletny scenariusz od zera:

--- KROK 8a: Uruchom backend ---

  cd coworking-system
  docker compose up -d

--- KROK 8b: Zarejestruj pierwszego użytkownika (admina) ---

  curl -X POST http://localhost:8000/api/v1/auth/register \
    -H "Content-Type: application/json" \
    -d '{"email": "admin@firma.pl", "full_name": "Admin Systemu", "password": "Admin1234"}'

--- KROK 8c: Nadaj uprawnienia admina ---

  docker exec -it auth_db psql -U auth_user -d auth_db \
    -c "UPDATE users SET is_admin = true WHERE email = 'admin@firma.pl';"

--- KROK 8d: Zaloguj się przez curl i skopiuj token ---

  curl -X POST http://localhost:8000/api/v1/auth/login \
    -H "Content-Type: application/x-www-form-urlencoded" \
    -d "username=admin@firma.pl&password=Admin1234"

  Skopiuj access_token z odpowiedzi. Poniżej używamy zmiennej TOKEN.

--- KROK 8e: Dodaj przykładowe miejsca ---

  Biurko 1 (piętro 1):
  curl -X POST http://localhost:8001/api/v1/spaces/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer TOKEN" \
    -d '{"name":"Biurko A1","description":"Przy oknie","space_type":"desk","floor":1,"capacity":1,"pos_x":0.1,"pos_y":0.2}'

  Biurko 2 (piętro 1):
  curl -X POST http://localhost:8001/api/v1/spaces/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer TOKEN" \
    -d '{"name":"Biurko A2","description":"Środek sali","space_type":"desk","floor":1,"capacity":1,"pos_x":0.5,"pos_y":0.5}'

  Sala konferencyjna (piętro 2):
  curl -X POST http://localhost:8001/api/v1/spaces/ \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer TOKEN" \
    -d '{"name":"Sala Alfa","description":"10 osób, projektor","space_type":"meeting_room","floor":2,"capacity":10,"pos_x":0.3,"pos_y":0.4}'

--- KROK 8f: Otwórz aplikację Android i zaloguj się ---

Zaloguj się jako admin@firma.pl z hasłem Admin1234.
Na ekranie głównym zobaczysz dodane miejsca.
Kliknij na dowolne miejsce, aby je zarezerwować.

--- KROK 8g: Zarejestruj zwykłego użytkownika przez aplikację ---

Na ekranie logowania kliknij "Zarejestruj się" i wypełnij formularz.
Zwykły użytkownik może przeglądać i rezerwować miejsca, ale nie ma dostępu do panelu admina.



================================================================================
  9. OPIS WSZYSTKICH ENDPOINTÓW API
================================================================================

AUTH SERVICE (port 8000)
------------------------

  POST /api/v1/auth/register
    Rejestracja nowego użytkownika
    Body: { "email": "...", "full_name": "...", "password": "..." }
    Zwraca: obiekt UserDto (201)

  POST /api/v1/auth/login
    Logowanie (format application/x-www-form-urlencoded)
    Body: username=...&password=...
    Zwraca: { access_token, refresh_token, token_type }

  POST /api/v1/auth/refresh
    Odświeżenie tokenu dostępu
    Body: { "refresh_token": "..." }
    Zwraca: nowe { access_token, refresh_token }

  POST /api/v1/auth/verify
    Weryfikacja tokenu (użytek wewnętrzny)
    Body: { "refresh_token": "twoj_access_token" }  ← tak, access_token w tym polu
    Zwraca: { user_id, valid }

  GET  /api/v1/users/me
    Pobranie profilu aktualnego użytkownika
    Header: Authorization: Bearer <access_token>
    Zwraca: obiekt UserDto

  PATCH /api/v1/users/me
    Aktualizacja profilu (imię, hasło)
    Header: Authorization: Bearer <access_token>
    Body: { "full_name": "...", "password": "..." }  ← oba pola opcjonalne


RESERVATION SERVICE (port 8001)
--------------------------------

  GET  /api/v1/spaces/
    Lista miejsc (z opcjonalnym filtrowaniem)
    Header: Authorization: Bearer <access_token>
    Query params:
      ?floor=1           filtruj po piętrze
      ?space_type=desk   filtruj po typie (desk / meeting_room)
      ?date=2026-05-10   pokaż z informacją o dostępności w tym dniu
    Zwraca: lista SpaceDto

  GET  /api/v1/spaces/{id}
    Szczegóły miejsca
    Header: Authorization: Bearer <access_token>
    Zwraca: SpaceDto

  POST /api/v1/spaces/
    Dodanie nowego miejsca (tylko admin)
    Header: Authorization: Bearer <access_token>
    Body: { "name", "description", "space_type", "floor", "capacity", "pos_x", "pos_y" }
    Zwraca: SpaceDto (201)

  PATCH /api/v1/spaces/{id}
    Aktualizacja miejsca (tylko admin)
    Body: dowolne pola z SpaceDto (opcjonalne)

  DELETE /api/v1/spaces/{id}
    Usunięcie miejsca (tylko admin, 204 No Content)

  GET  /api/v1/reservations/
    Lista rezerwacji
    Zwykły użytkownik widzi tylko swoje rezerwacje
    Admin widzi wszystkie
    Header: Authorization: Bearer <access_token>
    Zwraca: lista ReservationDto

  POST /api/v1/reservations/
    Stworzenie rezerwacji
    Header: Authorization: Bearer <access_token>
    Body: { "space_id", "start_time", "end_time", "notes" }
    Daty w formacie ISO 8601, np: "2026-05-10T09:00:00Z"
    Zwraca: ReservationDto (201) lub 409 jeśli termin zajęty

  GET  /api/v1/reservations/{id}
    Szczegóły rezerwacji
    Dostępne tylko dla właściciela lub admina

  DELETE /api/v1/reservations/{id}
    Anulowanie rezerwacji (zmiana statusu na "cancelled")
    Właściciel może anulować swoją, admin może anulować każdą


KODY ODPOWIEDZI HTTP (co oznaczają)
-------------------------------------
  200 OK           — sukces, zwraca dane
  201 Created      — zasób stworzony pomyślnie
  204 No Content   — sukces, brak danych do zwrócenia (np. DELETE)
  400 Bad Request  — błędne dane wejściowe (walidacja)
  401 Unauthorized — brak tokenu lub token nieważny
  403 Forbidden    — brak uprawnień (np. nie-admin próbuje dodać miejsce)
  404 Not Found    — zasób nie istnieje
  409 Conflict     — konflikt (np. email zajęty, termin zajęty)
  422 Unprocessable— błąd walidacji Pydantic (szczegóły w response body)



================================================================================
  10. ROZWIĄZYWANIE PROBLEMÓW
================================================================================

PROBLEM: "docker compose up" zwraca błąd "Cannot connect to Docker daemon"
ROZWIĄZANIE: Uruchom Docker Desktop i poczekaj aż ikona przestanie się animować.

PROBLEM: Port 8000 lub 8001 jest już zajęty
ROZWIĄZANIE: Zmień porty w docker-compose.yml:
  ports:
    - "8002:8000"   ← zmień 8000 na 8002
Pamiętaj, żeby zaktualizować też BuildConfig w Android.

PROBLEM: Backend zwraca 500 Internal Server Error
ROZWIĄZANIE: Sprawdź logi:
  docker compose logs auth_service
  docker compose logs reservation_service
Najczęstsze przyczyny: błędny SECRET_KEY w .env, brak połączenia z DB.

PROBLEM: Tabele nie istnieją w bazie danych
ROZWIĄZANIE: Bazy są tworzone automatycznie przez SQLAlchemy przy starcie.
Jeśli to nie działa, zrestartuj serwisy:
  docker compose restart auth_service reservation_service

PROBLEM: "401 Unauthorized" przy każdym żądaniu do reservation-service
ROZWIĄZANIE: Upewnij się, że SECRET_KEY w reservation-service/.env jest
IDENTYCZNY jak w auth-service/.env. Token JWT jest podpisywany tym kluczem.

PROBLEM: Android Studio: "Unresolved reference" w kodzie Kotlin
ROZWIĄZANIE:
  1. File → Invalidate Caches → Invalidate and Restart
  2. Poczekaj na ponowną synchronizację Gradle

PROBLEM: Aplikacja Android nie może połączyć się z backendem
ROZWIĄZANIE:
  1. Upewnij się, że emulator jest uruchomiony i backend działa
  2. Sprawdź czy adres IP jest poprawny:
     - Emulator: 10.0.2.2 (zawsze)
     - Fizyczne urządzenie: IP komputera w sieci lokalnej
  3. Sprawdź czy firewall nie blokuje portów 8000/8001

PROBLEM: "Network on main thread exception"
ROZWIĄZANIE: To nie powinno wystąpić — Retrofit z Kotlin coroutines jest asynchroniczny.
Jeśli wystąpi, sprawdź czy wywołania API są w bloku suspend lub viewModelScope.launch.

PROBLEM: Baza danych pełna starych danych przy testach
ROZWIĄZANIE: Zresetuj wszystko:
  docker compose down -v
  docker compose up --build

PROBLEM: Android Room: "Migration required" po zmianie schematu
ROZWIĄZANIE: W trakcie developmentu użyj:
  .fallbackToDestructiveMigration()
(już skonfigurowane w CoworkingDatabase.kt — Room skasuje i odtworzy lokalną bazę)



================================================================================
  DODATKOWE INFORMACJE
================================================================================

GDZIE SĄ DANE?
  - Baza auth_db: przechowuje konta użytkowników i hasła (bcrypt)
  - Baza reservation_db: przechowuje miejsca i rezerwacje
  - Dane przetrwają restart kontenerów (Docker volumes)
  - Usunięcie volumes: docker compose down -v (TRWAŁE USUNIĘCIE DANYCH)

BEZPIECZEŃSTWO
  - Hasła są hashowane algorytmem bcrypt (nigdy nie przechowujemy plaintext)
  - JWT tokeny są podpisane kluczem HMAC-SHA256
  - Access token wygasa po 30 minutach
  - Refresh token wygasa po 7 dniach
  - Tokeny są przechowywane w Android DataStore (szyfrowane preferencje)
  - Nigdy nie umieszczaj plików .env w repozytorium Git

ARCHITEKTURA (skrót)
  - Auth Service (port 8000): odpowiada za rejestrację, logowanie, JWT
  - Reservation Service (port 8001): zarządza miejscami i rezerwacjami
  - Oba serwisy mają osobne bazy PostgreSQL
  - Android app: komunikuje się bezpośrednio z oboma serwisami przez Retrofit
  - Room Database: lokalna cache offline (dane odświeżane przy każdym starcie)
  - Hilt: dependency injection (automatyczne wstrzykiwanie zależności)
  - Jetpack Compose: deklaratywny UI


