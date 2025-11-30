-- V900__insert_test_users.sql
-- password of hash: test123
INSERT INTO public.businesses (credit_limit, email_verified, is_active, latitude, longitude, created_at, id,
                               last_login_at, updated_at, verification_token_expires_at, phone, business_code,
                               business_type, contact_person, email, location_name, name, address, address_description,
                               notes, password_hash, payment_terms, status, verification_token)
VALUES ('1000.0', true, true, 41, 29, '2025-11-30 22:31:44', 42, '2025-11-30 22:31:47', '2025-11-30 22:31:47', null, '05321234567',
        '042', 'food', 'Keanu Reeves', 'keanu@reeves.com', 'Istanbul', 'Jack''s Burger', 'Moda, Istanbul', null, null,
        '$2a$10$E9wN7tezrqobA0z4DprMce2GUK/Icy6mKVk/UzPOchD0ZU8Rms1bG', 'PREPAID', 'ACTIVE', null);
