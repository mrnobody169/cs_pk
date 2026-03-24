-- =====================================================
-- Poker Cash Group Tracker - Seed Data
-- Password cho tất cả user: password
-- BCrypt hash của "password"
-- =====================================================

-- =====================================================
-- 1. USERS
-- =====================================================
INSERT INTO users (email, password_hash, display_name, status, is_admin) VALUES
    ('admin@poker.com',   '$2a$10$DOCqGx/f2VbQFxdKFpyvLeDu4Di8DyDCXSG60g3Hn1uNCwMg8bOp6', 'Admin',   'APPROVED', TRUE),
    ('alice@poker.com',   '$2a$10$DOCqGx/f2VbQFxdKFpyvLeDu4Di8DyDCXSG60g3Hn1uNCwMg8bOp6', 'Alice',   'APPROVED', FALSE),
    ('bob@poker.com',     '$2a$10$DOCqGx/f2VbQFxdKFpyvLeDu4Di8DyDCXSG60g3Hn1uNCwMg8bOp6', 'Bob',     'APPROVED', FALSE),
    ('charlie@poker.com', '$2a$10$DOCqGx/f2VbQFxdKFpyvLeDu4Di8DyDCXSG60g3Hn1uNCwMg8bOp6', 'Charlie', 'APPROVED', FALSE);

-- =====================================================
-- 2. GROUPS
-- =====================================================
INSERT INTO groups (name, created_by) VALUES
    ('Nhóm Thứ 6', 1),    -- id = 1, tạo bởi admin
    ('Nhóm Cuối Tuần', 2); -- id = 2, tạo bởi alice

-- =====================================================
-- 3. GROUP MEMBERS
-- =====================================================
-- Nhóm Thứ 6: admin=ADMIN, alice=MEMBER, bob=MEMBER
INSERT INTO group_members (group_id, user_id, role) VALUES
    (1, 1, 'ADMIN'),
    (1, 2, 'MEMBER'),
    (1, 3, 'MEMBER');

-- Nhóm Cuối Tuần: alice=ADMIN, charlie=MEMBER
INSERT INTO group_members (group_id, user_id, role) VALUES
    (2, 2, 'ADMIN'),
    (2, 4, 'MEMBER');

-- =====================================================
-- 4. PLAYERS
-- =====================================================
-- Players trong Nhóm Thứ 6
INSERT INTO players (group_id, name, alias, is_active) VALUES
    (1, 'Admin',   'AD', TRUE),
    (1, 'Alice',   'AL', TRUE),
    (1, 'Bob',     'BB', TRUE),
    (1, 'David',   'DV', TRUE),
    (1, 'Eve',     'EV', FALSE);

-- Players trong Nhóm Cuối Tuần
INSERT INTO players (group_id, name, alias, is_active) VALUES
    (2, 'Alice',   'AL', TRUE),
    (2, 'Charlie', 'CH', TRUE),
    (2, 'Frank',   'FK', TRUE);

-- =====================================================
-- 5. DAILY SESSIONS
-- =====================================================
-- Nhóm Thứ 6, phiên tuần trước (đã lock)
INSERT INTO daily_sessions (group_id, session_date, data_locked, locked_by, locked_at, created_by) VALUES
    (1, CURRENT_DATE - INTERVAL '7 days', TRUE,  1, NOW() - INTERVAL '6 days', 1),
    (1, CURRENT_DATE - INTERVAL '14 days', TRUE, 1, NOW() - INTERVAL '13 days', 1),
    (1, CURRENT_DATE,                      FALSE, NULL, NULL, 1);

-- Nhóm Cuối Tuần, phiên gần đây
INSERT INTO daily_sessions (group_id, session_date, data_locked, created_by) VALUES
    (2, CURRENT_DATE - INTERVAL '2 days', FALSE, 2),
    (2, CURRENT_DATE,                     FALSE, 2);

-- =====================================================
-- 6. ENTRIES (session_id 1 — Nhóm Thứ 6, tuần trước, đã lock)
-- member_user_id references users.id (1=admin, 2=alice, 3=bob, 4=charlie)
-- =====================================================
INSERT INTO entries (daily_session_id, member_user_id, buy_in_total, cash_out_total, settlement_status, settled_amount, settled_by, settled_at, created_by, updated_by) VALUES
    (1, 1, 500000, 750000, 'PAID',    250000, 1, NOW() - INTERVAL '6 days', 1, 1),
    (1, 2, 500000, 200000, 'PAID',    300000, 1, NOW() - INTERVAL '6 days', 1, 1),
    (1, 3, 300000, 400000, 'PAID',    100000, 1, NOW() - INTERVAL '6 days', 1, 1);

-- Session 2 — Nhóm Thứ 6, 2 tuần trước, đã lock
INSERT INTO entries (daily_session_id, member_user_id, buy_in_total, cash_out_total, settlement_status, created_by, updated_by) VALUES
    (2, 1, 400000, 600000, 'PAID',    1, 1),
    (2, 2, 400000, 100000, 'PAID',    1, 1),
    (2, 3, 200000, 350000, 'PAID',    1, 1);

-- Session 3 — Nhóm Thứ 6, hôm nay, chưa lock
INSERT INTO entries (daily_session_id, member_user_id, buy_in_total, cash_out_total, settlement_status, created_by, updated_by) VALUES
    (3, 1, 300000, 0,      'UNPAID', 1, 1),
    (3, 2, 300000, 500000, 'UNPAID', 1, 1),
    (3, 3, 200000, 150000, 'UNPAID', 1, 1);

-- Session 4 — Nhóm Cuối Tuần (alice=2, charlie=4)
INSERT INTO entries (daily_session_id, member_user_id, buy_in_total, cash_out_total, settlement_status, created_by, updated_by) VALUES
    (4, 2, 500000, 300000, 'UNPAID', 2, 2),
    (4, 4, 500000, 900000, 'UNPAID', 2, 2);

-- =====================================================
-- SUMMARY
-- =====================================================
-- Users:    admin@poker.com / alice@poker.com / bob@poker.com / charlie@poker.com
-- Password: password123
-- Groups:   "Nhóm Thứ 6" (id=1), "Nhóm Cuối Tuần" (id=2)
-- Sessions: 5 phiên với đầy đủ entries
-- =====================================================
