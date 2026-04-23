# FundPilot MVP

FundPilot is an MVP platform for NAV-based mutual fund analysis (Vietnam open-end funds), with periodic ingestion, scoring, and signal display.

## What is implemented in this vertical slice
- Import mock or CSV data into PostgreSQL (`fund`, `nav_history`, `benchmark_history`)
- Compute composite score (0-100) + signal class (STRONG_BUY/BUY/HOLD/AVOID)
- Persist signal with human-readable reasons
- REST APIs:
  - `GET /api/funds`
  - `GET /api/funds/{id}`
  - `GET /api/funds/{id}/nav-history`
  - `GET /api/funds/{id}/latest-signal`
  - `POST /api/import/mock`
  - `POST /api/import/csv`
- UI dashboard (fund list, fund detail, latest signal panel, NAV chart, mock import trigger)
- Unit tests for scoring and integration flow test (import -> signal -> API)

## Project structure
- `backend/`: Spring Boot 3 + Java 21 + Flyway + JPA
- `frontend/`: lightweight SPA UI consuming backend APIs
- `docker-compose.yml`: postgres + backend + frontend
- `docs/`: architecture/schema/API/scoring docs

## CSV import format
`fund_code,fund_name,date,nav,benchmark_code,benchmark_value`

Example:
```csv
VNFD01,Vietnam Growth Fund,2026-01-01,10.1234,VNINDEX,1025.9
```

## Run with Docker Compose
```bash
docker compose up --build
```

- Backend API: `http://localhost:8080`
- Frontend UI: `http://localhost:4200`
- Postgres: `localhost:5432` (`fundpilot` / `fundpilot`)

## Local backend run (without docker)
```bash
cd backend
mvn spring-boot:run
```

## Notes / assumptions
- Demo mock data auto-seeds at startup unless `app.seed-demo=false`.
- Benchmark component in score is used only when benchmark data exists.
- This phase intentionally does **not** execute buy orders.
- In this execution environment, outbound package registry access can be restricted (403), so build/test may need a network-enabled environment.
