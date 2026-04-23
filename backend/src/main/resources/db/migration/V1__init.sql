CREATE TABLE fund (
  id UUID PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(255) NOT NULL,
  benchmark_code VARCHAR(50),
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE TABLE nav_history (
  id UUID PRIMARY KEY,
  fund_id UUID NOT NULL REFERENCES fund(id) ON DELETE CASCADE,
  nav_date DATE NOT NULL,
  nav_value NUMERIC(18,6) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE (fund_id, nav_date)
);
CREATE INDEX idx_nav_fund_date ON nav_history(fund_id, nav_date DESC);

CREATE TABLE benchmark_history (
  id UUID PRIMARY KEY,
  benchmark_code VARCHAR(50) NOT NULL,
  benchmark_date DATE NOT NULL,
  benchmark_value NUMERIC(18,6) NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE (benchmark_code, benchmark_date)
);

CREATE TABLE signal (
  id UUID PRIMARY KEY,
  fund_id UUID NOT NULL REFERENCES fund(id) ON DELETE CASCADE,
  signal_date DATE NOT NULL,
  score NUMERIC(5,2) NOT NULL,
  label VARCHAR(20) NOT NULL,
  reasons TEXT NOT NULL,
  metrics_json TEXT NOT NULL,
  created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
  UNIQUE (fund_id, signal_date)
);
CREATE INDEX idx_signal_fund_date ON signal(fund_id, signal_date DESC);
