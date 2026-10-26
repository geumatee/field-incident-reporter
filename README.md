# Field Incident Reporter

A facility maintenance reporting system built with an Android client for field workers, an operator triage web console, and a local mock REST API.

---

## Demo Videos & Screen Recordings

Recorded walkthroughs demonstrating the core flows across both platforms:

| Feature / Flow | Platform | Recording Link | Notes |
| :--- | :--- | :--- | :--- |
| **Full End-to-End Walkthrough** | Android & Web | <video src="https://github.com/user-attachments/assets/89861ad2-c528-45d2-afe1-f8e878ea09de" width="600" controls></video> | Complete 1–2 min flow: reporting on Android, triaging on Web, resolving incident |
| **Android Incident Capture** | Android | <video src="https://github.com/user-attachments/assets/c6df4a6a-f9f4-4ce5-b03f-6ae5d304e6d2" width="600" controls></video> | Form input validation, photo capture/picker, theme toggle, and offline handling |
| **Operator Triage Console** | Web | <video src="https://github.com/user-attachments/assets/157c5f97-3c5a-4276-b767-4a2e2a48fe5f" width="600" controls></video> | KPI metrics, search, filtering, inline severity/status updates, and photo preview |

---

## Quick Start

Run these three components in separate terminals:

### 1. Mock Backend (Port 3001)

```bash
cd backend
npm install
npm start
```

API is hosted at `http://localhost:3001/incidents` (accessible from Android emulator at `http://10.0.2.2:3001/incidents`).

### 2. Android App

Open `android/` in Android Studio, or build and install via terminal:

```bash
cd android
./gradlew assembleDebug
```

Run on an emulator or device running Android 10+ (API 29+).

> **Android 16 note:** Android 16 (API 37) requires `ACCESS_LOCAL_NETWORK` permission to access host loopback (`10.0.2.2`). The app prompts for this on launch. If denied, the app provides a screen with a fallback button to App Settings.

### 3. Web Dashboard (Port 3000)

```bash
cd web-dashboard
npm install
npm run dev
```

Open `http://localhost:3000` in your browser.

---

## Testing

### Android Tests

```bash
cd android

# Run 32 unit tests (DTOs, Repository, ViewModels)
./gradlew testDebugUnitTest

# Run Compose UI tests (requires running emulator/device)
./gradlew connectedDebugAndroidTest
```

### Web Dashboard Build Check

```bash
cd web-dashboard
npm run build
```

---

## Architecture & Notes

- **Manual DI (`AppContainer`):** Kept dependencies lightweight with `AppContainer` instead of Hilt. This avoids kapt/ksp code-generation overhead and keeps clean build times under 4 seconds. For a larger app with dozens of screens, migrating to Hilt would be the right trade-off.
- **In-Memory SSOT (`MutableSharedFlow`):** `IncidentRepository` exposes an `incidentsFlow` (replay = 1) that automatically refreshes when incidents are created, updated, or deleted. This avoids passing reload callbacks between screens. Room was skipped for simplicity since offline database persistence wasn't required.
- **Structured Concurrency (`runCatchingCancellable`):** Custom inline wrapper around `runCatching` that re-throws `CancellationException` so coroutine scopes cancel cleanly when leaving screens.
- **Fakes Over Mocks:** Unit tests use `FakeIncidentApiService` instead of MockK/Mockito to avoid fragile method call verification and enable testing async gates cleanly with `CompletableDeferred`.
- **Photo Compression:** Photos are downscaled to 480px JPEG (~20-40 KB) and stored as Base64 Data URIs directly in JSON so `json-server` can persist them without a dedicated S3 bucket or file server.
