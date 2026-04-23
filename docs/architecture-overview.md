# FundPilot MVP - Architecture Overview (Phase 1)

## Assumptions
1. Primary users are long-term retail investors in Vietnam evaluating open-end mutual funds by daily/periodic NAV updates.
2. Data update frequency is periodic (daily or end-of-day), not tick-level real-time.
3. For phase 1, data providers can be CSV or mock adapters; production providers are added later through provider interfaces.
4. A single-tenant deployment model is acceptable for MVP (one organization/user group), with future extension to multi-user.
5. Alerts are outbound only (Telegram first), no conversational bot workflows needed in phase 1.
6. No real-money order placement in phase 1. Broker integration points should exist only as extension interfaces.

## High-level Design Choices
- **Modular monolith** with strict package boundaries for fast MVP delivery and maintainability.
- **Hexagonal / clean architecture style**:
  - Domain + application services isolated from infrastructure details.
  - Inbound adapters: REST controllers, schedulers.
  - Outbound adapters: PostgreSQL repositories, provider connectors, notifier connectors.
- **Event-friendly design**: analysis runs produce signal events so alerting and future integrations can subscribe.
- **Versioned strategy config** for reproducible scoring and backtesting-lite.

## Runtime Components
1. **Fund Data Ingestion Module**
   - Reads CSV files or mock provider payloads.
   - Validates and normalizes NAV/benchmark records.
   - Upserts funds and writes time-series histories.
2. **Analysis Engine Module**
   - Computes metrics (returns, drawdown, volatility, MA relation, percentile, benchmark spread).
   - Produces composite score + classification + explainable reasons.
   - Persists signals and emits domain events.
3. **Signal & Alert Module**
   - Detects transitions (e.g., HOLD -> BUY, threshold crossing).
   - Sends Telegram notifications.
   - Persists notification logs.
4. **Dashboard API Module**
   - Exposes fund list/detail, signal history, watchlist, strategy configuration, notifications.
5. **Backtesting-lite Module**
   - Simulates BUY/STRONG_BUY trigger investing vs monthly DCA baseline.
   - Returns summary-level metrics.
6. **Angular Web App**
   - Dashboard pages for ranking, details, charts, strategy config, watchlist, and notification history.

## System Context (Phase 1)
- Frontend (Angular) -> Backend REST API (Spring Boot)
- Backend -> PostgreSQL
- Backend Scheduler -> Provider adapters
- Backend -> Telegram Bot API

## Deployment (Local MVP)
- Docker Compose services:
  - `fundpilot-api` (Spring Boot)
  - `fundpilot-web` (Angular dev/prod container)
  - `fundpilot-db` (PostgreSQL)

## Extension Path for Phase 2 (Order Placement)
Prepare a dedicated application port:
- `OrderExecutionPort.placeOrder(OrderIntent)`
- Keep disabled/no-op adapter in phase 1.
- Add broker/distributor adapters in phase 2 without changing domain scoring logic.
