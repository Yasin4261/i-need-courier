-- V18: Siparişlere pickup/delivery koordinatları ekle
-- Harita üzerinde restoran (pickup) ve müşteri (delivery) konumlarını göstermek için.
-- Koordinat verisi olmayan siparişler için Kadıköy merkez çevresinde değerler atanır.

-- 1) Kolonları ekle
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS pickup_latitude DOUBLE PRECISION;
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS pickup_longitude DOUBLE PRECISION;
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_latitude DOUBLE PRECISION;
ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS delivery_longitude DOUBLE PRECISION;

-- 2) Mevcut koordinatsız siparişleri Kadıköy çevresinde doldur
-- Kadıköy merkez: 40.9907, 29.0245
-- Pickup ve delivery için küçük rastgele ofsetlerle gerçekçi konumlar üret.
UPDATE orders
SET pickup_latitude    = 40.9907 + (random() - 0.5) * 0.02,
    pickup_longitude   = 29.0245 + (random() - 0.5) * 0.02,
    delivery_latitude  = 40.9907 + (random() - 0.5) * 0.03,
    delivery_longitude = 29.0245 + (random() - 0.5) * 0.03
WHERE pickup_latitude IS NULL
   OR delivery_latitude IS NULL;

-- 3) Koordinatı olmayan işletmeleri de Kadıköy merkeze yerleştir
UPDATE businesses
SET latitude  = 40.9907 + (random() - 0.5) * 0.015,
    longitude = 29.0245 + (random() - 0.5) * 0.015
WHERE latitude IS NULL
   OR longitude IS NULL;

COMMENT
ON COLUMN orders.pickup_latitude IS 'Alış (restoran) konumu enlem';
COMMENT
ON COLUMN orders.pickup_longitude IS 'Alış (restoran) konumu boylam';
COMMENT
ON COLUMN orders.delivery_latitude IS 'Teslimat (müşteri) konumu enlem';
COMMENT
ON COLUMN orders.delivery_longitude IS 'Teslimat (müşteri) konumu boylam';

