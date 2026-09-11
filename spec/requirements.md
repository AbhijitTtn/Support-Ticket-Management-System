# Support Ticket Management System — Requirements Specification

## 1. Overview

This document defines the functional and non-functional requirements for a full-stack Support Ticket Management System. The system allows users to create, view, update, search, and filter support tickets, add comments, and manage ticket lifecycle through a defined status workflow.

**Technology stack (target):**

| Layer    | Technology                          |
|----------|-------------------------------------|
| Backend  | Java 21, Spring Boot, REST/JSON API |
| Database | H2 (default local), PostgreSQL (profile) |
| Frontend | React                               |

**Out of scope for this specification:** authentication/authorization, multi-tenancy, file attachments, email notifications, and role-based access control unless added in a future revision.

---

## 2. Domain Model (Conceptual)

### 2.1 Ticket

A ticket represents a support request or issue.

| Field       | Type        | Required | Notes                                      |
|-------------|-------------|----------|--------------------------------------------|
| id          | identifier  | auto     | System-generated, unique                   |
| title       | string      | yes      | Short summary of the issue                 |
| description | string      | yes      | Detailed explanation                       |
| priority    | enum        | yes      | `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`        |
| status      | enum        | auto     | Defaults to `OPEN`; see status rules       |
| assignee    | string      | no       | Optional identifier or name of assignee    |
| createdAt   | timestamp   | auto     | Set on creation                            |
| updatedAt   | timestamp   | auto     | Set on creation and each update            |

### 2.2 Comment

A comment is a user-authored note attached to a ticket.

| Field     | Type       | Required | Notes                          |
|-----------|------------|----------|--------------------------------|
| id        | identifier | auto     | System-generated, unique       |
| ticketId  | identifier | yes      | Parent ticket reference        |
| body      | string     | yes      | Comment text                     |
| author    | string     | yes      | Author identifier or display name |
| createdAt | timestamp  | auto     | Set on creation                |

### 2.3 Status Values

`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`

---

## 3. Functional Requirements

### FR-1: Create Ticket

Users can create a ticket by providing **title**, **description**, **priority**, and an optional **assignee**.

**Acceptance criteria:**

- [ ] **AC-1.1** Given valid `title`, `description`, and `priority`, when a user submits a create request, then the system creates a ticket with status `OPEN` and returns the created ticket including its generated `id` and timestamps.
- [ ] **AC-1.2** Given a create request that omits `assignee`, when the ticket is created, then `assignee` is stored as null/empty and the ticket is otherwise valid.
- [ ] **AC-1.3** Given a create request that includes a non-empty `assignee`, when the ticket is created, then the assignee value is persisted on the ticket.
- [ ] **AC-1.4** Given a create request with a missing or blank `title`, when submitted, then the backend rejects the request with a validation error and no ticket is created.
- [ ] **AC-1.5** Given a create request with a missing or blank `description`, when submitted, then the backend rejects the request with a validation error and no ticket is created.
- [ ] **AC-1.6** Given a create request with a missing, blank, or invalid `priority`, when submitted, then the backend rejects the request with a validation error and no ticket is created.
- [ ] **AC-1.7** Given a successful create response, when the UI receives it, then the user sees confirmation and the new ticket details (or is navigated to the ticket detail view).

---

### FR-2: List Tickets

Users can retrieve a list of tickets.

**Acceptance criteria:**

- [ ] **AC-2.1** Given one or more tickets exist, when a user requests the ticket list, then the system returns all tickets with at least `id`, `title`, `priority`, `status`, `assignee`, `createdAt`, and `updatedAt`.
- [ ] **AC-2.2** Given no tickets exist, when a user requests the ticket list, then the system returns an empty list (not an error).
- [ ] **AC-2.3** Given tickets exist, when the list is displayed in the UI, then each ticket shows enough summary information for the user to identify and select a ticket.
- [ ] **AC-2.4** Given the list endpoint is called, when the response is returned, then it uses JSON and follows REST conventions.

---

### FR-3: View Ticket Details

Users can view a single ticket's full details, including its comments.

**Acceptance criteria:**

- [ ] **AC-3.1** Given a ticket with id `X` exists, when a user requests ticket `X`, then the system returns the full ticket record (`title`, `description`, `priority`, `status`, `assignee`, timestamps).
- [ ] **AC-3.2** Given a ticket with one or more comments, when a user requests ticket details, then all comments for that ticket are included, each with `id`, `body`, `author`, and `createdAt`.
- [ ] **AC-3.3** Given a ticket with no comments, when a user requests ticket details, then the comments collection is empty.
- [ ] **AC-3.4** Given no ticket with id `X` exists, when a user requests ticket `X`, then the backend returns a not-found error (HTTP 404) with a meaningful message.
- [ ] **AC-3.5** Given ticket details are loaded in the UI, when displayed, then title, description, priority, status, assignee, timestamps, and comments are all visible.

---

### FR-4: Update Ticket

Users can update a ticket's **title**, **description**, **priority**, and **assignee**.

**Acceptance criteria:**

- [ ] **AC-4.1** Given an existing ticket, when a user submits valid updates to `title`, `description`, `priority`, and/or `assignee`, then the system persists the changes and returns the updated ticket.
- [ ] **AC-4.2** Given an update request, when `assignee` is cleared (set to null/empty), then the assignee is removed from the ticket.
- [ ] **AC-4.3** Given an update request with a blank `title` or `description`, or an invalid `priority`, when submitted, then the backend rejects the request with a validation error and the ticket is unchanged.
- [ ] **AC-4.4** Given no ticket with the requested id exists, when an update is submitted, then the backend returns a not-found error (HTTP 404).
- [ ] **AC-4.5** Given a successful update, when the UI receives the response, then the displayed ticket reflects the new values.
- [ ] **AC-4.6** Given an update request, when processed successfully, then `updatedAt` is refreshed to the current time.

---

### FR-5: Add Comment

Users can add a comment to an existing ticket.

**Acceptance criteria:**

- [ ] **AC-5.1** Given an existing ticket, when a user submits a comment with a non-empty `body` and `author`, then the comment is persisted, linked to the ticket, and returned with its generated `id` and `createdAt`.
- [ ] **AC-5.2** Given a comment is added, when ticket details are subsequently requested, then the new comment appears in the comments list.
- [ ] **AC-5.3** Given a comment request with a missing or blank `body`, when submitted, then the backend rejects the request with a validation error.
- [ ] **AC-5.4** Given a comment request with a missing or blank `author`, when submitted, then the backend rejects the request with a validation error.
- [ ] **AC-5.5** Given no ticket with the requested id exists, when a comment is submitted, then the backend returns a not-found error (HTTP 404).
- [ ] **AC-5.6** Given a successful comment creation in the UI, when the response is received, then the new comment is shown on the ticket detail view without requiring a full page reload (or the detail view is refreshed to include it).

---

### FR-6: Search Tickets

Users can search tickets by keyword in **title** or **description**.

**Acceptance criteria:**

- [ ] **AC-6.1** Given tickets exist whose `title` contains the search keyword (case-insensitive), when a user searches, then those tickets are returned.
- [ ] **AC-6.2** Given tickets exist whose `description` contains the search keyword (case-insensitive), when a user searches, then those tickets are returned.
- [ ] **AC-6.3** Given a ticket matches the keyword in either title or description, when searched, then it appears exactly once in the results (no duplicates).
- [ ] **AC-6.4** Given no tickets match the keyword, when a user searches, then the system returns an empty list (not an error).
- [ ] **AC-6.5** Given an empty or whitespace-only search term, when submitted, then the system either returns all tickets or returns a validation error — behavior must be documented and consistent between API and UI.
- [ ] **AC-6.6** Given search results are returned, when displayed in the UI, then matching tickets are shown in the same summary format as the ticket list.

---

### FR-7: Filter Tickets by Status

Users can filter tickets by **status**.

**Acceptance criteria:**

- [ ] **AC-7.1** Given tickets with various statuses exist, when a user filters by a specific status (e.g. `OPEN`), then only tickets with that status are returned.
- [ ] **AC-7.2** Given a filter for each valid status value (`OPEN`, `IN_PROGRESS`, `RESOLVED`, `CLOSED`, `CANCELLED`), when applied, then results contain only tickets in that status.
- [ ] **AC-7.3** Given an invalid status value in a filter request, when submitted, then the backend rejects the request with a validation error.
- [ ] **AC-7.4** Given no tickets match the selected status, when filtered, then the system returns an empty list.
- [ ] **AC-7.5** Given search and status filter are both provided, when the request is processed, then results satisfy **both** criteria (logical AND).
- [ ] **AC-7.6** Given a status filter is active in the UI, when results are displayed, then the user can see which status filter is applied and can clear or change it.

---

### FR-8: Data Persistence

Ticket and comment data must survive application restarts.

**Acceptance criteria:**

- [ ] **AC-8.1** Given a ticket is created, when the application is stopped and restarted, then the ticket is still retrievable with the same data.
- [ ] **AC-8.2** Given a ticket is updated, when the application is restarted, then the updated values are persisted.
- [ ] **AC-8.3** Given a comment is added, when the application is restarted, then the comment is still associated with the correct ticket.
- [ ] **AC-8.4** Given the default (local) profile is active, when the application runs, then H2 is used as the database without requiring external infrastructure.
- [ ] **AC-8.5** Given the PostgreSQL profile is active and correctly configured via environment variables or external config (not committed secrets), when the application runs, then PostgreSQL is used and data persists across restarts.

---

### FR-9: Backend Input Validation

The backend must validate all incoming input before persisting or processing.

**Acceptance criteria:**

- [ ] **AC-9.1** Given any API request with invalid or missing required fields, when processed, then the backend responds with HTTP 400 and a structured error body describing the validation failures.
- [ ] **AC-9.2** Given an invalid `priority` or `status` enum value, when submitted, then the backend rejects the request with a clear validation message.
- [ ] **AC-9.3** Given field length limits are defined (e.g. max title length), when input exceeds a limit, then the backend rejects the request with a validation error.
- [ ] **AC-9.4** Given a malformed JSON request body, when submitted, then the backend responds with HTTP 400 and an appropriate error message.
- [ ] **AC-9.5** Given validation fails, when the response is returned, then no partial or corrupt data is written to the database.

---

### FR-10: UI Error Display

The UI must display meaningful errors to the user.

**Acceptance criteria:**

- [ ] **AC-10.1** Given the backend returns a validation error (HTTP 400), when the UI receives it, then field-level or summary error messages are shown in user-friendly language.
- [ ] **AC-10.2** Given the backend returns a not-found error (HTTP 404), when the UI receives it, then the user sees a clear message that the ticket was not found.
- [ ] **AC-10.3** Given the backend is unreachable or returns a server error (HTTP 5xx), when the UI detects the failure, then the user sees a generic failure message (not a raw stack trace or empty screen).
- [ ] **AC-10.4** Given a form submission fails validation, when errors are displayed, then the user's entered data is preserved in the form where possible.
- [ ] **AC-10.5** Given an error is shown, when the user corrects the input and resubmits, then the previous error message is cleared on success.

---

## 4. Status Transition Rules

Ticket status changes follow a strict state machine. Status may be updated as part of a dedicated status-change operation or an update endpoint — the exact API shape is an implementation decision, but the rules below are mandatory.

### 4.1 Allowed Transitions

| From          | To            |
|---------------|---------------|
| `OPEN`        | `IN_PROGRESS` |
| `IN_PROGRESS` | `RESOLVED`    |
| `RESOLVED`    | `CLOSED`      |
| `OPEN`        | `CANCELLED`   |
| `IN_PROGRESS` | `CANCELLED`   |

### 4.2 Disallowed Transitions

Any transition not listed in §4.1 is **invalid**. Examples include but are not limited to:

- `OPEN` → `RESOLVED`, `OPEN` → `CLOSED`
- `IN_PROGRESS` → `OPEN`, `IN_PROGRESS` → `CLOSED`
- `RESOLVED` → `OPEN`, `RESOLVED` → `IN_PROGRESS`, `RESOLVED` → `CANCELLED`
- `CLOSED` → any other status
- `CANCELLED` → any other status

### 4.3 Acceptance Criteria — Status Transitions

- [ ] **AC-ST-1** Given a ticket in status `OPEN`, when a transition to `IN_PROGRESS` is requested, then the status is updated successfully.
- [ ] **AC-ST-2** Given a ticket in status `IN_PROGRESS`, when a transition to `RESOLVED` is requested, then the status is updated successfully.
- [ ] **AC-ST-3** Given a ticket in status `RESOLVED`, when a transition to `CLOSED` is requested, then the status is updated successfully.
- [ ] **AC-ST-4** Given a ticket in status `OPEN`, when a transition to `CANCELLED` is requested, then the status is updated successfully.
- [ ] **AC-ST-5** Given a ticket in status `IN_PROGRESS`, when a transition to `CANCELLED` is requested, then the status is updated successfully.
- [ ] **AC-ST-6** Given a ticket in any status, when a transition not listed in §4.1 is requested, then the backend rejects the request (HTTP 400 or 409) with a message indicating the transition is not allowed, and the status remains unchanged.
- [ ] **AC-ST-7** Given a ticket in status `CLOSED` or `CANCELLED`, when any status change is requested, then the backend rejects the request.
- [ ] **AC-ST-8** Given a new ticket is created, when no status is supplied, then the initial status is `OPEN`.
- [ ] **AC-ST-9** Given an invalid status transition is attempted in the UI, when the backend rejects it, then the UI displays the error and the displayed status reflects the unchanged value.

---

## 5. Non-Functional Requirements

### NFR-1: Java 21

The backend must compile and run on Java 21.

**Acceptance criteria:**

- [ ] **AC-NFR-1.1** Given the project build configuration, when built, then the target Java version is 21.
- [ ] **AC-NFR-1.2** Given a Java 21 runtime, when the backend application starts, then it runs without version-compatibility errors.

---

### NFR-2: Spring Boot

The backend must be implemented using Spring Boot.

**Acceptance criteria:**

- [ ] **AC-NFR-2.1** Given the backend project, when inspected, then it uses Spring Boot as the application framework (dependencies, auto-configuration, executable JAR).
- [ ] **AC-NFR-2.2** Given the application is started, when health/readiness is checked, then the Spring Boot application context loads successfully.

---

### NFR-3: REST/JSON API

The backend must expose a RESTful API using JSON request and response bodies.

**Acceptance criteria:**

- [ ] **AC-NFR-3.1** Given any successful API response with a body, when inspected, then the `Content-Type` is `application/json`.
- [ ] **AC-NFR-3.2** Given API endpoints for tickets and comments, when reviewed, then they follow REST conventions (appropriate HTTP methods and status codes).
- [ ] **AC-NFR-3.3** Given a client sends `Content-Type: application/json`, when the backend parses the body, then it deserializes correctly.

---

### NFR-4: H2 as Default Local Database

H2 must be the default database for local development without extra setup.

**Acceptance criteria:**

- [ ] **AC-NFR-4.1** Given no database profile is explicitly set, when the application starts, then it connects to H2 and schema/tables are initialized.
- [ ] **AC-NFR-4.2** Given the default profile, when a developer clones the repository and starts the app, then no external database installation is required.

---

### NFR-5: PostgreSQL Profile

PostgreSQL must be supported through a separate Spring profile.

**Acceptance criteria:**

- [ ] **AC-NFR-5.1** Given the PostgreSQL profile is activated, when the application starts with valid external configuration, then it connects to PostgreSQL instead of H2.
- [ ] **AC-NFR-5.2** Given the PostgreSQL profile, when connection settings are provided via environment variables or local untracked config, then no database credentials appear in committed source files.
- [ ] **AC-NFR-5.3** Given the same API operations against either H2 or PostgreSQL, when executed, then functional behavior is equivalent.

---

### NFR-6: React Frontend

The user interface must be built with React.

**Acceptance criteria:**

- [ ] **AC-NFR-6.1** Given the frontend project, when inspected, then it is a React application.
- [ ] **AC-NFR-6.2** Given the frontend is running, when a user interacts with ticket list, detail, create, update, search, filter, and comment flows, then all FR acceptance criteria achievable via UI are satisfied.

---

### NFR-7: No Committed Secrets

Secrets must not be stored in the repository.

**Acceptance criteria:**

- [ ] **AC-NFR-7.1** Given the repository contents, when searched, then no passwords, API keys, tokens, or private keys are committed.
- [ ] **AC-NFR-7.2** Given configuration requires sensitive values (e.g. PostgreSQL password), when documented, then instructions reference environment variables or an untracked `.env` file listed in `.gitignore`.
- [ ] **AC-NFR-7.3** Given example configuration files are committed, when reviewed, then they contain only placeholders, not real credentials.

---

## 6. API Surface (Indicative)

The following endpoints are expected; exact paths and payloads will be defined in a separate API specification or implementation.

| Method | Endpoint (indicative)              | Purpose                          |
|--------|------------------------------------|----------------------------------|
| POST   | `/api/tickets`                     | Create ticket                    |
| GET    | `/api/tickets`                     | List tickets (search/filter)     |
| GET    | `/api/tickets/{id}`                | Get ticket with comments         |
| PUT/PATCH | `/api/tickets/{id}`             | Update ticket fields             |
| PATCH  | `/api/tickets/{id}/status`         | Transition ticket status         |
| POST   | `/api/tickets/{id}/comments`       | Add comment                      |

Query parameters for list: `q` (search keyword), `status` (filter).

---

## 7. Glossary

| Term       | Definition                                                |
|------------|-----------------------------------------------------------|
| Ticket     | A support request tracked through its lifecycle           |
| Comment    | A text note attached to a ticket                          |
| Status     | Current lifecycle state of a ticket                       |
| Priority   | Urgency level: LOW, MEDIUM, HIGH, CRITICAL                |
| Assignee   | Optional person responsible for the ticket                |
| Profile    | Spring Boot configuration set (default/H2 vs PostgreSQL) |

---

## 8. Document History

| Version | Date       | Author | Changes              |
|---------|------------|--------|----------------------|
| 1.0     | 2026-09-11 | —      | Initial requirements |
