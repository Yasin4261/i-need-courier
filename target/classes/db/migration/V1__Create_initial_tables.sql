-- V1__Create_initial_tables.sql
-- B2B Kurye servisi için temel tabloları oluşturan migration

-- System Users tablosu (Sadece sistem kullanıcıları: Admin, Koordinatör, Kurye)
CREATE TABLE system_users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    phone VARCHAR(20),
    role ENUM('ADMIN', 'COORDINATOR', 'COURIER') NOT NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_email (email),
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_is_active (is_active)
);

-- Vehicles tablosu (Araç bilgileri)
CREATE TABLE vehicles (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    plate_number VARCHAR(20) UNIQUE NOT NULL,
    vehicle_type ENUM('BICYCLE', 'MOTORCYCLE', 'CAR', 'VAN', 'TRUCK', 'WALKING') NOT NULL,
    brand VARCHAR(50),
    model VARCHAR(50),
    year INT,
    color VARCHAR(30),
    max_weight_capacity DECIMAL(8,2) COMMENT 'Maksimum taşıma kapasitesi (kg)',
    is_active BOOLEAN DEFAULT TRUE,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_plate_number (plate_number),
    INDEX idx_vehicle_type (vehicle_type),
    INDEX idx_is_active (is_active)
);

-- Coordinators tablosu (Takım kaptanları/koordinatörler)
CREATE TABLE coordinators (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    employee_id VARCHAR(50) UNIQUE,
    work_type ENUM('JOKER', 'SHIFT') DEFAULT 'SHIFT',
    shift_start TIME,
    shift_end TIME,
    is_active BOOLEAN DEFAULT TRUE,
    status ENUM('ONLINE', 'OFFLINE', 'BUSY', 'ON_BREAK', 'IN_FIELD') DEFAULT 'OFFLINE',
    current_latitude FLOAT,
    current_longitude FLOAT,
    coverage_area VARCHAR(500) COMMENT 'Sorumlu olduğu bölge',
    max_courier_count INT DEFAULT 10 COMMENT 'Maksimum yönetebileceği kurye sayısı',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (system_user_id) REFERENCES system_users(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_employee_id (employee_id),
    INDEX idx_status (status),
    INDEX idx_is_active (is_active),
    INDEX idx_location (current_latitude, current_longitude)
);

-- Couriers tablosu (Kuryeler)
CREATE TABLE couriers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    system_user_id BIGINT UNIQUE,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    phone VARCHAR(20) NOT NULL,
    license_number VARCHAR(50),
    vehicle_type ENUM('BICYCLE', 'MOTORCYCLE', 'CAR', 'VAN', 'TRUCK', 'WALKING'),
    work_type ENUM('JOKER', 'SHIFT') DEFAULT 'SHIFT',
    shift_start TIME,
    shift_end TIME,
    is_available BOOLEAN DEFAULT TRUE,
    status ENUM('ONLINE', 'OFFLINE', 'BUSY', 'ON_BREAK', 'IN_DELIVERY') DEFAULT 'OFFLINE',
    current_latitude FLOAT,
    current_longitude FLOAT,
    current_location_name VARCHAR(200),
    total_deliveries INT DEFAULT 0,
    vehicle_id BIGINT,
    coordinator_id BIGINT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (system_user_id) REFERENCES system_users(id) ON DELETE SET NULL,
    FOREIGN KEY (vehicle_id) REFERENCES vehicles(id) ON DELETE SET NULL,
    FOREIGN KEY (coordinator_id) REFERENCES coordinators(id) ON DELETE SET NULL,
    INDEX idx_email (email),
    INDEX idx_status (status),
    INDEX idx_is_available (is_available),
    INDEX idx_work_type (work_type),
    INDEX idx_vehicle_type (vehicle_type),
    INDEX idx_location (current_latitude, current_longitude),
    INDEX idx_coordinator_id (coordinator_id)
);

-- Business/Company tablosu (İşletmeler - sipariş veren firmalar)
CREATE TABLE businesses (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    business_code VARCHAR(50) UNIQUE NOT NULL COMMENT 'İşletme kodu (otomatik veya manuel)',
    name VARCHAR(200) NOT NULL,
    contact_person VARCHAR(100) COMMENT 'İrtibat kişisi',
    phone VARCHAR(20) NOT NULL,
    email VARCHAR(100),
    address VARCHAR(500) NOT NULL,
    address_description TEXT COMMENT 'Adres tarifi - detaylı açıklama',


    latitude FLOAT,
    longitude FLOAT,
    location_name VARCHAR(200) COMMENT 'Konum ismi (Kadıköy, Merkez vb.)',
    business_type VARCHAR(100) COMMENT 'İşletme türü (Restaurant, Market, Eczane vb.)',
    payment_terms ENUM('PREPAID', 'POSTPAID', 'CASH_ON_DELIVERY') DEFAULT 'POSTPAID',
    credit_limit DECIMAL(10,2) DEFAULT 0.00,
    is_active BOOLEAN DEFAULT TRUE,
    notes TEXT COMMENT 'İşletme hakkında özel notlar',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    INDEX idx_business_code (business_code),
    INDEX idx_name (name),
    INDEX idx_phone (phone),
    INDEX idx_business_type (business_type),
    INDEX idx_is_active (is_active),
    INDEX idx_location (latitude, longitude)
);

-- Orders tablosu (Siparişler/Paketler)
CREATE TABLE orders (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_number VARCHAR(50) UNIQUE NOT NULL,
    status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED') DEFAULT 'PENDING',
    priority ENUM('LOW', 'NORMAL', 'HIGH', 'URGENT') DEFAULT 'NORMAL',

    -- İşletme bilgileri (sipariş veren)
    business_id BIGINT NOT NULL,
    business_contact_person VARCHAR(100),
    business_phone VARCHAR(20),

    -- Kurye ve koordinatör bilgileri
    courier_id BIGINT,
    coordinator_id BIGINT,

    -- Son müşteri bilgileri (teslimat alacak kişi)
    end_customer_name VARCHAR(100) NOT NULL COMMENT 'Teslimatı alacak kişi',
    end_customer_phone VARCHAR(20) COMMENT 'Teslimatı alacak kişinin telefonu',

    -- Alım lokasyonu (işletmeden)
    pickup_address TEXT NOT NULL,
    pickup_address_description TEXT COMMENT 'Alım adresi tarifi',
    pickup_latitude FLOAT,
    pickup_longitude FLOAT,
    pickup_location_name VARCHAR(200),
    pickup_contact_person VARCHAR(100) COMMENT 'Alım yerindeki irtibat kişisi',

    -- Teslimat lokasyonu (son müşteriye)
    delivery_address TEXT NOT NULL,
    delivery_address_description TEXT COMMENT 'Teslimat adresi tarifi',
    delivery_latitude FLOAT,
    delivery_longitude FLOAT,
    delivery_location_name VARCHAR(200),

    -- Paket bilgileri
    package_description VARCHAR(500) NOT NULL,
    package_weight FLOAT COMMENT 'Paket ağırlığı (kg)',
    package_count INT DEFAULT 1 COMMENT 'Paket adedi',
    package_value DECIMAL(10,2) COMMENT 'Paket değeri (sigorta için)',

    -- Ödeme bilgileri
    payment_type ENUM('CASH', 'CARD', 'ONLINE', 'BUSINESS_ACCOUNT') DEFAULT 'BUSINESS_ACCOUNT',
    delivery_fee DECIMAL(10,2) NOT NULL,
    collection_amount DECIMAL(10,2) DEFAULT 0.00 COMMENT 'Tahsil edilecek tutar (varsa)',

    -- Notlar ve dökümanlar
    courier_notes TEXT COMMENT 'Kuryeye özel notlar',
    business_notes TEXT COMMENT 'İşletmenin notları',
    receipt_image_url VARCHAR(500) COMMENT 'Fiş/Adisyon resmi URL',
    delivery_proof_url VARCHAR(500) COMMENT 'Teslimat kanıtı resmi URL',

    -- Zaman bilgileri
    order_date DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT 'Sipariş tarihi',
    scheduled_pickup_time DATETIME COMMENT 'Planlanan alım zamanı',
    actual_pickup_time DATETIME COMMENT 'Gerçek alım zamanı',
    estimated_delivery_time DATETIME COMMENT 'Tahmini teslimat zamanı',
    actual_delivery_time DATETIME COMMENT 'Gerçek teslimat zamanı',

    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (business_id) REFERENCES businesses(id) ON DELETE RESTRICT,
    FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE SET NULL,
    FOREIGN KEY (coordinator_id) REFERENCES coordinators(id) ON DELETE SET NULL,

    INDEX idx_order_number (order_number),
    INDEX idx_status (status),
    INDEX idx_priority (priority),
    INDEX idx_business_id (business_id),
    INDEX idx_courier_id (courier_id),
    INDEX idx_coordinator_id (coordinator_id),
    INDEX idx_order_date (order_date),
    INDEX idx_scheduled_pickup (scheduled_pickup_time),
    INDEX idx_estimated_delivery (estimated_delivery_time),
    INDEX idx_payment_type (payment_type)
);

-- Order tracking tablosu (Sipariş takip geçmişi)
CREATE TABLE order_tracking (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status ENUM('PENDING', 'ASSIGNED', 'PICKED_UP', 'IN_TRANSIT', 'DELIVERED', 'CANCELLED') NOT NULL,
    description VARCHAR(500),
    location VARCHAR(200),
    latitude DECIMAL(10, 8),
    longitude DECIMAL(11, 8),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(100) COMMENT 'Durumu güncelleyen kişi (kurye adı, koordinatör veya sistem)',
    created_by_type ENUM('SYSTEM', 'COURIER', 'COORDINATOR', 'ADMIN') DEFAULT 'SYSTEM',

    FOREIGN KEY (order_id) REFERENCES orders(id) ON DELETE CASCADE,
    INDEX idx_order_id (order_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at),
    INDEX idx_location (latitude, longitude),
    INDEX idx_created_by_type (created_by_type)
);
