-- V16: Create order_assignments table
-- This table tracks order assignment history between orders and couriers

CREATE TABLE IF NOT EXISTS order_assignments (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    courier_id BIGINT NOT NULL,
    assigned_at TIMESTAMP WITH TIME ZONE NOT NULL,
    response_at TIMESTAMP WITH TIME ZONE,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    rejection_reason TEXT,
    assignment_type VARCHAR(20) NOT NULL DEFAULT 'AUTO',
    timeout_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,

    -- Foreign key constraints
    CONSTRAINT fk_order_assignment_order FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    CONSTRAINT fk_order_assignment_courier FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE,

    -- Check constraints
    CONSTRAINT chk_assignment_status CHECK (status IN ('PENDING', 'ACCEPTED', 'REJECTED', 'TIMEOUT', 'CANCELLED')),
    CONSTRAINT chk_assignment_type CHECK (assignment_type IN ('AUTO', 'MANUAL', 'REASSIGN'))
);

-- Create indexes for faster lookups
CREATE INDEX IF NOT EXISTS idx_order_assignments_order_id ON order_assignments(order_id);
CREATE INDEX IF NOT EXISTS idx_order_assignments_courier_id ON order_assignments(courier_id);
CREATE INDEX IF NOT EXISTS idx_order_assignments_status ON order_assignments(status);
CREATE INDEX IF NOT EXISTS idx_order_assignments_assigned_at ON order_assignments(assigned_at);
CREATE INDEX IF NOT EXISTS idx_order_assignments_timeout_at ON order_assignments(timeout_at);

-- Add comments
COMMENT ON TABLE order_assignments IS 'Tracks order assignment history between orders and couriers';
COMMENT ON COLUMN order_assignments.status IS 'Assignment status: PENDING, ACCEPTED, REJECTED, TIMEOUT, CANCELLED';
COMMENT ON COLUMN order_assignments.assignment_type IS 'How the assignment was made: AUTO, MANUAL, REASSIGN';
COMMENT ON COLUMN order_assignments.timeout_at IS 'When the assignment will timeout if not responded';

