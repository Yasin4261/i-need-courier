-- V1__Create_initial_tables.sql
-- B2B Kurye servisi için temel tabloları oluşturan migration

-- PostgreSQL için Enum tiplerini önceden oluşturma
CREATE TYPE user_role AS ENUM ('ADMIN', 'COORDINATOR', 'COURIER');
CREATE TYPE vehicle_type AS ENUM ('BICYCLE', 'MOTORCYCLE', 'CAR', 'VAN', 'TRUCK', 'WALKING');
CREATE TYPE work_type AS ENUM ('JOKER', 'SHIFT');
CREATE TYPE user_status AS ENUM ('ONLINE', 'OFFLINE', 'BUSY', 'ON_BREAK', 'IN_FIELD', 'IN_DELIVERY');
CREATE TYPE order_status AS ENUM ('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED', 'RETURNED');
CREATE TYPE order_priority AS ENUM ('NORMAL', 'HIGH', 'URGENT');
CREATE TYPE payment_type AS ENUM ('CASH', 'CREDIT_CARD', 'BUSINESS_ACCOUNT', 'CASH_ON_DELIVERY', 'ONLINE');
CREATE TYPE tracking_creator_type AS ENUM ('SYSTEM', 'COORDINATOR', 'COURIER', 'BUSINESS', 'CUSTOMER');

-- System Users tablosu (Sadece sistem kullanıcıları: Admin, Koordinatör, Kurye)
CREATE TABLE system_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    role user_role NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kullanıcı indeksleri
CREATE INDEX idx_email ON system_users(email);
CREATE INDEX idx_username ON system_users(username);
CREATE INDEX idx_role ON system_users(role);
CREATE INDEX idx_is_active ON system_users(is_active);

-- Vehicles tablosu (Araç bilgileri)
CREATE TABLE vehicles (
    id BIGSERIAL PRIMARY KEY,
    plate_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type vehicle_type NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    year INT,
    color VARCHAR(30),
    max_weight_capacity DECIMAL(8,2),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Araç indeksleri
CREATE INDEX idx_plate_number ON vehicles(plate_number);
CREATE INDEX idx_vehicle_type ON vehicles(vehicle_type);
CREATE INDEX idx_vehicles_is_active ON vehicles(is_active);

-- Coordinators tablosu (Takım kaptanları/koordinatörler)
CREATE TABLE coordinators (
    id BIGSERIAL PRIMARY KEY,
    system_user_id BIGINT UNIQUE REFERENCES system_users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    work_type work_type DEFAULT 'SHIFT',
    shift_start TIME,
    shift_end TIME,
    is_active BOOLEAN DEFAULT TRUE,
    status user_status DEFAULT 'OFFLINE',
    current_latitude FLOAT,
    current_longitude FLOAT,
    coverage_area VARCHAR(500),
    max_courier_count INT DEFAULT 10,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Koordinatör indeksleri
CREATE INDEX idx_coord_email ON coordinators(email);
CREATE INDEX idx_employee_id ON coordinators(employee_id);
CREATE INDEX idx_coord_status ON coordinators(status);
CREATE INDEX idx_coord_is_active ON coordinators(is_active);
CREATE INDEX idx_location ON coordinators(current_latitude, current_longitude);

-- Couriers tablosu (Kuryeler)
CREATE TABLE couriers (
    id BIGSERIAL PRIMARY KEY,
    system_user_id BIGINT UNIQUE REFERENCES system_users(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_number VARCHAR(50),
    vehicle_type vehicle_type,
    work_type work_type DEFAULT 'SHIFT',
    shift_start TIME,
    shift_end TIME,
    is_available BOOLEAN DEFAULT TRUE,
    status user_status DEFAULT 'OFFLINE',
    current_latitude FLOAT,
    current_longitude FLOAT,
    current_location_name VARCHAR(200),
    total_deliveries INT DEFAULT 0,
    vehicle_id BIGINT REFERENCES vehicles(id) ON DELETE SET NULL,
    coordinator_id BIGINT REFERENCES coordinators(id) ON DELETE SET NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Kurye indeksleri
CREATE INDEX idx_courier_email ON couriers(email);
CREATE INDEX idx_courier_status ON couriers(status);
CREATE INDEX idx_is_available ON couriers(is_available);
CREATE INDEX idx_courier_work_type ON couriers(work_type);
CREATE INDEX idx_courier_vehicle_type ON couriers(vehicle_type);

-- İşletmeler (sipariş veren firmalar) tablosu
CREATE TABLE businesses (
    id BIGSERIAL PRIMARY KEY,
    business_code VARCHAR(20) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    contact_person VARCHAR(100),
    phone VARCHAR(20),
    email VARCHAR(100),
    address TEXT,
    address_description TEXT,
    location_name VARCHAR(200),
    business_type VARCHAR(50),
    payment_terms VARCHAR(20),
    credit_limit DECIMAL(10,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- İşletme indeksleri
CREATE INDEX idx_business_code ON businesses(business_code);
CREATE INDEX idx_business_name ON businesses(name);
CREATE INDEX idx_business_is_active ON businesses(is_active);

-- Siparişler tablosu
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    order_number VARCHAR(20) UNIQUE NOT NULL,
    status order_status NOT NULL DEFAULT 'PENDING',
    priority order_priority NOT NULL DEFAULT 'NORMAL',
    business_id BIGINT REFERENCES businesses(id),
    business_contact_person VARCHAR(100),
    business_phone VARCHAR(20),
    courier_id BIGINT REFERENCES couriers(id),
    coordinator_id BIGINT REFERENCES coordinators(id),
    end_customer_name VARCHAR(100),
    end_customer_phone VARCHAR(20),
    pickup_address TEXT NOT NULL,
    pickup_address_description TEXT,
    pickup_contact_person VARCHAR(100),
    delivery_address TEXT NOT NULL,
    delivery_address_description TEXT,
    package_description TEXT,
    package_weight DECIMAL(8,2),
    package_count INT DEFAULT 1,
    payment_type payment_type DEFAULT 'CASH',
    delivery_fee DECIMAL(10,2) NOT NULL,
    collection_amount DECIMAL(10,2) DEFAULT 0.00,
    courier_notes TEXT,
    business_notes TEXT,
    scheduled_pickup_time TIMESTAMP,
    estimated_delivery_time TIMESTAMP,
    order_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sipariş indeksleri
CREATE INDEX idx_order_number ON orders(order_number);
CREATE INDEX idx_order_status ON orders(status);
CREATE INDEX idx_order_priority ON orders(priority);
CREATE INDEX idx_order_business_id ON orders(business_id);
CREATE INDEX idx_order_courier_id ON orders(courier_id);

-- Sipariş takip tablosu
CREATE TABLE order_tracking (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT REFERENCES orders(id),
    status order_status NOT NULL,
    description TEXT,
    created_by VARCHAR(100) NOT NULL,
    created_by_type tracking_creator_type NOT NULL,
    latitude FLOAT,
    longitude FLOAT,
    location_name VARCHAR(200),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Sipariş takip indeksleri
CREATE INDEX idx_tracking_order_id ON order_tracking(order_id);
CREATE INDEX idx_tracking_status ON order_tracking(status);

-- PostgreSQL için güncelleme tetikleyicisi (updated_at alanını otomatik güncellemek için)
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER update_system_users_updated_at
    BEFORE UPDATE ON system_users
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_vehicles_updated_at
    BEFORE UPDATE ON vehicles
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_coordinators_updated_at
    BEFORE UPDATE ON coordinators
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_couriers_updated_at
    BEFORE UPDATE ON couriers
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_businesses_updated_at
    BEFORE UPDATE ON businesses
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
