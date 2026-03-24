-- =====================================================
-- Poker Cash Group Tracker - Database Schema
-- PostgreSQL 13+
-- Run this file manually: psql -U <user> -d <dbname> -f schema.sql
-- =====================================================

-- 1. Users (Người dùng hệ thống)
CREATE TABLE users (
    id           BIGSERIAL PRIMARY KEY,
    email        VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
    is_admin     BOOLEAN NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 2. Groups (Nhóm chơi)
CREATE TABLE groups (
    id         BIGSERIAL PRIMARY KEY,
    name       VARCHAR(255) NOT NULL,
    created_by BIGINT NOT NULL REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. Group Members (Thành viên trong nhóm & Phân quyền)
CREATE TABLE group_members (
    group_id  BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    user_id   BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role      VARCHAR(50) NOT NULL CHECK (role IN ('ADMIN', 'MEMBER', 'VIEWER')),
    joined_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (group_id, user_id)
);

-- 4. Players (Danh sách người chơi trong nhóm)
CREATE TABLE players (
    id         BIGSERIAL PRIMARY KEY,
    group_id   BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    name       VARCHAR(255) NOT NULL,
    alias      VARCHAR(255),
    is_active  BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Daily Sessions (Các phiên chơi theo ngày, 1 ngày 1 session mỗi nhóm)
CREATE TABLE daily_sessions (
    id           BIGSERIAL PRIMARY KEY,
    group_id     BIGINT NOT NULL REFERENCES groups(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    data_locked  BOOLEAN DEFAULT FALSE,
    locked_by    BIGINT REFERENCES users(id),
    locked_at    TIMESTAMP WITH TIME ZONE,
    created_by   BIGINT NOT NULL REFERENCES users(id),
    created_at   TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_group_session_date UNIQUE (group_id, session_date)
);

-- 6. Entries (Ghi chép dòng tiền từng thành viên trong 1 phiên)
CREATE TABLE entries (
    id               BIGSERIAL PRIMARY KEY,
    daily_session_id BIGINT NOT NULL REFERENCES daily_sessions(id) ON DELETE CASCADE,
    member_user_id   BIGINT NOT NULL REFERENCES users(id),
    buy_in_total     NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (buy_in_total >= 0),
    cash_out_total   NUMERIC(15, 2) NOT NULL DEFAULT 0 CHECK (cash_out_total >= 0),
    profit           NUMERIC(15, 2) NOT NULL GENERATED ALWAYS AS (cash_out_total - buy_in_total) STORED,
    note             TEXT,

    -- Settlement (Kết toán)
    settlement_status VARCHAR(50) NOT NULL DEFAULT 'UNPAID'
        CHECK (settlement_status IN ('UNPAID', 'PAID', 'PARTIAL', 'DISPUTED')),
    settled_amount   NUMERIC(15, 2),
    settlement_note  TEXT,
    settled_by       BIGINT REFERENCES users(id),
    settled_at       TIMESTAMP WITH TIME ZONE,

    -- Audit
    created_by BIGINT NOT NULL REFERENCES users(id),
    updated_by BIGINT REFERENCES users(id),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uq_session_member UNIQUE (daily_session_id, member_user_id)
);

-- 7. Audit Log (Lưu vết mọi thay đổi quan trọng)
CREATE TABLE audit_logs (
    id            BIGSERIAL PRIMARY KEY,
    group_id      BIGINT NOT NULL REFERENCES groups(id),
    entity_type   VARCHAR(50) NOT NULL CHECK (entity_type IN ('ENTRY', 'SESSION', 'MEMBER')),
    entity_id     BIGINT NOT NULL,
    action        VARCHAR(50) NOT NULL CHECK (action IN ('CREATE', 'UPDATE', 'DELETE', 'LOCK', 'SETTLEMENT')),
    before_json   TEXT,
    after_json    TEXT,
    actor_user_id BIGINT NOT NULL REFERENCES users(id),
    created_at    TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- =====================================================
-- INDEXES
-- =====================================================
CREATE INDEX idx_sessions_group_date     ON daily_sessions(group_id, session_date);
CREATE INDEX idx_entries_session_id      ON entries(daily_session_id);
CREATE INDEX idx_entries_member_user_id  ON entries(member_user_id);
CREATE INDEX idx_entries_settlement      ON entries(settlement_status);
CREATE INDEX idx_audit_logs_entity       ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_group_date   ON audit_logs(group_id, created_at);
CREATE INDEX idx_group_members_user_id   ON group_members(user_id);
