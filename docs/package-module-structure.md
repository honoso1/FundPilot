# Package & Module Structure Proposal

## Backend (Java 21 + Spring Boot)

```text
com.fundpilot
├── FundPilotApplication
├── common
│   ├── exception
│   ├── validation
│   ├── time
│   └── config
├── domain
│   ├── fund
│   │   ├── model
│   │   ├── service
│   │   └── port
│   ├── nav
│   │   ├── model
│   │   ├── service
│   │   └── port
│   ├── benchmark
│   ├── signal
│   ├── strategy
│   ├── watchlist
│   ├── notification
│   └── backtest
├── application
│   ├── ingestion
│   │   ├── command
│   │   ├── usecase
│   │   └── dto
│   ├── analysis
│   ├── dashboard
│   ├── alert
│   ├── strategy
│   └── backtest
├── infrastructure
│   ├── persistence
│   │   ├── entity
│   │   ├── repository
│   │   └── mapper
│   ├── provider
│   │   ├── csv
│   │   ├── mock
│   │   └── example
│   ├── notification
│   │   ├── telegram
│   │   └── email
│   ├── scheduler
│   └── config
└── interfaces
    ├── rest
    │   ├── fund
    │   ├── signal
    │   ├── strategy
    │   ├── watchlist
    │   ├── notification
    │   └── backtest
    └── dto
```

## Frontend (Angular)

```text
src/app
├── core
│   ├── api
│   ├── interceptors
│   ├── models
│   └── services
├── features
│   ├── funds
│   │   ├── fund-list
│   │   ├── fund-detail
│   │   └── nav-chart
│   ├── signals
│   │   └── signal-history
│   ├── strategy
│   │   └── strategy-config
│   ├── watchlist
│   ├── notifications
│   └── backtest
├── shared
│   ├── components
│   ├── pipes
│   └── utils
└── app-routing.module.ts
```

## Boundary Rules
1. Domain layer must not depend on Spring, JPA, or HTTP classes.
2. Application layer orchestrates use cases and transaction boundaries.
3. Infrastructure depends inward (implements ports).
4. Interfaces (controllers) depend on application DTOs/services only.
