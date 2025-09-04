-- V2__Insert_initial_data.sql
-- B2B Kurye servisi için örnek veriler

-- Sistem kullanıcıları (Admin, Koordinatör, Kurye)
INSERT INTO system_users (username, email, password, first_name, last_name, role, is_active) VALUES
('admin', 'admin@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'System', 'Admin', 'ADMIN', true),
('coord1', 'ahmet.yilmaz@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Ahmet', 'Yılmaz', 'COORDINATOR', true),
('coord2', 'ayse.demir@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Ayşe', 'Demir', 'COORDINATOR', true),
('courier1', 'ali.ozkan@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Ali', 'Özkan', 'COURIER', true),
('courier2', 'fatma.celik@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Fatma', 'Çelik', 'COURIER', true),
('courier3', 'osman.acar@courier.com', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.', 'Osman', 'Acar', 'COURIER', true);

-- Örnek araçlar
INSERT INTO vehicles (plate_number, vehicle_type, brand, model, year, color, max_weight_capacity, is_active) VALUES
('34ABC123', 'MOTORCYCLE', 'Yamaha', 'NMAX', 2023, 'Siyah', 30.00, true),
('34DEF456', 'MOTORCYCLE', 'Honda', 'PCX', 2022, 'Beyaz', 25.00, true),
('34GHI789', 'CAR', 'Renault', 'Clio', 2021, 'Gri', 300.00, true),
('BIKE001', 'BICYCLE', 'Bianchi', 'City', 2023, 'Mavi', 15.00, true),
('34JKL012', 'VAN', 'Ford', 'Transit', 2020, 'Beyaz', 1000.00, true);

-- Koordinatörler
INSERT INTO coordinators (system_user_id, name, email, phone, employee_id, work_type, shift_start, shift_end, status, coverage_area, max_courier_count, is_active) VALUES
(2, 'Ahmet Yılmaz', 'ahmet.yilmaz@courier.com', '+905551234567', 'COORD001', 'SHIFT', '09:00:00', '18:00:00', 'ONLINE', 'Kadıköy, Üsküdar, Ataşehir', 15, true),
(3, 'Ayşe Demir', 'ayse.demir@courier.com', '+905551234568', 'COORD002', 'SHIFT', '18:00:00', '03:00:00', 'OFFLINE', 'Beşiktaş, Şişli, Beyoğlu', 12, true);

-- Kuryeler (rating alanı kaldırıldı)
INSERT INTO couriers (system_user_id, name, email, phone, license_number, vehicle_type, work_type, shift_start, shift_end, status, total_deliveries, vehicle_id, coordinator_id, is_available) VALUES
(4, 'Ali Özkan', 'ali.ozkan@courier.com', '+905551111111', 'B123456', 'MOTORCYCLE', 'SHIFT', '09:00:00', '18:00:00', 'ONLINE', 150, 1, 1, true),
(5, 'Fatma Çelik', 'fatma.celik@courier.com', '+905552222222', 'B234567', 'MOTORCYCLE', 'SHIFT', '09:00:00', '18:00:00', 'OFFLINE', 200, 2, 1, true),
(6, 'Osman Acar', 'osman.acar@courier.com', '+905553333333', 'B345678', 'CAR', 'SHIFT', '18:00:00', '03:00:00', 'ONLINE', 120, 3, 2, true);

-- İşletmeler (sipariş veren firmalar)
INSERT INTO businesses (business_code, name, contact_person, phone, email, address, address_description, location_name, business_type, payment_terms, credit_limit, is_active, notes) VALUES
('BUS001', 'Köşe Restoran', 'Mehmet Bey', '+902161234567', 'info@koserestoran.com', 'Kadıköy Mah. Moda Cad. No:15 Kadıköy/İstanbul', 'Moda Sahili yanında, yeşil tabelalı bina, arka kapıdan giriş', 'Kadıköy Moda', 'Restaurant', 'POSTPAID', 5000.00, true, 'Hızlı teslimat gerektiren restoran'),

('BUS002', 'Pizza Palace Suadiye', 'Ayşe Hanım', '+902162345678', 'order@pizzapalace.com', 'Bağdat Cad. No:250 Suadiye/İstanbul', 'AVM karşısı, büyük pizza tabelası, araç park yeri var', 'Suadiye', 'Fast Food', 'PREPAID', 0.00, true, 'Günlük yoğun sipariş hacmi'),

('BUS003', 'Market Plus Ataşehir', 'Hasan Usta', '+902163456789', 'info@marketplus.com', 'Ataşehir Bulvarı No:100 Ataşehir/İstanbul', 'Metro çıkışı, 24 saat açık market, yükleme alanı mevcut', 'Ataşehir Merkez', 'Market', 'POSTPAID', 10000.00, true, '24 saat sipariş alıyor'),

('BUS004', 'Çiçek Dünyası Nişantaşı', 'Zeynep Hanım', '+902164567890', 'siparis@cicekdunyasi.com', 'Nişantaşı Cad. No:45 Şişli/İstanbul', 'Eski çiçekçiler çarşısı içinde, kırmızı tabela', 'Nişantaşı', 'Flower Shop', 'CASH_ON_DELIVERY', 2000.00, true, 'Özel günlerde yoğunluk artar'),

('BUS005', 'Eczane Sağlık Beyoğlu', 'Doktor Ahmet', '+902165678901', 'info@eczanesaglik.com', 'İstiklal Cad. No:123 Beyoğlu/İstanbul', 'Galatasaray Lisesi yanı, yeşil haç tabelası, 7/24 açık', 'Beyoğlu İstiklal', 'Pharmacy', 'POSTPAID', 3000.00, true, 'Acil ilaç siparişleri alır');

-- Örnek siparişler
INSERT INTO orders (order_number, status, priority, business_id, business_contact_person, business_phone, courier_id, coordinator_id, end_customer_name, end_customer_phone, pickup_address, pickup_address_description, pickup_contact_person, delivery_address, delivery_address_description, package_description, package_weight, package_count, payment_type, delivery_fee, collection_amount, courier_notes, business_notes, scheduled_pickup_time, estimated_delivery_time, order_date) VALUES

('ORD-2025080401', 'PENDING', 'NORMAL', 1, 'Mehmet Bey', '+902161234567', NULL, 1, 'Elif Yılmaz', '+905556666666', 'Kadıköy Mah. Moda Cad. No:15 Kadıköy/İstanbul', 'Moda Sahili yanında, yeşil tabelalı bina, arka kapıdan giriş', 'Mehmet Bey', 'Acıbadem Mah. Çeçen Sok. No:8 Kat:3 Üsküdar/İstanbul', 'Apartman zili çalışmıyor, telefon arayın, 3. kat daire 7', 'Pizza Margherita ve İçecek', 1.5, 1, 'CASH', 25.00, 45.50, 'Sıcak tutulmalı, ekstra soğuk içecek', 'Müşteri arayıp geldiğinde teslim et', '2025-08-04 19:30:00', '2025-08-04 20:15:00', '2025-08-04 19:00:00'),

('ORD-2025080402', 'ASSIGNED', 'HIGH', 2, 'Ayşe Hanım', '+902162345678', 1, 1, 'Ahmet Kaya', '+905557777777', 'Bağdat Cad. No:250 Suadiye/İstanbul', 'AVM karşısı, büyük pizza tabelası, araç park yeri var', 'Ayşe Hanım', 'Levent Mah. Büyükdere Cad. No:200 Şişli/İstanbul', 'İş merkezinde, güvenlik kaydı gerekli, 15. kat Garanti Bankası', 'Büyük boy karışık pizza ve salata', 2.0, 1, 'BUSINESS_ACCOUNT', 35.00, 0.00, 'İş merkezine giriş için kimlik gerekli', 'Öğle molasında teslim edilmeli', '2025-08-04 20:00:00', '2025-08-04 21:00:00', '2025-08-04 19:45:00'),

('ORD-2025080403', 'IN_TRANSIT', 'URGENT', 5, 'Doktor Ahmet', '+902165678901', 3, 2, 'Yaşlı Teyze', '+905558888888', 'İstiklal Cad. No:123 Beyoğlu/İstanbul', 'Galatasaray Lisesi yanı, yeşil haç tabelası, 7/24 açık', 'Doktor Ahmet', 'Fenerbahçe Mah. Bağdat Cad. No:300 Kat:1 Kadıköy/İstanbul', 'Kapıcı var, apartman no: 5, yaşlı teyze tek başına', 'Acil ilaç paketi - Kalp ilacı', 0.3, 1, 'CASH', 15.00, 125.00, 'Acil ilaç, hızlı teslimat gerekli, para tahsil edilecek', 'Kalp hastası için acil, gecikme olmasın', '2025-08-04 18:45:00', '2025-08-04 19:30:00', '2025-08-04 18:30:00');

-- Sipariş takip kayıtları
INSERT INTO order_tracking (order_id, status, description, created_by, created_by_type) VALUES
(1, 'PENDING', 'Sipariş alındı, kurye ataması bekleniyor', 'SISTEM', 'SYSTEM'),
(2, 'PENDING', 'Sipariş alındı', 'SISTEM', 'SYSTEM'),
(2, 'ASSIGNED', 'Ali Özkan kuryesine atandı', 'Ahmet Yılmaz', 'COORDINATOR'),
(3, 'PENDING', 'Acil sipariş alındı', 'SISTEM', 'SYSTEM'),
(3, 'ASSIGNED', 'Osman Acar kuryesine atandı', 'Ayşe Demir', 'COORDINATOR'),
(3, 'PICKED_UP', 'Paket eczaneden alındı', 'Osman Acar', 'COURIER'),
(3, 'IN_TRANSIT', 'Teslimat adresine doğru yola çıkıldı', 'Osman Acar', 'COURIER');
