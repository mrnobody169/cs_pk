-- ============================================================
-- SmartNote / PokerTrack Database Schema
-- PostgreSQL
-- ============================================================

-- Users
CREATE TABLE IF NOT EXISTS users (
    id            BIGSERIAL PRIMARY KEY,
    email         VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name  VARCHAR(100),
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Groups
CREATE TABLE IF NOT EXISTS groups (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_by BIGINT       NOT NULL REFERENCES users(id),
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Group Members
CREATE TABLE IF NOT EXISTS group_members (
    group_id  BIGINT      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id   BIGINT      NOT NULL REFERENCES users(id)  ON DELETE CASCADE,
    role      VARCHAR(20) NOT NULL DEFAULT 'MEMBER',  -- ADMIN | MEMBER | VIEWER
    joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    PRIMARY KEY (group_id, user_id)
);

-- Players  (named players within a group, may or may not be app users)
CREATE TABLE IF NOT EXISTS players (
    id         BIGSERIAL    PRIMARY KEY,
    group_id   BIGINT       NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    name       VARCHAR(100) NOT NULL,
    alias      VARCHAR(100),
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Daily Sessions
CREATE TABLE IF NOT EXISTS daily_sessions (
    id           BIGSERIAL   PRIMARY KEY,
    group_id     BIGINT      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    session_date DATE        NOT NULL,
    data_locked  BOOLEAN     NOT NULL DEFAULT FALSE,
    locked_by    BIGINT      REFERENCES users(id),
    locked_at    TIMESTAMPTZ,
    created_by   BIGINT      NOT NULL REFERENCES users(id),
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE (group_id, session_date)
);

-- Entries  (buy-in / cash-out per player per session)
CREATE TABLE IF NOT EXISTS entries (
    id                BIGSERIAL      PRIMARY KEY,
    daily_session_id  BIGINT         NOT NULL REFERENCES daily_sessions(id) ON DELETE CASCADE,
    player_id         BIGINT         NOT NULL REFERENCES players(id),
    buy_in_total      NUMERIC(12, 2) NOT NULL DEFAULT 0,
    cash_out_total    NUMERIC(12, 2) NOT NULL DEFAULT 0,
    profit            NUMERIC(12, 2) GENERATED ALWAYS AS (cash_out_total - buy_in_total) STORED,
    note              TEXT,
    -- Settlement
    settlement_status VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',  -- UNPAID | PAID | PARTIAL | DISPUTED
    settled_amount    NUMERIC(12, 2),
    settlement_note   TEXT,
    settled_by        BIGINT         REFERENCES users(id),
    settled_at        TIMESTAMPTZ,
    -- Audit
    created_by        BIGINT         REFERENCES users(id),
    updated_by        BIGINT         REFERENCES users(id),
    created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
    updated_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW()
);

-- Audit Logs
CREATE TABLE IF NOT EXISTS audit_logs (
    id            BIGSERIAL   PRIMARY KEY,
    group_id      BIGINT      NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    entity_type   VARCHAR(50) NOT NULL,  -- ENTRY | SESSION | PLAYER
    entity_id     BIGINT      NOT NULL,
    action        VARCHAR(50) NOT NULL,  -- CREATE | UPDATE | DELETE | LOCK | SETTLEMENT
    before_json   TEXT,
    after_json    TEXT,
    actor_user_id BIGINT      REFERENCES users(id),
    created_at    TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
