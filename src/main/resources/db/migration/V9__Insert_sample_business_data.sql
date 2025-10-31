-- Insert sample business data for testing
-- Password for all test businesses: password123

INSERT INTO businesses (
    business_code,
    name,
    email,
    password_hash,
    phone,
    address,
    contact_person,
    business_type,
    status,
    is_active,
    email_verified,
    created_at,
    updated_at
) VALUES
(
    'BUS-TEST001',
    'Pizza Palace',
    'contact@pizzapalace.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Z7bv8xQq8Z0MQfRJ/Q5IfB8q1C9qGe', -- password123
    '+905551234001',
    'Kadıköy, İstanbul',
    'Mehmet Yılmaz',
    'Restaurant',
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'BUS-TEST002',
    'Burger King Branch',
    'branch@burgerking.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Z7bv8xQq8Z0MQfRJ/Q5IfB8q1C9qGe', -- password123
    '+905551234002',
    'Beşiktaş, İstanbul',
    'Ayşe Demir',
    'Fast Food',
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'BUS-TEST003',
    'Pharmacy Plus',
    'info@pharmacyplus.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Z7bv8xQq8Z0MQfRJ/Q5IfB8q1C9qGe', -- password123
    '+905551234003',
    'Şişli, İstanbul',
    'Can Öztürk',
    'Pharmacy',
    'ACTIVE',
    true,
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    'BUS-TEST004',
    'New Business Pending',
    'pending@newbiz.com',
    '$2a$10$N9qo8uLOickgx2ZMRZoMye.Z7bv8xQq8Z0MQfRJ/Q5IfB8q1C9qGe', -- password123
    '+905551234004',
    'Ankara',
    'Test User',
    'General',
    'PENDING',
    true,
    false,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

