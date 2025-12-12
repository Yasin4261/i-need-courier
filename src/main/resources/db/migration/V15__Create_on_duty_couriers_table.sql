-- V15: Create on_duty_couriers table
-- This table tracks couriers who are currently on duty and available for order assignments

CREATE TABLE IF NOT EXISTS on_duty_couriers (
    id BIGSERIAL PRIMARY KEY,
    courier_id BIGINT NOT NULL UNIQUE,
    shift_id BIGINT,
    on_duty_since TIMESTAMP WITH TIME ZONE NOT NULL,
    source VARCHAR(50) NOT NULL DEFAULT 'app',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_on_duty_courier_id FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE,
    CONSTRAINT fk_on_duty_shift_id FOREIGN KEY (shift_id) REFERENCES shifts(shift_id) ON DELETE SET NULL
);

-- Create index for faster lookups
CREATE INDEX IF NOT EXISTS idx_on_duty_couriers_courier_id ON on_duty_couriers(courier_id);
CREATE INDEX IF NOT EXISTS idx_on_duty_couriers_shift_id ON on_duty_couriers(shift_id);
CREATE INDEX IF NOT EXISTS idx_on_duty_couriers_on_duty_since ON on_duty_couriers(on_duty_since);

-- Add comment to table
COMMENT ON TABLE on_duty_couriers IS 'Tracks couriers currently on duty and available for order assignments';
COMMENT ON COLUMN on_duty_couriers.courier_id IS 'Reference to the courier';
COMMENT ON COLUMN on_duty_couriers.shift_id IS 'Optional reference to the active shift';
COMMENT ON COLUMN on_duty_couriers.on_duty_since IS 'Timestamp when the courier went on duty';
COMMENT ON COLUMN on_duty_couriers.source IS 'Source of duty status change (app, admin, system)';
