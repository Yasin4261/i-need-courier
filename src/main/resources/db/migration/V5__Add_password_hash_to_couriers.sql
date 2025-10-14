-- Add password_hash column to existing couriers table for JWT authentication
ALTER TABLE couriers ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Update the table to make password_hash NOT NULL after adding it
-- We'll set a default for existing records first
UPDATE couriers SET password_hash = 'placeholder' WHERE password_hash IS NULL;
ALTER TABLE couriers ALTER COLUMN password_hash SET NOT NULL;
