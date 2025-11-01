-- Add authentication columns to businesses table for login functionality
-- This allows businesses to register and login to the system

-- Add password hash column
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS password_hash VARCHAR(255);

-- Add user status enum type if not exists
DO $$ BEGIN
    CREATE TYPE business_status AS ENUM ('PENDING', 'ACTIVE', 'SUSPENDED', 'INACTIVE');
EXCEPTION
    WHEN duplicate_object THEN null;
END $$;

-- Add status column with enum type
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS status business_status DEFAULT 'PENDING';

-- Add verification columns
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS email_verified BOOLEAN DEFAULT FALSE;
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS verification_token VARCHAR(255);
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS verification_token_expires_at TIMESTAMP;

-- Add last login tracking
ALTER TABLE businesses ADD COLUMN IF NOT EXISTS last_login_at TIMESTAMP;

-- Make email unique and not null (for login)
ALTER TABLE businesses ALTER COLUMN email SET NOT NULL;

-- Add unique constraint only if it doesn't exist
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'unique_business_email'
    ) THEN
        ALTER TABLE businesses ADD CONSTRAINT unique_business_email UNIQUE (email);
    END IF;
END $$;

-- Create index on email for faster login queries
CREATE INDEX IF NOT EXISTS idx_businesses_email ON businesses(email);
CREATE INDEX IF NOT EXISTS idx_businesses_status ON businesses(status);

-- Update existing businesses to have ACTIVE status if they were active before
UPDATE businesses
SET status = 'ACTIVE'::business_status
WHERE is_active = TRUE AND status IS NULL;

-- Add comments
COMMENT ON COLUMN businesses.password_hash IS 'BCrypt hashed password for business login';
COMMENT ON COLUMN businesses.status IS 'Business account status: PENDING (awaiting approval), ACTIVE (can login), SUSPENDED (blocked), INACTIVE (deactivated)';
COMMENT ON COLUMN businesses.email_verified IS 'Whether business email has been verified';
COMMENT ON COLUMN businesses.last_login_at IS 'Timestamp of last successful login';

