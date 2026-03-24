-- =====================================================
-- Migration: Add status + is_admin to users table
-- Run this ONCE on existing database
-- =====================================================

-- 1. Thêm cột status (mặc định PENDING cho user mới)
ALTER TABLE users ADD COLUMN IF NOT EXISTS status VARCHAR(20) NOT NULL DEFAULT 'PENDING';

-- 2. Thêm cột is_admin
ALTER TABLE users ADD COLUMN IF NOT EXISTS is_admin BOOLEAN NOT NULL DEFAULT FALSE;

-- 3. Set tất cả user hiện tại = APPROVED
UPDATE users SET status = 'APPROVED' WHERE status = 'PENDING';

-- 4. Set admin@poker.com là super admin
UPDATE users SET is_admin = TRUE WHERE email = 'admin@poker.com';

-- 5. Thêm constraint (nếu chưa có)
-- ALTER TABLE users ADD CONSTRAINT chk_user_status CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED'));
