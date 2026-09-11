# Frontend Smoke Test Results

**Date:** 2026-09-11  
**Environment:** Backend `:8080` (H2 file DB), Frontend `:5173` (Vite dev + `/api` proxy)

---

## 1. Spec Compliance Review

Reviewed frontend source against [ui-flow.md](./ui-flow.md) and [api-contract.md](./api-contract.md).

### Compliant

| Area | Status |
|------|--------|
| Routes `/`, `/tickets/new`, `/tickets/:id` | OK |
| Global header: "Support Tickets", Dashboard, "+ New Ticket" | OK |
| List: `GET /api/tickets`, columns, empty state, loading spinner | OK |
| Search: 300 ms debounce, blank clears `q`, combined with status (AND) | OK |
| Status filter: All + all five statuses | OK |
| Create: `POST /api/tickets`, default priority MEDIUM, redirect to detail on 201 | OK |
| Detail: `GET /api/tickets/{id}`, 404 page, full-page spinner | OK |
| Edit: `PATCH /api/tickets/{id}`, inline toggle, field errors inline | OK |
| Status actions mirror state machine; terminal messages for CLOSED/CANCELLED | OK |
| Cancel confirmation dialog for destructive transition | OK |
| 409 on illegal transition → ErrorBanner with API `message` | OK |
| Comments: `POST /api/tickets/{id}/comments`, count header, empty state | OK |
| Error display: ErrorBanner + FieldError; no stack traces | OK |
| API client: correct paths, JSON parsing, network error message | OK |
| Priority/status badges with color coding | OK |

### Minor gaps (non-blocking)

| Gap | Spec reference | Notes |
|-----|----------------|-------|
| Create submit shows "Saving…" instead of "Creating…" | ui-flow §4.5 | Shared `TicketForm` loading label |
| No success toast after create | ui-flow §4.3 | Marked optional in spec |
| No HTML `minLength` on title/description | ui-flow §4.1 | Server-side validation still enforced; API errors shown inline |
| Search/filter disabled only on initial load | ui-flow §3.5 | Disabled when `loading && tickets.length === 0` only |

### Bug fixed during review

**Comment form cleared on validation failure** — `CommentForm` always reset fields after submit even when the API returned 400. Fixed to clear only on success; `TicketDetail` now propagates errors from `handleAddComment`.

---

## 2. Frontend Test Strategy

No Vitest/Jest/Testing Library is configured (`package.json` has only `dev`, `build`, `preview`). Per [test-strategy.md](./test-strategy.md) §3.2, v1 relies on **manual smoke** rather than adding a new test runner.

**Recommendation:** Add Vitest later for pure utilities (`statusTransitions.ts`, `errors.ts`) if automated frontend tests are desired. Component/E2E tests are out of scope for v1.

---

## 3. Manual Smoke Checklist

Executed with both servers running locally. API steps verified via `curl` against backend and Vite proxy; UI behavior for status buttons verified against `statusTransitions.ts` (mirrors state machine).

| # | Step | Result |
|---|------|--------|
| 1 | Start backend | **PASS** — Spring Boot on `:8080` |
| 2 | Start frontend | **PASS** — Vite on `:5173`, proxy `/api` → backend |
| 3 | Create a ticket | **PASS** — `POST /api/tickets` → 201, status `OPEN` |
| 4 | Confirm in list | **PASS** — ticket ID present in `GET /api/tickets` |
| 5 | Search for it | **PASS** — `q=Smoke test` returns matching ticket |
| 6 | Filter by OPEN | **PASS** — `q=…&status=OPEN` returns only OPEN tickets |
| 7 | Open detail | **PASS** — `GET /api/tickets/{id}` returns full detail |
| 8 | Edit title/description/priority/assignee | **PASS** — `PATCH` → 200, fields updated |
| 9 | Add a comment | **PASS** — `POST` → 201, comment on detail GET |
| 10 | OPEN → IN_PROGRESS | **PASS** — 200 |
| 11 | IN_PROGRESS → RESOLVED | **PASS** — 200 |
| 12 | RESOLVED → CLOSED | **PASS** — 200 |
| 13 | Illegal transitions not offered by UI | **PASS** — `CLOSED`/`CANCELLED` show no buttons; illegal targets absent from `getLegalStatusActions` |
| 14 | Backend rejects illegal transition (409) | **PASS** — `CLOSED → OPEN` → 409, message *"Invalid status transition from CLOSED to OPEN"*, status unchanged |
| 15 | API/validation errors readable | **PASS** — 400 returns `message` + `fieldErrors` per field; no stack trace in body |

### Sample validation error (step 15)

```json
{
  "status": 400,
  "message": "Validation failed",
  "fieldErrors": [
    { "field": "description", "message": "Description must be between 5 and 5000 characters" },
    { "field": "title", "message": "Title must be between 3 and 120 characters" }
  ]
}
```

Frontend maps these to `FieldError` components under each form field via `fieldErrorsToMap`.

### Sample 409 error (step 14)

```json
{
  "status": 409,
  "message": "Invalid status transition from CLOSED to OPEN",
  "fieldErrors": []
}
```

Frontend displays this in `ErrorBanner` on the detail page; status badge remains `CLOSED`.

---

## 4. Overall Result

**PASS** — Full create → search → filter → edit → comment → lifecycle → terminal-state smoke completed successfully.

---

## 5. How to Re-run

```bash
# Terminal 1 — backend
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
cd backend && ./mvnw spring-boot:run

# Terminal 2 — frontend
cd frontend && npm run dev
```

Then walk through [test-strategy.md](./test-strategy.md) §12 in the browser at http://localhost:5173/.
