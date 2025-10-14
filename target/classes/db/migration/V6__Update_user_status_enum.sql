-- Update user_status enum to match application CourierStatus enum
-- First, add the new values we need
ALTER TYPE user_status ADD VALUE IF NOT EXISTS 'INACTIVE';
ALTER TYPE user_status ADD VALUE IF NOT EXISTS 'SUSPENDED';

-- We need to handle existing data that uses the old enum values
-- Update any existing records that might have incompatible values
UPDATE couriers SET status = 'OFFLINE' WHERE status IN ('ON_BREAK', 'IN_FIELD', 'IN_DELIVERY');

-- Now we have: ONLINE, OFFLINE, BUSY, ON_BREAK, IN_FIELD, IN_DELIVERY, INACTIVE, SUSPENDED
-- The application uses: INACTIVE, ONLINE, OFFLINE, BUSY, SUSPENDED
-- This is compatible now since all app values exist in DB
