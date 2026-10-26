# Field Incident Reporter

A facility maintenance reporting system built with an Android client for field workers, an operator triage web console, and a local mock REST API.

---

## Architecture Overview

```
┌────────────────────────────────┐       ┌────────────────────────────────┐
│       Android Mobile App       │       │      Web Operator Console      │
│  (Compose, MVVM, Coroutines)   │       │     (Next.js 16, React 19)     │
└──────────────┬─────────────────┘       └────────────────┬───────────────┘
               │                                          │
               │ REST (Retrofit / OkHttp)                 │ REST (fetch)
               ▼                                          ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                     Mock REST API (json-server :3001)                   │
│                    Database Storage: backend/db.json                    │
└─────────────────────────────────────────────────────────────────────────┘
```

### Project Structure

```
field-incident-reporter/
├── android/            # Android client (Kotlin, Jetpack Compose, Material 3)
│   ├── app/src/main/   # Feature packages: ui/ (form, list), data/, model/
│   ├── app/src/test/   # 34 unit tests (DTOs, Repository, ViewModels)
│   └── app/src/androidTest/ # 20 Compose UI integration tests
├── backend/            # Mock REST API (json-server, seed data, reset script)
├── web-dashboard/      # Next.js 16 triage dashboard (React 19, Tailwind CSS v4)
└── docs/               # REST API contract specification and demo videos
```

---

## Demo Videos & Screen Recordings

Recorded walkthroughs demonstrating the core flows across both platforms:

| Feature / Flow | Platform | Recording Link | Notes |
| :--- | :--- | :--- | :--- |
| **Full End-to-End Walkthrough** | Android & Web | <video src="https://github.com/user-attachments/assets/89861ad2-c528-45d2-afe1-f8e878ea09de" width="600" controls></video> | Complete 1–2 min flow: reporting on Android, triaging on Web, resolving incident |
| **Android Incident Capture** | Android | <video src="https://github.com/user-attachments/assets/c6df4a6a-f9f4-4ce5-b03f-6ae5d304e6d2" width="600" controls></video> | Form input validation, photo capture/picker, theme toggle, and offline handling |
| **Operator Triage Console** | Web | <video src="https://github.com/user-attachments/assets/157c5f97-3c5a-4276-b767-4a2e2a48fe5f" width="600" controls></video> | KPI metrics, search, filtering, inline severity/status updates, and photo preview |

---

## Prerequisites

Before running the project, ensure your environment has:

- **Node.js:** v20.x or higher (`node -v`) and npm v10+
- **JDK:** Java 17+ (e.g. Eclipse Adoptium or Android Studio bundled JBR)
- **Android Studio:** Ladybug / Meerkat or later (supports AGP 9.4 and compileSdk 37)
- **Android Runtime:** Android Emulator or physical device running Android 10+ (API 29+)

---

## Quick Start

Run these three components in separate terminals:

### 1. Mock Backend (Port 3001)

```bash
cd backend
npm install
npm start
```

API runs at `http://localhost:3001/incidents` (accessible from the Android emulator at `http://10.0.2.2:3001/incidents`).

> **Resetting Demo Data:** During testing and evaluation, creating or editing incidents modifies `backend/db.json`. To restore the database to its pristine seed state at any time, run:
> ```bash
> npm run reset
> ```

### 2. Android App

Open `android/` in Android Studio, or build and install via terminal:

```bash
cd android
./gradlew assembleDebug
```

Run on an Android emulator running Android 10+ (API 29+).

> **Physical Device Connectivity:** The Android client defaults to the emulator loopback interface (`10.0.2.2:3001`). If deploying to a physical hardware device connected via USB, enable reverse port-forwarding:
> ```bash
> adb reverse tcp:3001 tcp:3001
> ```

> **Android 17 note:** Android 17 (API 37) requires `ACCESS_LOCAL_NETWORK` permission to access host loopback (`10.0.2.2`). The app prompts for this on launch. If denied, the app provides a screen with a fallback button to App Settings.

### 3. Web Dashboard (Port 3000)

```bash
cd web-dashboard
npm install
npm run dev
```

Open `http://localhost:3000` in your browser.

---

## Testing & Quality Checks

### Android Tests

```bash
cd android

# Run 34 unit tests (DTOs, Repository, ViewModels)
./gradlew testDebugUnitTest

# Run 20 Compose UI tests (requires running emulator/device)
./gradlew connectedDebugAndroidTest
```

### Web Dashboard Checks

```bash
cd web-dashboard
npm run lint
npm run build
```

---

## Architecture & Notes

- **API Contract:** Comprehensive REST endpoint specifications, field ownership, error envelope, and concurrency considerations are documented in [docs/api-contract.md](docs/api-contract.md).
- **Manual DI (`AppContainer`):** Kept dependencies lightweight with `AppContainer` instead of Hilt. This avoids kapt/ksp code-generation overhead and keeps clean build times fast. For a larger app with dozens of screens, migrating to Hilt would be the right trade-off.
- **In-Memory SSOT (`MutableSharedFlow`):** `IncidentRepository` exposes an `incidentsFlow` (replay = 1) that automatically refreshes when incidents are created, updated, or deleted. This avoids passing reload callbacks between screens. Room was skipped for simplicity since offline database persistence wasn't required.
- **Structured Concurrency (`runCatchingCancellable`):** Custom inline wrapper around `runCatching` that re-throws `CancellationException` so coroutine scopes cancel cleanly when leaving screens.
- **Fakes Over Mocks:** Unit tests use `FakeIncidentApiService` instead of MockK/Mockito to avoid fragile method call verification and enable testing async gates cleanly with `CompletableDeferred`.
- **Photo Compression:** Photos are downscaled to 480px JPEG (~20-40 KB) and stored as Base64 Data URIs directly in JSON so `json-server` can persist them without a dedicated S3 bucket or file server.

---

## How I Used AI

### Where AI Accelerated Development
- **Boilerplate & Scaffolding:** Accelerated initial Jetpack Compose screen layouts, Retrofit interface declarations, DTO mappers, and Next.js Tailwind KPI card components.
- **Test Generation & Coverage:** Assisted in brainstorming comprehensive boundary conditions across form validation, network edge cases, filter combinations, and Coroutine cancellation paths.
- **Realistic Seed Data:** Assisted in synthesizing realistic facility maintenance records in `db.seed.json` across varied severities and categories.

