-- =====================================================
-- Migration: Replace player_id with member_user_id in entries
-- Also fix audit_log → audit_logs if not yet done
-- Run this ONCE on existing database
-- =====================================================

-- 1. Add member_user_id column to entries
ALTER TABLE entries ADD COLUMN IF NOT EXISTS member_user_id BIGINT;

-- 2. Drop the old player_id foreign key constraint if it exists
ALTER TABLE entries DROP CONSTRAINT IF EXISTS entries_player_id_fkey;

-- 3. Drop player_id column
-- (Optional — only if you want to fully remove it. Keep if you want to migrate data first)
-- ALTER TABLE entries DROP COLUMN IF EXISTS player_id;

-- 4. Add foreign key for member_user_id → users(id)
ALTER TABLE entries ADD CONSTRAINT fk_entries_member_user
    FOREIGN KEY (member_user_id) REFERENCES users(id);
