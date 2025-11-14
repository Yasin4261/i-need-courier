-- V12__Create_shift_system.sql
-- Kurye Vardiya Yönetim Sistemi
-- Dökümantasyondaki tasarıma uygun olarak shifts tablosu ve yönetim özellikleri

-- Vardiya Durumu ENUM
CREATE TYPE shift_status AS ENUM (
    'RESERVED',      -- Vardiya rezerve edildi
    'CHECKED_IN',    -- Vardiyaya giriş yapıldı (aktif çalışıyor)
    'CHECKED_OUT',   -- Vardiyadan çıkış yapıldı (tamamlandı)
    'CANCELLED',     -- Rezervasyon iptal edildi
    'NO_SHOW'        -- Kurye rezerve ettiği vardiyaya gelmedi
);

-- Vardiya Rolü ENUM (dökümantasyona uygun)
CREATE TYPE shift_role AS ENUM (
    'COURIER',       -- Normal kurye
    'CAPTAIN'        -- Takım kaptanı
);

-- Ana Vardiya Tablosu (dökümantasyondaki shifts tablosu)
-- Kuryelerin planlanmış çalışma saatleri ve durumları
CREATE TABLE shifts (
    shift_id BIGSERIAL PRIMARY KEY,
    courier_id BIGINT NOT NULL REFERENCES couriers(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    shift_role shift_role NOT NULL DEFAULT 'COURIER',
    status shift_status NOT NULL DEFAULT 'RESERVED',
    check_in_time TIMESTAMP WITH TIME ZONE,
    check_out_time TIMESTAMP WITH TIME ZONE,
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

    -- Check-in zamanı başlangıçtan önce olamaz (30dk tolerans)
    CONSTRAINT chk_checkin_time CHECK (
        check_in_time IS NULL OR
        check_in_time >= start_time - INTERVAL '30 minutes'
    ),

    -- Check-out zamanı check-in zamanından sonra olmalı
    CONSTRAINT chk_checkout_after_checkin CHECK (
        check_out_time IS NULL OR
        check_in_time IS NULL OR
        check_out_time > check_in_time
    ),

    -- Vardiya süresi mantıklı olmalı (max 24 saat)
    CONSTRAINT chk_shift_duration CHECK (
        end_time > start_time AND
        end_time <= start_time + INTERVAL '24 hours'
    )
);

-- Vardiya Şablonları Tablosu (kolay vardiya tanımlama için)
CREATE TABLE shift_templates (
    template_id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    default_role shift_role DEFAULT 'COURIER',
    max_couriers INT DEFAULT 10,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- İndeksler
CREATE INDEX idx_shifts_courier ON shifts(courier_id);
CREATE INDEX idx_shifts_status ON shifts(status);
CREATE INDEX idx_shifts_start_time ON shifts(start_time);
CREATE INDEX idx_shifts_date_range ON shifts(start_time, end_time);
CREATE INDEX idx_shifts_courier_status ON shifts(courier_id, status);

CREATE INDEX idx_shift_templates_active ON shift_templates(is_active);
CREATE INDEX idx_shift_templates_times ON shift_templates(start_time, end_time);

-- Updated_at otomatik güncelleme trigger'ı
CREATE TRIGGER update_shifts_updated_at
    BEFORE UPDATE ON shifts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_shift_templates_updated_at
    BEFORE UPDATE ON shift_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Sıra tabanlı atama için kuryeler tablosuna on_duty_since kolonu ekle
ALTER TABLE couriers
ADD COLUMN on_duty_since TIMESTAMP WITH TIME ZONE;

-- Örnek Vardiya Şablonları
INSERT INTO shift_templates (name, description, start_time, end_time, default_role, max_couriers) VALUES
('Sabah Vardiyası', 'Sabah teslimatları için erken vardiya', '09:00:00', '17:00:00', 'COURIER', 15),
('Akşam Vardiyası', 'Öğleden sonra ve akşam teslimatları', '14:00:00', '22:00:00', 'COURIER', 12),
('Gece Vardiyası', 'Gece teslimatları (restoran vb.)', '18:00:00', '02:00:00', 'COURIER', 8),
('Tam Gün Vardiyası', 'Tam gün çalışma (joker kuryeler)', '08:00:00', '20:00:00', 'COURIER', 5),
('Kaptan Sabah Vardiyası', 'Takım kaptanı sabah vardiyası', '08:00:00', '16:00:00', 'CAPTAIN', 3);

COMMENT ON TABLE shifts IS 'Kuryelerin planlanmış ve gerçekleşen vardiyaları - check-in/out sistemi ile';
COMMENT ON TABLE shift_templates IS 'Vardiya şablonları - tekrar eden vardiya zaman dilimleri';
COMMENT ON COLUMN shifts.start_time IS 'Planlanan vardiya başlangıç zamanı';
COMMENT ON COLUMN shifts.end_time IS 'Planlanan vardiya bitiş zamanı';
COMMENT ON COLUMN shifts.check_in_time IS 'Gerçek giriş zamanı (kurye vardiyaya check-in yaptığında)';
COMMENT ON COLUMN shifts.check_out_time IS 'Gerçek çıkış zamanı (kurye vardiyadan check-out yaptığında)';
COMMENT ON COLUMN shifts.status IS 'RESERVED: Rezerve, CHECKED_IN: Aktif çalışıyor, CHECKED_OUT: Tamamlandı';
COMMENT ON COLUMN couriers.on_duty_since IS 'Sıra tabanlı atama için - kurye vardiyaya giriş yaptığında set edilir';

