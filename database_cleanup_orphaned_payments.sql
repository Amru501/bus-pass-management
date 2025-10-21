-- Database Cleanup Script: Remove Orphaned Payment Records
-- This script removes payment records that reference non-existent users
-- Run this if you get errors like "Unable to find User with id X"

-- Step 1: Check for orphaned payment records (payments referencing deleted users)
SELECT p.id, p.user_id, p.amount, p.due_date, p.status
FROM payments p
LEFT JOIN users u ON p.user_id = u.id
WHERE u.id IS NULL;

-- Step 2: Delete orphaned payment records
-- CAUTION: This will permanently delete payment records with invalid user references
-- Uncomment the line below to execute the deletion
-- DELETE FROM payments WHERE user_id NOT IN (SELECT id FROM users);

-- Alternative: Update orphaned payments to reference a placeholder user (if you have one)
-- UPDATE payments SET user_id = (SELECT id FROM users WHERE email = 'deleted@placeholder.com' LIMIT 1) 
-- WHERE user_id NOT IN (SELECT id FROM users);

-- Step 3: Verify cleanup
-- Run this query again to confirm no orphaned records remain
SELECT COUNT(*) as orphaned_count
FROM payments p
LEFT JOIN users u ON p.user_id = u.id
WHERE u.id IS NULL;

