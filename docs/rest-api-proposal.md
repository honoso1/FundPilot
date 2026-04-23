# REST API Proposal (Phase 1)

## Ingestion
- `POST /api/v1/ingestion/import/csv` - upload CSV and import records.
- `POST /api/v1/ingestion/import/mock` - load mock provider dataset.
- `POST /api/v1/ingestion/run` - run configured provider ingestion.

## Funds & NAV
- `GET /api/v1/funds` - list funds with latest score summary.
- `GET /api/v1/funds/{fundCode}` - fund detail.
- `GET /api/v1/funds/{fundCode}/nav?from=YYYY-MM-DD&to=YYYY-MM-DD` - NAV series.
- `GET /api/v1/funds/{fundCode}/signals` - signal history.

## Analysis
- `POST /api/v1/analysis/run` - run analysis for all active funds.
- `POST /api/v1/analysis/run/{fundCode}` - run analysis for one fund.

## Strategy
- `GET /api/v1/strategy-configs` - list strategy configs.
- `POST /api/v1/strategy-configs` - create strategy config.
- `PUT /api/v1/strategy-configs/{id}` - update strategy config.
- `POST /api/v1/strategy-configs/{id}/activate` - mark as default active config.

## Watchlist
- `GET /api/v1/watchlist?ownerKey=...`
- `POST /api/v1/watchlist`
- `DELETE /api/v1/watchlist/{id}`

## Notifications
- `GET /api/v1/notifications?status=SENT&channel=TELEGRAM`

## Backtesting-lite
- `POST /api/v1/backtests/signal-vs-dca`
