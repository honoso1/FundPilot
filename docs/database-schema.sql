-- FundPilot MVP PostgreSQL schema (Phase 1)

CREATE TABLE fund (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  management_company VARCHAR(255),
  category VARCHAR(100),
  inception_date DATE,
  currency VARCHAR(10) NOT NULL DEFAULT 'VND',
  benchmark_code VARCHAR(50),
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE nav_history (
  id UUID PRIMARY KEY,
  fund_id UUID NOT NULL REFERENCES fund(id) ON DELETE CASCADE,
  nav_date DATE NOT NULL,
  nav_value NUMERIC(18,6) NOT NULL,
  source VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (fund_id, nav_date)
);
CREATE INDEX idx_nav_history_fund_date ON nav_history(fund_id, nav_date DESC);

CREATE TABLE benchmark_history (
  id UUID PRIMARY KEY,
  benchmark_code VARCHAR(50) NOT NULL,
  benchmark_date DATE NOT NULL,
  benchmark_value NUMERIC(18,6) NOT NULL,
  source VARCHAR(50) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (benchmark_code, benchmark_date)
);
CREATE INDEX idx_benchmark_code_date ON benchmark_history(benchmark_code, benchmark_date DESC);

CREATE TYPE signal_label AS ENUM ('STRONG_BUY', 'BUY', 'HOLD', 'AVOID');

CREATE TABLE strategy_config (
  id UUID PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  version INT NOT NULL,
  is_default BOOLEAN NOT NULL DEFAULT FALSE,
  min_data_points INT NOT NULL DEFAULT 120,
  weight_return_1y NUMERIC(5,2) NOT NULL,
  weight_return_3y NUMERIC(5,2) NOT NULL,
  weight_drawdown NUMERIC(5,2) NOT NULL,
  weight_volatility NUMERIC(5,2) NOT NULL,
  weight_ma_position NUMERIC(5,2) NOT NULL,
  weight_percentile NUMERIC(5,2) NOT NULL,
  weight_benchmark NUMERIC(5,2) NOT NULL,
  strong_buy_threshold INT NOT NULL,
  buy_threshold INT NOT NULL,
  hold_threshold INT NOT NULL,
  config_json JSONB NOT NULL,
  effective_from DATE NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(name, version)
);

CREATE TABLE signal (
  id UUID PRIMARY KEY,
  fund_id UUID NOT NULL REFERENCES fund(id) ON DELETE CASCADE,
  strategy_config_id UUID NOT NULL REFERENCES strategy_config(id),
  signal_date DATE NOT NULL,
  score NUMERIC(5,2) NOT NULL,
  label signal_label NOT NULL,
  reasons JSONB NOT NULL,
  metrics JSONB NOT NULL,
  previous_label signal_label,
  previous_score NUMERIC(5,2),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (fund_id, strategy_config_id, signal_date)
);
CREATE INDEX idx_signal_fund_date ON signal(fund_id, signal_date DESC);
CREATE INDEX idx_signal_label_date ON signal(label, signal_date DESC);

CREATE TABLE portfolio_watchlist (
  id UUID PRIMARY KEY,
  owner_key VARCHAR(100) NOT NULL,
  fund_id UUID NOT NULL REFERENCES fund(id) ON DELETE CASCADE,
  target_score INT,
  notify_on_buy BOOLEAN NOT NULL DEFAULT TRUE,
  notify_on_strong_buy BOOLEAN NOT NULL DEFAULT TRUE,
  notify_on_score_cross BOOLEAN NOT NULL DEFAULT TRUE,
  is_active BOOLEAN NOT NULL DEFAULT TRUE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE(owner_key, fund_id)
);

CREATE TYPE notification_channel AS ENUM ('TELEGRAM', 'EMAIL');
CREATE TYPE notification_status AS ENUM ('SENT', 'FAILED', 'SKIPPED');

CREATE TABLE notification_log (
  id UUID PRIMARY KEY,
  fund_id UUID REFERENCES fund(id) ON DELETE SET NULL,
  signal_id UUID REFERENCES signal(id) ON DELETE SET NULL,
  channel notification_channel NOT NULL,
  recipient VARCHAR(200) NOT NULL,
  status notification_status NOT NULL,
  message TEXT NOT NULL,
  error_detail TEXT,
  sent_at TIMESTAMPTZ,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_notification_created_at ON notification_log(created_at DESC);
