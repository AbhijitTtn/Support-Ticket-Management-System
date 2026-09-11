# Support Ticket Management System

Full-stack support ticket application built with Spring Boot and React.

Specifications live in [`spec/`](./spec/) (requirements, architecture, API contract, and implementation plan).

## Prerequisites

| Tool | Version |
|------|---------|
| Java | 21 |
| Maven | 3.9+ (or use the Maven Wrapper in `backend/`) |
| Node.js | 18+ |
| npm | 9+ |

## Project structure

```
├── backend/     # Spring Boot REST API (Java 21)
├── frontend/    # React + Vite UI
└── spec/        # Requirements and design documents
```

## Java version

The backend requires **Java 21**. If your default `java` is older, set `JAVA_HOME` before running Maven:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64   # Linux example
export PATH="$JAVA_HOME/bin:$PATH"
```

## Run the backend

```bash
cd backend
./mvnw spring-boot:run
```

API base URL: `http://localhost:8080`

## Run the frontend

```bash
cd frontend
npm install
npm run dev
```

UI: `http://localhost:5173`

## Build for production

```bash
cd backend && ./mvnw clean package
cd frontend && npm install && npm run build
```

## Run backend tests

```bash
cd backend
./mvnw test
```

Frontend production assets are written to `frontend/dist/`.
