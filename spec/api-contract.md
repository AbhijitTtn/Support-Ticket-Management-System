# Support Ticket Management System — API Contract

## 1. Purpose

This document defines the REST/JSON API contract between the React frontend and the Spring Boot backend. All field constraints align with [data-model.md](./data-model.md). Status rules align with [state-machine.md](./state-machine.md).

**Base URL (local):** `http://localhost:8080/api`

**Content type:** `application/json` for all request and response bodies.

**ID format:** UUID strings.

**Timestamps:** ISO-8601 UTC instants, e.g. `"2026-09-11T10:00:00Z"`.

---

## 2. Shared Types

### 2.1 Enums

```json
// Priority
"LOW" | "MEDIUM" | "HIGH" | "CRITICAL"

// TicketStatus
"OPEN" | "IN_PROGRESS" | "RESOLVED" | "CLOSED" | "CANCELLED"
```

### 2.2 TicketSummary

Used in list responses.

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Cannot reset password",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "jane.smith",
  "createdAt": "2026-09-11T10:00:00Z",
  "updatedAt": "2026-09-11T10:00:00Z"
}
```

### 2.3 Comment

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "ticketId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "author": "support.agent",
  "body": "Verified SMTP logs; retrying delivery.",
  "createdAt": "2026-09-11T11:30:00Z"
}
```

### 2.4 TicketDetail

Extends summary with `description` and `comments`.

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Cannot reset password",
  "description": "User reports password reset email never arrives after three attempts.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "jane.smith",
  "createdAt": "2026-09-11T10:00:00Z",
  "updatedAt": "2026-09-11T10:00:00Z",
  "comments": [
    {
      "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
      "ticketId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
      "author": "support.agent",
      "body": "Verified SMTP logs; retrying delivery.",
      "createdAt": "2026-09-11T11:30:00Z"
    }
  ]
}
```

Comments are ordered by `createdAt` ascending.

### 2.5 ErrorResponse

All error responses use this shape.

```json
{
  "timestamp": "2026-09-11T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tickets",
  "fieldErrors": [
    {
      "field": "title",
      "message": "Title must be between 3 and 120 characters"
    }
  ]
}
```

| Field | Type | Description |
|-------|------|-------------|
| `timestamp` | string | ISO-8601 instant when error occurred |
| `status` | number | HTTP status code (duplicated in body) |
| `error` | string | HTTP reason phrase |
| `message` | string | Human-readable summary |
| `path` | string | Request path |
| `fieldErrors` | array | Per-field validation errors; empty array when N/A |

**FieldError object:**

```json
{
  "field": "title",
  "message": "Title must be between 3 and 120 characters"
}
```

---

## 3. Endpoints

### 3.1 POST /api/tickets — Create ticket

Creates a new ticket with status `OPEN`.

**Request body:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | yes | 3–120 chars |
| `description` | string | yes | 5–5000 chars |
| `priority` | Priority | yes | valid enum |
| `assignee` | string | no | max 120 chars; omit or null for unassigned |

**Example request:**

```json
{
  "title": "Cannot reset password",
  "description": "User reports password reset email never arrives after three attempts.",
  "priority": "HIGH",
  "assignee": "jane.smith"
}
```

**Success: `201 Created`**

Returns `TicketDetail` with empty `comments` array.

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Cannot reset password",
  "description": "User reports password reset email never arrives after three attempts.",
  "priority": "HIGH",
  "status": "OPEN",
  "assignee": "jane.smith",
  "createdAt": "2026-09-11T10:00:00Z",
  "updatedAt": "2026-09-11T10:00:00Z",
  "comments": []
}
```

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Validation failure (missing/invalid fields) |
| 400 | Malformed JSON |
| 500 | Unexpected server error |

**Example 400:**

```json
{
  "timestamp": "2026-09-11T10:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed",
  "path": "/api/tickets",
  "fieldErrors": [
    { "field": "title", "message": "Title must be between 3 and 120 characters" }
  ]
}
```

---

### 3.2 GET /api/tickets — List, search, and filter

Returns ticket summaries matching optional filters.

**Query parameters:**

| Parameter | Type | Required | Description |
|-----------|------|----------|-------------|
| `q` | string | no | Case-insensitive keyword search in `title` OR `description` |
| `status` | TicketStatus | no | Exact status filter |

**Filter semantics:**

- No parameters → all tickets
- `q` only → tickets matching keyword
- `status` only → tickets with that status
- Both → logical **AND** (must match keyword and status)
- `q` blank or whitespace-only → treated as absent (no text filter)

**Example:** `GET /api/tickets?q=password&status=OPEN`

**Success: `200 OK`**

```json
[
  {
    "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
    "title": "Cannot reset password",
    "priority": "HIGH",
    "status": "OPEN",
    "assignee": "jane.smith",
    "createdAt": "2026-09-11T10:00:00Z",
    "updatedAt": "2026-09-11T10:00:00Z"
  }
]
```

Returns `[]` when no matches (not an error).

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Invalid `status` enum value |
| 500 | Unexpected server error |

---

### 3.3 GET /api/tickets/{id} — Get ticket detail

**Path parameter:** `id` — UUID

**Success: `200 OK`**

Returns `TicketDetail` including `comments`.

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Malformed UUID in path |
| 404 | Ticket not found |
| 500 | Unexpected server error |

**Example 404:**

```json
{
  "timestamp": "2026-09-11T10:00:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Ticket not found: 3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "path": "/api/tickets/3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "fieldErrors": []
}
```

---

### 3.4 PATCH /api/tickets/{id} — Update ticket fields

Updates `title`, `description`, `priority`, and `assignee`. Does **not** change `status`.

**Path parameter:** `id` — UUID

**Request body:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `title` | string | yes | 3–120 chars |
| `description` | string | yes | 5–5000 chars |
| `priority` | Priority | yes | valid enum |
| `assignee` | string | no | max 120 chars; null or `""` clears assignee |

**Example request:**

```json
{
  "title": "Cannot reset password — escalated",
  "description": "User reports password reset email never arrives. SMTP queue checked.",
  "priority": "CRITICAL",
  "assignee": "john.doe"
}
```

**Success: `200 OK`**

Returns updated `TicketDetail` (comments unchanged).

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Validation failure |
| 400 | Malformed UUID or JSON |
| 404 | Ticket not found |
| 500 | Unexpected server error |

---

### 3.5 PATCH /api/tickets/{id}/status — Transition status

Changes ticket status according to [state-machine.md](./state-machine.md).

**Path parameter:** `id` — UUID

**Request body:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `status` | TicketStatus | yes | Must be a valid target for current state |

**Example request:**

```json
{
  "status": "IN_PROGRESS"
}
```

**Success: `200 OK`**

Returns updated `TicketDetail`.

```json
{
  "id": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "title": "Cannot reset password",
  "description": "User reports password reset email never arrives after three attempts.",
  "priority": "HIGH",
  "status": "IN_PROGRESS",
  "assignee": "jane.smith",
  "createdAt": "2026-09-11T10:00:00Z",
  "updatedAt": "2026-09-11T12:00:00Z",
  "comments": []
}
```

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Missing or invalid `status` enum |
| 400 | Malformed UUID or JSON |
| 404 | Ticket not found |
| **409** | **Invalid status transition** (see [state-machine.md](./state-machine.md)) |
| 500 | Unexpected server error |

**Example 409:**

```json
{
  "timestamp": "2026-09-11T12:00:00Z",
  "status": 409,
  "error": "Conflict",
  "message": "Invalid status transition from OPEN to RESOLVED",
  "path": "/api/tickets/3fa85f64-5717-4562-b3fc-2c963f66afa6/status",
  "fieldErrors": []
}
```

---

### 3.6 POST /api/tickets/{id}/comments — Add comment

**Path parameter:** `id` — UUID (ticket id)

**Request body:**

| Field | Type | Required | Constraints |
|-------|------|----------|-------------|
| `author` | string | yes | 1–120 chars |
| `body` | string | yes | 1–2000 chars |

**Example request:**

```json
{
  "author": "support.agent",
  "body": "Verified SMTP logs; retrying delivery."
}
```

**Success: `201 Created`**

Returns `Comment` (not full ticket).

```json
{
  "id": "7c9e6679-7425-40de-944b-e07fc1f90ae7",
  "ticketId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "author": "support.agent",
  "body": "Verified SMTP logs; retrying delivery.",
  "createdAt": "2026-09-11T11:30:00Z"
}
```

**Error responses:**

| Status | Condition |
|--------|-----------|
| 400 | Validation failure |
| 400 | Malformed UUID or JSON |
| 404 | Ticket not found |
| 500 | Unexpected server error |

---

## 4. HTTP Status Code Summary

| Code | Usage |
|------|-------|
| 200 | Successful GET, PATCH |
| 201 | Successful POST (create ticket, create comment) |
| 400 | Validation error, malformed JSON, invalid query/path enum |
| 404 | Ticket not found |
| 409 | Invalid status transition only |
| 500 | Unhandled server error (no stack trace in body) |

---

## 5. Validation Rules Reference

Mirrors [data-model.md](./data-model.md).

| Field | Min | Max | Required on |
|-------|-----|-----|-------------|
| `title` | 3 | 120 | create, update |
| `description` | 5 | 5000 | create, update |
| `assignee` | — | 120 | optional on create, update |
| `author` | 1 | 120 | create comment |
| `body` (comment) | 1 | 2000 | create comment |
| `priority` | — | — | create, update |
| `status` | — | — | status transition only |

---

## 6. Endpoint Quick Reference

| Method | Path | Success | Response type |
|--------|------|---------|-----------------|
| POST | `/api/tickets` | 201 | TicketDetail |
| GET | `/api/tickets` | 200 | TicketSummary[] |
| GET | `/api/tickets/{id}` | 200 | TicketDetail |
| PATCH | `/api/tickets/{id}` | 200 | TicketDetail |
| PATCH | `/api/tickets/{id}/status` | 200 | TicketDetail |
| POST | `/api/tickets/{id}/comments` | 201 | Comment |

---

## 7. Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-09-11 | Initial API contract |
