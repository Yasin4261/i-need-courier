-- Convert business status from ENUM to VARCHAR for better Hibernate compatibility
-- This fixes the "column status is of type business_status but expression is of type character varying" error

-- Drop the enum constraint and convert to VARCHAR
ALTER TABLE businesses ALTER COLUMN status TYPE VARCHAR(50);

-- Drop the enum type (if no other tables use it)
DROP TYPE IF EXISTS business_status CASCADE;

-- Add check constraint to ensure only valid values
ALTER TABLE businesses ADD CONSTRAINT business_status_check
    CHECK (status IN ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE'));

-- Update existing NULL values to PENDING
UPDATE businesses SET status = 'PENDING' WHERE status IS NULL;

COMMENT ON COLUMN businesses.status IS 'Business account status: PENDING, ACTIVE, SUSPENDED, INACTIVE';

