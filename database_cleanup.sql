-- ============================================
-- Database Cleanup Script for Users Table
-- ============================================
-- This script removes old unused columns from the users table
-- Run this on your database to clean up the schema

-- For Local Database (buspassdb)
USE buspassdb;

-- Check current columns in users table (optional, for verification)
-- DESCRIBE users;

-- Drop old unused columns from users table
-- Note: assigned_bus_id has a foreign key constraint that must be dropped first
ALTER TABLE users DROP COLUMN image;
ALTER TABLE users DROP COLUMN image_url;

-- Drop the foreign key constraint first (your constraint name may differ)
-- Find constraint name with: SHOW CREATE TABLE users;
ALTER TABLE users DROP FOREIGN KEY FKpsyaqks7s0ikhd31xotlwlrr9;
ALTER TABLE users DROP COLUMN assigned_bus_id;

-- Verify the cleanup (optional)
DESCRIBE users;

-- Expected columns after cleanup:
-- - id
-- - name
-- - email
-- - phone
-- - password
-- - role
-- - selected_route
-- - bus_pass_active

-- ============================================
-- For Render/Production Database
-- ============================================
-- When deploying to Render, you'll need to run this manually
-- via the Render MySQL console or using a MySQL client
-- connected to your production database.
--
-- Steps for Render:
-- 1. Go to your Render MySQL database dashboard
-- 2. Connect using the provided credentials
-- 3. Run the same ALTER TABLE commands above

