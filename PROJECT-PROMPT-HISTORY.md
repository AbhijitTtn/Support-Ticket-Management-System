# Support Ticket Management System — Prompt History

This document records how this project was created using **spec-driven development** in Cursor on **September 11, 2026**.

The main Cursor conversation: **"Support ticket system spec"** (`8fa1f33e-7501-4a41-8a5d-8502ad36f46b`).

---

## Overview

The project was built in a **17-step workflow**: write specifications first, then implement code in small, sequential slices. Each prompt instructed the agent to read the specs before coding and to implement only one layer at a time.

### Final structure

```
spec/           # Specifications written first (source of truth)
backend/        # Spring Boot REST API (Java 21)
frontend/       # React + Vite UI
```

### Git commits

| Commit    | Date       | Message              |
|-----------|------------|----------------------|
| `d589028` | 2026-09-11 | initial commit       |
| `073fe96` | 2026-09-11 | added BE and FE code |
| `51642d1` | 2026-09-11 | Working repo         |

---

## Phase 1 — Specifications (no code)

### Prompt 1 — Requirements (12:43 PM)

```
We are building a full-stack Support Ticket Management System using spec-driven development.

Do NOT implement application code yet.

Create a clear requirements specification in:
spec/requirements.md

Functional requirements:
1. Users can create tickets with title, description, priority and optional assignee.
2. Users can list tickets.
3. Users can view ticket details including comments.
4. Users can update title, description, priority and assignee.
5. Users can add comments.
6. Users can search by title or description keyword.
7. Users can filter by status.
8. Ticket data must be persisted.
9. Backend must validate input.
10. UI must display meaningful errors.

Status rules:
OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED
OPEN -> CANCELLED
IN_PROGRESS -> CANCELLED
Any other transition is invalid and must be rejected by the backend.
```

**Output:** `spec/requirements.md`

---

### Prompt 2 — Architecture (12:44 PM)

```
Read spec/requirements.md. Do not implement code yet.

Create:
spec/architecture.md

Define:
- React/Vite frontend -> REST API -> Spring service layer -> Spring Data JPA -> H2/PostgreSQL
- Controller responsibilities
- Service responsibilities
- Repository responsibilities
- Entity model responsibilities
- DTO responsibilities
- Global exception handling
- Transaction boundaries
- How the status state machine will be enforced
- H2 default configuration
- PostgreSQL profile using environment variables only
- CORS approach for local frontend/backend development

Make the design simple, maintainable and appropriate for a senior software engineer assignment.
```

**Output:** `spec/architecture.md`

---

### Prompt 3 — Data model, API contract, state machine, UI flow, test strategy (12:47 PM)

```
Read spec/requirements.md and spec/architecture.md.

Create these specification files. Do not implement application code.

1. spec/data-model.md
Ticket:
- id: UUID primary key
- title: required, 3-120 chars
- description: required, 5-5000 chars
- priority: LOW | MEDIUM | HIGH | CRITICAL
- status: OPEN | IN_PROGRESS | RESOLVED | CLOSED | CANCELLED
- assignee: optional, max 120 chars
- createdAt: instant
- updatedAt: instant

Comment:
- id: UUID primary key
- ticketId: foreign key
- author: required, max 120 chars
- body: required, 1-2000 chars
- createdAt: instant

2. spec/api-contract.md
Define:
POST /api/tickets
GET /api/tickets
GET /api/tickets/{id}
PATCH /api/tickets/{id}
PATCH /api/tickets/{id}/status
POST /api/tickets/{id}/comments

Include request/response JSON examples, query parameters for search/status filter,
validation error shape, 404/409 behavior.

3. spec/state-machine.md
4. spec/ui-flow.md
5. spec/test-strategy.md
```

**Output:** `spec/data-model.md`, `spec/api-contract.md`, `spec/state-machine.md`, `spec/ui-flow.md`, `spec/test-strategy.md`

---

### Prompt 4 — Implementation plan (12:58 PM)

```
Read every file under spec/.

Create spec/plan.md as an implementation plan.

Break the work into small sequential tasks:
1. Project scaffolding
2. Backend dependencies/configuration
3. Domain enums/entities
4. Repositories
5. DTOs and validation
6. Service layer
7. Explicit ticket state machine
8. REST controllers
9. Global error handling
10. Frontend scaffolding
11. API service
12. Ticket list/search/filter UI
13. Ticket detail/comments/edit/status UI
14. Backend tests
15. Frontend smoke verification
16. Documentation
17. Final review and fixes

For every task include:
- objective
- files to create/change
- dependencies
- acceptance criteria
- verification command where applicable

Do not implement anything yet.
```

**Output:** `spec/plan.md`

---

## Phase 2 — Scaffolding

### Prompt 5 — Project scaffolding (1:00 PM)

```
Read spec/requirements.md, spec/architecture.md and spec/plan.md.

Implement ONLY the project scaffolding:
- backend as a Maven Spring Boot project
- Java 21
- Spring Web
- Spring Data JPA
- Bean Validation
- H2
- PostgreSQL driver
- Spring Boot Test
- frontend as React + Vite
- npm scripts for development/build
- sensible .gitignore files
- README.md with prerequisites and basic run commands

Do not implement ticket business logic yet.

After implementation, show the exact files created and explain why each dependency exists.
```

**Output:** `backend/`, `frontend/`, `README.md`, `.gitignore` files

---

## Phase 3 — Backend

### Prompt 6 — Domain and persistence (3:41 PM)

```
Read all spec files before coding.

Implement the backend domain and persistence layer only.

Create:
- TicketStatus enum
- TicketPriority enum
- Ticket entity
- TicketComment entity
- TicketRepository
- TicketCommentRepository

Requirements:
- UUID primary keys
- createdAt/updatedAt timestamps
- Ticket -> comments relationship
- database-safe mappings
- appropriate indexes where useful
- no business logic in repositories
- preserve the exact data model from spec/data-model.md

Configure H2 as the default database and PostgreSQL through a postgres profile using environment variables.

Do not implement controllers yet.
```

---

### Prompt 7 — Service layer (3:44 PM)

```
Read all specifications and implement the application/service layer.

Create DTOs for:
- create ticket
- update ticket
- status transition
- create comment
- ticket response
- comment response
- API error

Add Bean Validation matching the specification.

Implement TicketService with:
- create ticket
- list/search/filter tickets
- get ticket with comments
- update editable fields
- add comment
- transition status

IMPORTANT:
Implement the status state machine explicitly in the service layer.
Allowed:
OPEN -> IN_PROGRESS
OPEN -> CANCELLED
IN_PROGRESS -> RESOLVED
IN_PROGRESS -> CANCELLED
RESOLVED -> CLOSED

Reject every other transition with a dedicated InvalidStatusTransitionException.
Do not allow clients to arbitrarily assign any enum value.

Use transactions appropriately.
Do not expose JPA entities directly from the service layer.
```

---

### Prompt 8 — REST API (3:47 PM)

```
Read spec/api-contract.md and the existing service implementation.

Implement:
- TicketController
- GlobalExceptionHandler
- ResourceNotFoundException
- InvalidStatusTransitionException handling

Endpoints must exactly match the API contract:
POST /api/tickets
GET /api/tickets
GET /api/tickets/{id}
PATCH /api/tickets/{id}
PATCH /api/tickets/{id}/status
POST /api/tickets/{id}/comments

Support:
- search query against title OR description
- status filter
- validation errors
- 404 for missing tickets
- 409 for invalid status transitions
- consistent JSON ApiError response

Do not return stack traces to clients.
Do not put business rules in the controller.
```

---

## Phase 4 — Frontend

### Prompt 9 — React UI (3:50 PM)

```
Read all spec files and the backend API contract.

Implement the React/Vite frontend.

Create:
- API service module
- TicketList component/page
- TicketForm component
- TicketDetail component
- appropriate reusable UI pieces
- CSS/styles

UI requirements:
1. Dashboard loads tickets.
2. Search by title/description keyword.
3. Filter by status.
4. Open a ticket to view details and comments.
5. Create a ticket.
6. Edit title, description, priority and assignee.
7. Add comments.
8. Show only sensible/legal next status transitions.
9. Backend remains authoritative for transition validation.
10. Show readable validation/API errors.
11. Include loading and empty states.
12. Keep the UI clean and usable without adding unnecessary libraries.

Use the API contract exactly; do not invent endpoint names or request shapes.
```

---

## Phase 5 — Testing and verification

### Prompt 10 — Backend tests (3:54 PM)

```
Read the requirements, API contract, state machine and test strategy.

Add/complete backend tests covering:
- application context startup
- ticket creation
- required-field validation
- title length boundaries
- description length boundaries
- update behavior
- comment creation
- missing ticket handling
- search by title
- search by description
- status filtering
- every allowed status transition
- representative invalid transitions
- CLOSED -> OPEN must fail
- CANCELLED -> OPEN must fail
- invalid transition must produce the expected exception/409 behavior
- persistence using H2

Tests must verify behavior, not merely line coverage.

Do not weaken production validation to make tests pass.
```

---

### Prompt 11 — Frontend smoke test (3:58 PM)

```
Review the frontend against spec/ui-flow.md and spec/api-contract.md.

Add lightweight frontend tests if the existing setup supports them without unnecessary complexity. Otherwise document the manual smoke test.

Perform this manual flow:
1. Start backend.
2. Start frontend.
3. Create a ticket.
4. Confirm it appears in the list.
5. Search for it.
6. Filter by OPEN.
7. Open detail.
8. Edit title/description/priority/assignee.
9. Add a comment.
10. Move OPEN -> IN_PROGRESS.
11. Move IN_PROGRESS -> RESOLVED.
12. Move RESOLVED -> CLOSED.
13. Verify illegal transitions are not offered by the UI.
14. Verify the backend rejects an illegal transition with HTTP 409.
15. Verify API/validation errors are readable.
```

**Output:** `spec/smoke-results.md`

---

## Development pattern used

This project followed a repeatable **spec-driven development** workflow:

| Step | What | Why |
|------|------|-----|
| 1 | Write requirements | Define what to build before writing code |
| 2 | Write architecture | Decide layers, tech choices, and boundaries |
| 3 | Write detailed specs | Data model, API contract, state machine, UI flows, tests |
| 4 | Create implementation plan | Break work into 17 small, reviewable tasks |
| 5 | Scaffold only | Set up project structure without business logic |
| 6 | Build backend layer by layer | Entities → repos → services → controllers |
| 7 | Build frontend | API client → list → detail → forms |
| 8 | Test and verify | Automated backend tests + manual smoke checklist |

### Key principles in every prompt

- **"Read spec/ before coding"** — specs are the source of truth
- **"Do NOT implement application code yet"** — specs come first
- **"Implement ONLY …"** — one slice per prompt, no scope creep
- **"Do not invent endpoint names"** — follow the API contract exactly

---

## Specification files

| File | Purpose |
|------|---------|
| `spec/requirements.md` | Functional and non-functional requirements |
| `spec/architecture.md` | Layered design, config, CORS |
| `spec/data-model.md` | Entities, UUID keys, field constraints |
| `spec/api-contract.md` | REST endpoints, JSON shapes, status codes |
| `spec/state-machine.md` | Status transitions, HTTP 409 rules |
| `spec/ui-flow.md` | Routes, screens, UX behavior |
| `spec/test-strategy.md` | Test pyramid, cases, smoke checklist |
| `spec/plan.md` | 17-task implementation plan |
| `spec/smoke-results.md` | Manual frontend smoke test results |

---

## Tech stack

| Layer    | Technology |
|----------|------------|
| Backend  | Java 21, Spring Boot, Spring Data JPA, Bean Validation |
| Database | H2 (default), PostgreSQL (profile via env vars) |
| Frontend | React, Vite, TypeScript |
| API      | REST/JSON |

---

## How to run

```bash
# Backend
cd backend && ./mvnw spring-boot:run

# Frontend (separate terminal)
cd frontend && npm install && npm run dev
```

- API: `http://localhost:8080`
- UI: `http://localhost:5173`

See [README.md](./README.md) for full setup instructions.
