-- Convert courier status from user_status enum to VARCHAR
ALTER TABLE couriers
ALTER COLUMN status TYPE VARCHAR(20) USING status::text;

-- Add check constraint
ALTER TABLE couriers
DROP CONSTRAINT IF EXISTS chk_courier_status;

ALTER TABLE couriers
ADD CONSTRAINT chk_courier_status
CHECK (status IN ('INACTIVE', 'ONLINE', 'OFFLINE', 'BUSY', 'SUSPENDED'));

