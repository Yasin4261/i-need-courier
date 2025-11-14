-- V13__Convert_shift_enums_to_varchar.sql
-- Convert shift_role and shift_status columns from ENUM to VARCHAR

-- Drop existing check constraints if any
ALTER TABLE shifts DROP CONSTRAINT IF EXISTS shifts_shift_role_check;
ALTER TABLE shifts DROP CONSTRAINT IF EXISTS shifts_status_check;

-- Convert shift_role column
ALTER TABLE shifts ALTER COLUMN shift_role TYPE VARCHAR(50);

-- Convert status column
ALTER TABLE shifts ALTER COLUMN status TYPE VARCHAR(20);

-- Add check constraints to ensure data integrity
ALTER TABLE shifts ADD CONSTRAINT shifts_shift_role_check
    CHECK (shift_role IN ('COURIER', 'CAPTAIN', 'SUPERVISOR'));

ALTER TABLE shifts ADD CONSTRAINT shifts_status_check
    CHECK (status IN ('RESERVED', 'CHECKED_IN', 'CHECKED_OUT', 'CANCELLED', 'NO_SHOW'));

-- Update existing rows if any (shouldn't be any in fresh database)
-- No UPDATE needed as we're converting compatible types

