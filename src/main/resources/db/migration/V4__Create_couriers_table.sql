-- Create couriers table for JWT authentication
CREATE TABLE IF NOT EXISTS couriers (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    phone VARCHAR(20) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'INACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_login_at TIMESTAMP,

    CONSTRAINT chk_courier_status CHECK (status IN ('INACTIVE', 'ONLINE', 'OFFLINE', 'BUSY', 'SUSPENDED'))
);

-- Create indexes for better performance (only if not exists)
CREATE INDEX IF NOT EXISTS idx_couriers_email ON couriers(email);
CREATE INDEX IF NOT EXISTS idx_couriers_status ON couriers(status);
CREATE INDEX IF NOT EXISTS idx_couriers_created_at ON couriers(created_at);
