# Implementation Plan (Initial Sequence)

## Step 1 - Foundation & Architecture Baseline
- Initialize Spring Boot backend project (Java 21, Web, Validation, Data JPA, PostgreSQL, Flyway, Actuator, Test).
- Create module package skeleton per architecture document.
- Add Flyway baseline migration with core schema.
- Add Docker Compose with PostgreSQL and backend service.
- Deliverable: backend boots and health endpoint responds.

## Step 2 - Ingestion Vertical Foundation
- Define provider port and domain DTOs for fund/NAV/benchmark snapshots.
- Implement CSV ingestion adapter.
- Implement mock provider adapter.
- Create ingestion use case and scheduler trigger endpoint/manual trigger endpoint.
- Persist fund/nav/benchmark records.
- Deliverable: import endpoint loads sample CSV into DB.

## Step 3 - Analysis Engine MVP
- Implement metric calculators:
  - 1Y/3Y return (3Y optional)
  - max drawdown
  - volatility
  - MA(20/60/120) delta
  - percentile in 1Y range
  - benchmark outperformance
- Implement configurable weighting from strategy_config.
- Persist signal rows including reasons + metrics JSON.
- Deliverable: API endpoint runs analysis for all active funds and stores latest signals.

## Step 4 - Signal API + Minimal Dashboard Slice
- Scaffold Angular app with feature modules.
- Build pages/components:
  - Fund list (code, name, latest NAV, latest score, label)
  - Fund detail (basic metadata + latest explanations)
- Implement backend endpoints for above views.
- Deliverable: first end-to-end slice visible in UI.

## Step 5 - Alerting MVP
- Add Telegram notifier adapter.
- Trigger alerts on label transition, threshold crossing, and BUY/STRONG_BUY entries.
- Persist notification logs.
- Deliverable: test notification path with mock Telegram config.

## Step 6 - Backtesting-lite
- Implement simulation endpoint:
  - signal-driven entry strategy
  - monthly DCA baseline
- Return summary metrics: total invested, final value, CAGR proxy, max drawdown proxy.
- Deliverable: basic comparison in API + UI table.

## Step 7 - Hardening
- Unit tests for scoring logic and classification boundaries.
- Integration tests for ingestion + signal endpoints.
- Seed demo data and README runbook.
- Deliverable: reproducible local environment via Docker Compose.
