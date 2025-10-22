-- ============================================
-- Database Migration Script: Create Passes Table
-- ============================================
-- This script creates the new `passes` table and migrates data from users table

USE buspassdb;

-- ============================================
-- Step 1: Create new passes table
-- ============================================
CREATE TABLE IF NOT EXISTS passes (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    selected_route VARCHAR(255) NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_user_id (user_id),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ============================================
-- Step 2: Migrate existing data from users to passes
-- ============================================
-- Only migrate users that have bus pass fields set
INSERT INTO passes (user_id, selected_route, status)
SELECT 
    id,
    selected_route,
    CASE 
        WHEN bus_pass_active = 1 THEN 'ACTIVE'
        ELSE 'INACTIVE'
    END as status
FROM users
WHERE role = 'USER'  -- Only migrate regular users
ON DUPLICATE KEY UPDATE
    selected_route = VALUES(selected_route),
    status = VALUES(status);

-- ============================================
-- Step 3: Drop old columns from users table
-- ============================================
ALTER TABLE users DROP COLUMN IF EXISTS selected_route;
ALTER TABLE users DROP COLUMN IF EXISTS bus_pass_active;

-- ============================================
-- Step 4: Verify the migration
-- ============================================
-- Check users table structure
DESCRIBE users;

-- Check passes table structure  
DESCRIBE passes;

-- Count records in passes table
SELECT COUNT(*) as total_passes FROM passes;

-- Show sample data
SELECT 
    u.id,
    u.name,
    u.email,
    p.selected_route,
    p.status as pass_status
FROM users u
LEFT JOIN passes p ON u.id = p.user_id
LIMIT 10;

-- ============================================
-- EXPECTED RESULTS
-- ============================================
-- users table should have:
-- - id, name, email, phone, password, role
--
-- passes table should have:
-- - id, user_id, selected_route, status
-- - status values: 'ACTIVE' or 'INACTIVE' (not 0/1)



