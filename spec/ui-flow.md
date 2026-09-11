# Support Ticket Management System — UI Flow

## 1. Purpose

This document defines user-facing screens, interactions, and UX behavior for the React/Vite frontend. All API calls follow [api-contract.md](./api-contract.md). Status actions follow [state-machine.md](./state-machine.md).

**Related specifications:**

- [requirements.md](./requirements.md) — AC-10.x error display requirements
- [architecture.md](./architecture.md) — frontend structure and API client

---

## 2. Navigation & Routes

| Route | Page | Purpose |
|-------|------|---------|
| `/` | Dashboard / Ticket List | Default landing; browse, search, filter |
| `/tickets/new` | Create Ticket | New ticket form |
| `/tickets/:id` | Ticket Detail | View, edit, transition status, comment |

**Global header** (all pages):

- App title: "Support Tickets"
- Link to Dashboard (`/`)
- Link/button: "New Ticket" → `/tickets/new`

---

## 3. Dashboard / Ticket List (`/`)

### 3.1 Layout

```
┌─────────────────────────────────────────────────────────┐
│  Support Tickets                    [+ New Ticket]      │
├─────────────────────────────────────────────────────────┤
│  Search: [________________________]  Status: [All ▼]    │
├─────────────────────────────────────────────────────────┤
│  Title              Priority  Status    Assignee  Updated │
│  ─────────────────────────────────────────────────────── │
│  Cannot reset...    HIGH      OPEN      jane.s    2h ago │
│  Login page 500     CRITICAL  IN_PROG   —         1d ago │
└─────────────────────────────────────────────────────────┘
```

### 3.2 Data loading

- On mount: `GET /api/tickets` (no query params)
- Show **loading skeleton or spinner** while fetching
- On success: render table/list of `TicketSummary` rows
- On empty result: show empty state — *"No tickets found."*
- Row click navigates to `/tickets/{id}`

### 3.3 Displayed columns

| Column | Source field | Format |
|--------|--------------|--------|
| Title | `title` | Truncate with ellipsis if > 60 chars |
| Priority | `priority` | Color-coded badge |
| Status | `status` | Badge (see §8) |
| Assignee | `assignee` | Display value or "—" if null |
| Updated | `updatedAt` | Relative time (e.g. "2 hours ago") or locale date |

### 3.4 Search & filter

**Search input (`q`):**

- Debounced (300 ms) or triggered on Enter / Search button
- Calls `GET /api/tickets?q={keyword}` combined with active status filter
- Blank/whitespace input → clears text filter (reload without `q`)

**Status filter dropdown:**

| Option | API param |
|--------|-----------|
| All | omit `status` |
| Open | `status=OPEN` |
| In Progress | `status=IN_PROGRESS` |
| Resolved | `status=RESOLVED` |
| Closed | `status=CLOSED` |
| Cancelled | `status=CANCELLED` |

- Changing filter immediately refetches with current search term
- Active filter label visible above results (e.g. *"Showing: Open tickets"*)

**Combined behavior:** `GET /api/tickets?q=password&status=OPEN` (AND semantics per api-contract)

### 3.5 Error handling

| API result | UI behavior |
|------------|-------------|
| 400 (invalid status param) | Error banner with `message` |
| 5xx / network failure | Banner: *"Unable to load tickets. Please try again."* + Retry button |
| Loading | Disable search/filter controls or show inline spinner |

---

## 4. Create Ticket (`/tickets/new`)

### 4.1 Form fields

| Field | Input type | Required | Client validation (mirrors API) |
|-------|------------|----------|-------------------------------|
| Title | text | yes | 3–120 chars |
| Description | textarea | yes | 5–5000 chars |
| Priority | select | yes | LOW, MEDIUM, HIGH, CRITICAL |
| Assignee | text | no | max 120 chars |

Default priority: `MEDIUM`.

### 4.2 Actions

- **Create** — `POST /api/tickets`
- **Cancel** — navigate to `/` without saving

### 4.3 Success flow

1. Show loading state on submit button ("Creating…")
2. On `201`: navigate to `/tickets/{id}` for the new ticket
3. Optional brief success toast: *"Ticket created"*

### 4.4 Error handling

| API result | UI behavior |
|------------|-------------|
| 400 + `fieldErrors` | Inline error under each field; preserve all entered values |
| 400 (general) | Banner with `message` |
| 5xx / network | Banner: *"Failed to create ticket. Please try again."* |

### 4.5 Loading state

- Submit button disabled + spinner while request in flight
- Form fields remain editable until submit starts

---

## 5. Ticket Detail (`/tickets/:id`)

### 5.1 Layout

```
┌─────────────────────────────────────────────────────────┐
│  ← Back to list                                         │
├─────────────────────────────────────────────────────────┤
│  Cannot reset password          [HIGH] [OPEN]           │
│  Assignee: jane.smith    Created: Sep 11    Updated: …  │
├─────────────────────────────────────────────────────────┤
│  Description                                            │
│  User reports password reset email never arrives...     │
├─────────────────────────────────────────────────────────┤
│  Status actions:  [Start Progress]  [Cancel Ticket]     │
├─────────────────────────────────────────────────────────┤
│  [Edit Ticket]                                          │
├─────────────────────────────────────────────────────────┤
│  Comments (2)                                           │
│  ┌ support.agent · Sep 11, 11:30 ────────────────────┐ │
│  │ Verified SMTP logs; retrying delivery.              │ │
│  └─────────────────────────────────────────────────────┘ │
│  Author: [________]  Comment: [________________] [Add]  │
└─────────────────────────────────────────────────────────┘
```

### 5.2 Data loading

- On mount: `GET /api/tickets/{id}`
- Show **full-page loading spinner** while fetching
- On `404`: show *"Ticket not found"* with link back to dashboard (not a blank page)
- On `5xx`/network: error banner + Retry button

### 5.3 Read-only display

Show all fields from `TicketDetail`:

- `title`, `description`, `priority`, `status`, `assignee`, `createdAt`, `updatedAt`
- Comment list ordered by `createdAt` ascending

---

## 6. Edit Ticket (inline or modal on detail page)

### 6.1 Trigger

"Edit Ticket" button toggles edit mode (inline form or modal — implementation choice).

### 6.2 Editable fields

Same as create form: `title`, `description`, `priority`, `assignee`.

**Not editable here:** `status` (use status action buttons), `id`, timestamps.

### 6.3 Actions

- **Save** — `PATCH /api/tickets/{id}`
- **Cancel** — discard changes, return to read-only view

### 6.4 Success flow

1. Loading on Save button
2. On `200`: exit edit mode; refresh displayed fields from response
3. Clear previous errors

### 6.5 Error handling

Same pattern as create (400 field errors inline, 404 → not found page, 5xx banner). Preserve form values on validation failure.

---

## 7. Status Transition Controls

Status changes use `PATCH /api/tickets/{id}/status`. Buttons are shown **only for legal next states** per [state-machine.md](./state-machine.md).

### 7.1 Button mapping

| Current status | Buttons shown | API call |
|----------------|---------------|----------|
| `OPEN` | "Start Progress", "Cancel Ticket" | `status: IN_PROGRESS`, `status: CANCELLED` |
| `IN_PROGRESS` | "Mark Resolved", "Cancel Ticket" | `status: RESOLVED`, `status: CANCELLED` |
| `RESOLVED` | "Close Ticket" | `status: CLOSED` |
| `CLOSED` | *(none)* | — |
| `CANCELLED` | *(none)* | — |

### 7.2 Interaction

1. User clicks action button
2. Optional confirmation for destructive actions (`CANCELLED`): *"Cancel this ticket?"*
3. Button shows loading spinner; disable all status buttons during request
4. On `200`: update displayed status badge and action buttons
5. On `409`: show error banner with API `message` (e.g. *"Invalid status transition from OPEN to RESOLVED"*); status display unchanged
6. On `404`: redirect to not-found state

### 7.3 UX rules

- Never show buttons for illegal transitions (client mirrors state machine)
- Backend 409 is still possible (race conditions) — always handle gracefully
- Terminal states (`CLOSED`, `CANCELLED`): show informational label *"This ticket is closed"* or *"This ticket is cancelled"* instead of buttons

---

## 8. Comments Section

### 8.1 Display

- Section header with count: *"Comments (N)"*
- Each comment: author, formatted `createdAt`, body text
- Empty state: *"No comments yet."*

### 8.2 Add comment form

| Field | Required | Validation |
|-------|----------|------------|
| Author | yes | 1–120 chars |
| Body | yes | 1–2000 chars |

- **Add** button → `POST /api/tickets/{id}/comments`
- Loading: disable form + show spinner on button
- On `201`: append new comment to list (or refetch ticket); clear form fields
- On `400`: inline field errors
- On `404`: not-found handling

Comments are allowed on tickets in **any** status (including `CLOSED` and `CANCELLED`).

---

## 9. Loading States Summary

| Context | Loading indicator |
|---------|-------------------|
| Ticket list initial load | Table skeleton or centered spinner |
| Search/filter refetch | Inline spinner in table area or subtle overlay |
| Ticket detail load | Full-page spinner |
| Create / edit submit | Disabled submit + button text change |
| Status transition | Disabled status buttons + spinner on clicked button |
| Add comment | Disabled comment form |

**Rules:**

- Never show a blank white screen during loading
- Avoid duplicate concurrent requests for the same resource (cancel or ignore stale responses)

---

## 10. API Error Display

All errors parsed from `ErrorResponse` shape ([api-contract.md](./api-contract.md)).

### 10.1 Components

| Component | Use |
|-----------|-----|
| `ErrorBanner` | Top-of-page dismissible alert for general/network errors |
| `FieldError` | Inline text below form field from `fieldErrors[]` |

### 10.2 Mapping

| HTTP status | Display |
|-------------|---------|
| 400 + `fieldErrors` | Inline per field; banner only if `fieldErrors` empty |
| 404 | Dedicated not-found message (detail page) |
| 409 | Banner with `message` (status transitions) |
| 5xx | Banner: *"Something went wrong. Please try again."* |
| Network error | Banner: *"Unable to reach the server. Check your connection."* |

### 10.3 Behavior rules

- **Preserve form data** on 400 validation errors
- **Clear errors** on successful submit
- **Never show** raw stack traces, HTML, or unformatted JSON to the user
- Prefer API `message` over generic text when present and readable

---

## 11. Priority & Status Visual Treatment

### 11.1 Priority badges

| Priority | Suggested color |
|----------|-----------------|
| LOW | neutral/gray |
| MEDIUM | blue |
| HIGH | orange |
| CRITICAL | red |

### 11.2 Status badges

| Status | Suggested color |
|--------|-----------------|
| OPEN | blue |
| IN_PROGRESS | yellow/amber |
| RESOLVED | green |
| CLOSED | gray |
| CANCELLED | red/muted |

---

## 12. User Journeys

### 12.1 Create and progress a ticket

1. User clicks "New Ticket" → fills form → Create
2. Redirected to detail page (status `OPEN`)
3. Clicks "Start Progress" → status `IN_PROGRESS`
4. Adds comment with investigation notes
5. Clicks "Mark Resolved" → status `RESOLVED`
6. Clicks "Close Ticket" → status `CLOSED`; action buttons hidden

### 12.2 Search and filter

1. User lands on dashboard
2. Types "password" in search → sees matching tickets
3. Selects status "Open" → sees intersection of search + filter
4. Clicks a row → detail page

### 12.3 Cancel a ticket

1. User opens `OPEN` ticket
2. Clicks "Cancel Ticket" → confirms dialog
3. Status becomes `CANCELLED`; no further status buttons

---

## 13. Document History

| Version | Date | Changes |
|---------|------|---------|
| 1.0 | 2026-09-11 | Initial UI flow spec |
