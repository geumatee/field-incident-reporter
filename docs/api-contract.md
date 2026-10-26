# API Contract

Simple REST contract used by both the Android client and the Next.js triage dashboard against the mock backend (`json-server` on port 3001).

- **Local:** `http://localhost:3001`
- **Android Emulator:** `http://10.0.2.2:3001`

---

## 1. Schema (`Incident`)

| Field | Type | Nullable | Description |
|---|---|:---:|---|
| `id` | string | No | Unique ID, e.g. `"inc-101"` or `"inc-<uuid>"` |
| `title` | string | No | Short incident summary (required) |
| `description` | string | No | Additional context / details (optional, defaults to `""`) |
| `category` | string | No | `"EQUIPMENT"` \| `"HAZARD"` \| `"ELECTRICAL"` \| `"OTHER"` |
| `severity` | string | No | `"LOW"` \| `"MEDIUM"` \| `"HIGH"` |
| `status` | string | No | `"OPEN"` \| `"IN_PROGRESS"` \| `"RESOLVED"` |
| `location` | string | No | Facility area / room (required) |
| `imageUrl` | string | Yes | Base64 JPEG data URI (`data:image/jpeg;base64,...`) or `null` |
| `createdAt` | string | No | ISO-8601 UTC timestamp (`YYYY-MM-DDTHH:mm:ssZ`) |
| `updatedAt` | string | No | ISO-8601 UTC timestamp (`YYYY-MM-DDTHH:mm:ssZ`) |

---

## 2. Endpoints

### `GET /incidents`
Fetch all incidents. Supports json-server sorting & filtering.

- **Query params:**
  - `_sort=createdAt&_order=desc` (default used by both clients)
  - `status`, `severity`, `category` (optional filters)
- **Response:** `200 OK` → `Incident[]`

```json
[
  {
    "id": "inc-101",
    "title": "Water leak near rack B4",
    "description": "AC pipe dripping water on the floor.",
    "category": "EQUIPMENT",
    "severity": "HIGH",
    "status": "OPEN",
    "location": "Building A, 3F",
    "imageUrl": null,
    "createdAt": "2026-10-23T08:30:00Z",
    "updatedAt": "2026-10-23T08:30:00Z"
  }
]
```

### `GET /incidents/:id`
- **Response:** `200 OK` → `Incident`
- **Error:** `404 Not Found`

### `POST /incidents`
Create a new report. `title` and `location` must not be blank.

- **Request:** `Incident`
- **Response:** `201 Created` → created `Incident`

### `PATCH /incidents/:id`
Update an incident. Supports partial payloads (e.g. status/severity triage from the web console).

- **Request:** Partial or full `Incident`
- **Response:** `200 OK` → updated `Incident`
- **Error:** `404 Not Found`

```json
// Example: Web operator updating status
{
  "status": "RESOLVED",
  "updatedAt": "2026-10-26T14:00:00Z"
}
```

### `DELETE /incidents/:id`
- **Response:** `200 OK` / `204 No Content`
- **Error:** `404 Not Found`

---

## 3. Design Notes & Trade-offs

- **Field Ownership:**
  - In this prototype, the client sets `id` (UUID) and timestamps on `POST` to keep the backend a vanilla `json-server`.
  - In production, `id`, `createdAt`, and `updatedAt` should be strictly server-generated. Clients would send a separate `CreateIncidentRequest` without these fields.

- **PATCH Semantics & Concurrency:**
  - The web dashboard sends partial updates (`status`, `severity`, `updatedAt`).
  - Android currently sends the full object. If an operator resolves an incident on the web while a technician has the edit screen open and saves a description edit, the Android save would overwrite the status back to `OPEN`.
  - Production solution: use partial PATCH DTOs on Android, or implement optimistic locking (`If-Match` with `updatedAt` or a `version` field returning `409 Conflict`).

- **Photo Storage:**
  - Photos are downscaled to 480px, compressed to JPEG, and stored inline as Base64 data URIs. This lets `json-server` persist everything in a single `db.json` without needing S3 or local disk storage.
  - In production, this would be a two-step upload: request a pre-signed S3/GCS URL, upload the binary image directly to object storage, and save the resulting CDN URL on the incident.

- **Error Handling:**
  - Client ViewModels map network/HTTP errors to user-friendly `@StringRes` strings rather than exposing raw server or socket error messages.
