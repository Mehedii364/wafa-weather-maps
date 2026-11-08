# Aurora — 3-in-1 Unified Super App
### Maps & Navigation • Weather Center • Gemini AI Assistant

Aurora is a unified, high-performance Android application built with modern **Kotlin**, **Jetpack Compose**, and **Material 3** following the **Aurora Obsidian** design system.

---

## 🌟 Key Features

1. **Interactive Maps & Location**:
   - Dynamic real-time map viewer with touch pan and zoom gestures.
   - GPS Current-Location detection and permission handling.
   - Search places with live geocoding (OpenStreetMap / Nominatim / Backend API).
   - Saved favorite locations with persistent local Room database storage.
   - Nearby points of interest (Cafes, Transit, Hospitals, Parks).
   - Turn-by-turn route previews with ETA, distance, and transit modes.

2. **Weather Center**:
   - Current temperature, high/low, feels-like, and weather conditions.
   - Comprehensive live weather metrics: humidity, wind speed, precipitation probability, pressure, UV index.
   - 24-hour hourly forecast and 7-day extended forecast.
   - Quick location switching with search and auto-detect GPS coordinates.
   - Real-time weather data integration via Open-Meteo API / Shared backend API.

3. **Gemini AI Assistant**:
   - Conversational assistant with streaming message bubbles and typing animation.
   - Smart prompt suggestions (e.g., weather analysis, route planning, local recommendations).
   - Contextual actions linking directly to Weather and Maps.
   - Voice input integration via SpeechRecognizer.
   - Conversation history persistence in Room database.

4. **Unified Profile & App Settings**:
   - Theme toggle: Aurora Dark (Obsidian) & Aurora Light.
   - Bilingual language selector: English & Bengali (বাংলা).
   - Backend URL configuration with live connectivity health check.
   - Cache clearing and storage status.

---

## 🎨 Design Identity: Aurora Obsidian

- **Dark Palette**:
  - Background: `#0B1020`
  - Surface: `#151E32`
  - Elevated Surface: `#25334C`
  - Accent Primary: `#4389FF`
  - Aurora Violet: `#8B72FF`
  - Aurora Cyan: `#36C8E8`
  - Status Success: `#35C99A`
- **Light Palette**:
  - Background: `#F4F7FC`
  - Surface: `#FFFFFF`
  - Elevated Surface: `#E8EEF8`
  - Accent Primary: `#2868E8`
- **Aesthetic**: Neumorphic depth, controlled glassmorphism, glowing aurora gradients, and responsive navigation (Bottom Bar on Mobile, Navigation Rail on Tablets).

---

## 🛠 Tech Stack

- **UI**: Jetpack Compose, Material 3, Compose Navigation.
- **Architecture**: MVVM with Clean Architecture & Repository pattern.
- **Networking**: Retrofit 2, Moshi, OkHttp 3.
- **Local Persistence**: AndroidX Room Database & DataStore Preferences.
- **Location**: Google Play Services Location (`FusedLocationProviderClient`).
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`), Java 17, Android SDK 36.

---

## 🚀 Building Locally

To build the debug APK using Gradle:

```bash
./gradlew assembleDebug
```

The output APK will be generated at:
```text
app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 Automated CI/CD & Downloadable APK

Every push to the `main` branch triggers an automated GitHub Actions pipeline (`.github/workflows/android-build.yml`):
1. Sets up JDK 17 and Android SDK.
2. Resolves dependencies and compiles `assembleDebug`.
3. Validates APK size, unzip integrity, and SHA-256 checksums.
4. Copies and stages the APK into:
   - `APK_DOWNLOAD/app-debug.apk`
   - `.build-outputs/app-debug.apk`
5. Uploads the build artifact as `app-debug-apk`.
