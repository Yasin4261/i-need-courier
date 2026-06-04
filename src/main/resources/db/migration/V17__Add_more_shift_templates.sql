-- V17: Daha çeşitli vardiya şablonları ekle
-- Kuryelere günün her saatine yönelik daha fazla seçenek sunar.
-- Mevcut V12 şablonları korunur, çakışmaları önlemek için aynı isimde olanlar atlanır.

INSERT INTO shift_templates (name, description, start_time, end_time, default_role, max_couriers, is_active)
SELECT new_templates.name,
       new_templates.description,
       new_templates.start_time,
       new_templates.end_time,
       new_templates.default_role::shift_role, new_templates.max_couriers,
       new_templates.is_active
FROM (VALUES
          -- Sabah blokları
          ('Erken Sabah Vardiyası', 'Şafak başlangıçlı erken saat teslimatları', TIME '06:00:00', TIME '12:00:00',
           'COURIER', 8, TRUE),
          ('Sabah Yarım Gün', 'Sabahtan öğleye 4 saatlik kısa vardiya', TIME '08:00:00', TIME '12:00:00', 'COURIER', 10,
           TRUE),
          ('Öğle Vardiyası', 'Öğle yoğunluğu için ideal vardiya', TIME '11:00:00', TIME '15:00:00', 'COURIER', 12,
           TRUE),

          -- Öğleden sonra blokları
          ('Öğleden Sonra Vardiyası', 'Öğleden sonra teslimatları', TIME '13:00:00', TIME '19:00:00', 'COURIER', 12,
           TRUE),
          ('Akşamüstü Vardiyası', 'İş çıkışı yoğunluğu (15:00 - 21:00)', TIME '15:00:00', TIME '21:00:00', 'COURIER',
           14, TRUE),

          -- Akşam / Gece blokları
          ('Akşam Pik Vardiyası', 'Akşam yemeği yoğunluk vardiyası (17:00 - 23:00)', TIME '17:00:00', TIME '23:00:00',
           'COURIER', 18, TRUE),
          ('Kapanış Vardiyası', 'Restoran kapanışlarına yönelik geç vardiya', TIME '20:00:00', TIME '02:00:00',
           'COURIER', 6, TRUE),
          ('Gece Ekspres', '24 saat çalışan işletmeler için gece vardiyası', TIME '22:00:00', TIME '06:00:00',
           'COURIER', 4, TRUE),

          -- Hafta sonu / esnek
          ('Hafta Sonu Sabah', 'Cumartesi-Pazar sabah vardiyası', TIME '10:00:00', TIME '16:00:00', 'COURIER', 15,
           TRUE),
          ('Hafta Sonu Akşam', 'Cumartesi-Pazar akşam vardiyası', TIME '16:00:00', TIME '23:00:00', 'COURIER', 15,
           TRUE),
          ('Esnek Kısa Vardiya', '3 saatlik mini vardiya - yoğun saatler', TIME '18:00:00', TIME '21:00:00', 'COURIER',
           20, TRUE),

          -- Kaptan rolleri
          ('Kaptan Akşam Vardiyası', 'Takım kaptanı akşam vardiyası', TIME '16:00:00', TIME '00:00:00', 'CAPTAIN', 3,
           TRUE),
          ('Kaptan Hafta Sonu', 'Hafta sonu takım kaptanı vardiyası', TIME '12:00:00', TIME '22:00:00', 'CAPTAIN', 2,
           TRUE)) AS new_templates(name, description, start_time, end_time, default_role, max_couriers, is_active)
WHERE NOT EXISTS (SELECT 1
                  FROM shift_templates st
                  WHERE st.name = new_templates.name);

-- Bilgi mesajı
DO
$$
DECLARE
total_count INTEGER;
BEGIN
SELECT COUNT(*)
INTO total_count
FROM shift_templates
WHERE is_active = TRUE;
RAISE
NOTICE 'Toplam aktif vardiya şablonu sayısı: %', total_count;
END $$;


