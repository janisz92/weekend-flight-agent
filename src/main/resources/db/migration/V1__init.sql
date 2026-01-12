-- =========================
-- window_check (planner)
-- =========================
CREATE TABLE window_check (
  id BIGSERIAL PRIMARY KEY,
  provider TEXT NOT NULL,
  origin CHAR(3) NOT NULL,
  destination CHAR(3) NOT NULL,
  depart_date DATE NOT NULL,
  return_date DATE NOT NULL,
  window_key TEXT NOT NULL,
  last_checked_at TIMESTAMPTZ NULL,
  check_count INTEGER NOT NULL DEFAULT 0,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_window_check_provider_window UNIQUE (provider, window_key)
);

CREATE INDEX ix_window_check_provider_depart_date
  ON window_check(provider, depart_date);

CREATE INDEX ix_window_check_provider_last_checked
  ON window_check(provider, last_checked_at);


-- =========================
-- price_observation
-- =========================
CREATE TABLE price_observation (
  id BIGSERIAL PRIMARY KEY,
  observed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  provider TEXT NOT NULL,
  origin CHAR(3) NOT NULL,
  destination CHAR(3) NOT NULL,
  depart_date DATE NOT NULL,
  return_date DATE NOT NULL,
  full_days SMALLINT NOT NULL,
  departure_month DATE NOT NULL,
  window_key TEXT NOT NULL,
  offer_key TEXT NOT NULL,
  price_pln INTEGER NOT NULL
);

CREATE INDEX ix_price_obs_offer_time
  ON price_observation(offer_key, observed_at DESC);

CREATE INDEX ix_price_obs_segment_time
  ON price_observation(origin, destination, full_days, departure_month, observed_at DESC);

CREATE INDEX ix_price_obs_window_time
  ON price_observation(window_key, observed_at DESC);


-- =========================
-- baseline (rolling 30 days)
-- =========================
CREATE TABLE baseline (
  id BIGSERIAL PRIMARY KEY,
  origin CHAR(3) NOT NULL,
  destination CHAR(3) NOT NULL,
  full_days SMALLINT NOT NULL,
  departure_month DATE NOT NULL,
  median30_pln INTEGER NULL,
  mean30_pln INTEGER NULL,
  std30_pln INTEGER NULL,
  min30_pln INTEGER NULL,
  count30 INTEGER NOT NULL,
  computed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  CONSTRAINT uq_baseline_segment
    UNIQUE (origin, destination, full_days, departure_month)
);


-- =========================
-- deal
-- =========================
CREATE TABLE deal (
  id BIGSERIAL PRIMARY KEY,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  status TEXT NOT NULL,
  provider TEXT NOT NULL,
  origin CHAR(3) NOT NULL,
  destination CHAR(3) NOT NULL,
  depart_date DATE NOT NULL,
  return_date DATE NOT NULL,
  full_days SMALLINT NOT NULL,
  departure_month DATE NOT NULL,
  window_key TEXT NOT NULL,
  offer_key TEXT NOT NULL,
  price_pln INTEGER NOT NULL,
  baseline_median30_pln INTEGER NULL,
  percent_below_median NUMERIC(6,2) NULL,
  saving_pln INTEGER NULL,
  CONSTRAINT uq_deal_offer UNIQUE (offer_key)
);

CREATE INDEX ix_deal_status_created
  ON deal(status, created_at DESC);

CREATE INDEX ix_deal_destination_created
  ON deal(destination, created_at DESC);


-- =========================
-- notification_log
-- =========================
CREATE TABLE notification_log (
  id BIGSERIAL PRIMARY KEY,
  sent_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  channel TEXT NOT NULL,
  offer_key TEXT NOT NULL,
  deal_id BIGINT NULL REFERENCES deal(id),
  destination CHAR(3) NOT NULL,
  success BOOLEAN NOT NULL,
  error_message TEXT NULL,
  CONSTRAINT uq_notification_dedupe UNIQUE (channel, offer_key)
);

CREATE INDEX ix_notification_sent_at
  ON notification_log(sent_at DESC);

CREATE INDEX ix_notification_destination_sent_at
  ON notification_log(destination, sent_at DESC);

