# FundPilot MVP

FundPilot is an MVP platform for NAV-based mutual fund analysis for Vietnam open-end funds. It ingests NAV history, calculates explainable buy scores/signals, and shows results in a demo-friendly dashboard.

## Architecture summary
- **Backend**: Spring Boot (Java 21), Flyway migrations, JPA repositories, service layer for ingestion and scoring.
- **Database**: PostgreSQL with tables for funds, NAV history, benchmark history, and generated signals.
- **Frontend**: lightweight web dashboard (served by Nginx container) consuming backend REST APIs.
- **Infra**: Docker Compose orchestrates DB + backend + frontend.

> Phase 1 intentionally excludes real-money broker order execution.

## API conventions
All API responses follow a standard envelope:
```json
{
  "success": true,
  "data": { },
  "error": null
}
```
Error responses include `code`, `message`, optional `validation`, and request `path`.

## Main endpoints
- `POST /api/import/mock`
- `POST /api/import/csv`
- `GET /api/funds?page=0&size=20`
- `GET /api/funds/{id}`
- `GET /api/funds/{id}/nav-history`
- `GET /api/funds/{id}/latest-signal`

## Demo flow (recommended)
1. Start services.
2. Open UI.
3. Click **Import Mock Data** (or **Reinitialize Demo**) to load rich sample history.
4. Select a fund in the table to inspect latest signal and NAV chart.

Mock data now includes **5 demo funds** and generates recent signal history snapshots for demoing trend changes.

## Local run
### Option A: Docker Compose (one command)
```bash
cp .env.example .env
docker compose up --build
```

URLs:
- Backend: `http://localhost:8080`
- Frontend: `http://localhost:4200`
- PostgreSQL: `localhost:5432`

### Option B: Backend local + DB docker
```bash
docker compose up db -d
cd backend
mvn spring-boot:run
```

## Test instructions
```bash
cd backend
mvn test
```

## Scoring model (current MVP)
Configurable through env vars:
- thresholds: strong buy / buy / hold
- weights: 1Y return, MA position, percentile, drawdown, benchmark

Metrics used:
- 1Y return (if enough data)
- NAV vs MA60
- percentile in recent range
- max drawdown
- benchmark excess return (if benchmark data exists; neutral fallback otherwise)

Score is bounded to `[0,100]` and mapped to:
- `STRONG_BUY` / `BUY` / `HOLD` / `AVOID`

## Observability-lite
- Structured log messages for ingestion and scoring runs.
- Request correlation via `X-Request-Id` response/header propagation.

## Known limitations
- Frontend is currently a lightweight SPA (not a compiled Angular workspace) due package-registry restrictions in this execution environment.
- Notification channels are not wired yet.
- Strategy config persistence and backtesting-lite are planned but not yet implemented.

## Roadmap (next)
1. Move UI to full Angular workspace once package install access is available.
2. Add strategy configuration persistence and management APIs.
3. Add signal history page and basic backtesting-lite endpoint.
4. Add Telegram notification adapter with transition-based alerts.
5. Prepare broker/distributor integration ports (without real execution in phase 1).
