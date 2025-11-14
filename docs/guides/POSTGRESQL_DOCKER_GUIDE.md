# PostgreSQL Veritabanı Kontrol Rehberi

Docker container'ı içinden PostgreSQL veritabanınızı kontrol etmek için adım adım rehber.

---

## 🐳 1. Docker Container'a Giriş

### Container'ı Bulma
```bash
# Çalışan container'ları listele
docker ps

# Veya docker compose ile
docker compose ps
```

### PostgreSQL Container'a Bağlanma
```bash
# Container adını kullanarak giriş
docker exec -it courier-postgres bash

# Veya docker compose ile
docker compose exec postgres bash
```

---

## 🗄️ 2. PostgreSQL'e Bağlanma

Container içine girdikten sonra:

```bash
# psql ile veritabanına bağlan
psql -U courier_user -d courier_db

# Veya tek komutta (container dışından)
docker exec -it courier-postgres psql -U courier_user -d courier_db
```

**Not:** Şifre istenirse: `courier_password` (compose.yaml'dan)

---

## 📊 3. Veritabanlarını Listeleme

### Tüm Veritabanlarını Göster
```sql
-- psql içinde
\l
-- veya
\list

-- SQL ile
SELECT datname FROM pg_database;
```

**Çıktı Örneği:**
```
                                  List of databases
     Name      |     Owner     | Encoding |   Collate   |    Ctype    
---------------+---------------+----------+-------------+-------------
 courier_db    | courier_user  | UTF8     | en_US.utf8  | en_US.utf8
 postgres      | postgres      | UTF8     | en_US.utf8  | en_US.utf8
 template0     | postgres      | UTF8     | en_US.utf8  | en_US.utf8
 template1     | postgres      | UTF8     | en_US.utf8  | en_US.utf8
```

### Mevcut Veritabanına Bağlan
```sql
\c courier_db
-- veya
\connect courier_db
```

---

## 📋 4. Tabloları Listeleme

### Tüm Tabloları Göster
```sql
-- psql içinde (en yaygın)
\dt

-- Detaylı bilgi ile
\dt+

-- SQL ile
SELECT table_name 
FROM information_schema.tables 
WHERE table_schema = 'public'
ORDER BY table_name;
```

**Çıktı Örneği:**
```
                List of relations
 Schema |        Name         | Type  |    Owner     
--------+---------------------+-------+--------------
 public | businesses          | table | courier_user
 public | coordinators        | table | courier_user
 public | couriers            | table | courier_user
 public | flyway_schema_history | table | courier_user
 public | order_tracking      | table | courier_user
 public | orders              | table | courier_user
 public | shift_templates     | table | courier_user
 public | shifts              | table | courier_user
 public | system_users        | table | courier_user
 public | vehicles            | table | courier_user
```

---

## 🔍 5. Tablo Yapısını Görüntüleme

### Belirli Bir Tablonun Şemasını Göster

#### shifts Tablosu
```sql
-- psql içinde
\d shifts

-- veya detaylı
\d+ shifts
```

**Çıktı Örneği:**
```
                                            Table "public.shifts"
     Column      |           Type           | Collation | Nullable |                Default                
-----------------+--------------------------+-----------+----------+---------------------------------------
 shift_id        | bigint                   |           | not null | nextval('shifts_shift_id_seq'::regclass)
 courier_id      | bigint                   |           | not null | 
 start_time      | timestamp with time zone |           | not null | 
 end_time        | timestamp with time zone |           | not null | 
 shift_role      | shift_role               |           | not null | 'COURIER'::shift_role
 status          | shift_status             |           | not null | 'RESERVED'::shift_status
 check_in_time   | timestamp with time zone |           |          | 
 check_out_time  | timestamp with time zone |           |          | 
 notes           | text                     |           |          | 
 created_at      | timestamp                |           |          | CURRENT_TIMESTAMP
 updated_at      | timestamp                |           |          | CURRENT_TIMESTAMP
Indexes:
    "shifts_pkey" PRIMARY KEY, btree (shift_id)
    "idx_shifts_courier" btree (courier_id)
    "idx_shifts_courier_status" btree (courier_id, status)
    "idx_shifts_date_range" btree (start_time, end_time)
    "idx_shifts_status" btree (status)
Foreign-key constraints:
    "shifts_courier_id_fkey" FOREIGN KEY (courier_id) REFERENCES couriers(id) ON DELETE CASCADE
```

#### shift_templates Tablosu
```sql
\d shift_templates
```

#### couriers Tablosu (on_duty_since kontrolü için)
```sql
\d couriers
```

---

## 📊 6. Tablo Verilerini Görüntüleme

### shifts Tablosundaki Verileri Göster
```sql
-- İlk 10 kayıt
SELECT * FROM shifts LIMIT 10;

-- Sadece belirli kolonlar
SELECT shift_id, courier_id, status, start_time, end_time 
FROM shifts 
ORDER BY created_at DESC 
LIMIT 10;

-- Kayıt sayısı
SELECT COUNT(*) FROM shifts;

-- Durum bazlı gruplama
SELECT status, COUNT(*) as count 
FROM shifts 
GROUP BY status;
```

### shift_templates Tablosundaki Verileri Göster
```sql
-- Tüm şablonlar
SELECT * FROM shift_templates;

-- Aktif şablonlar
SELECT template_id, name, start_time, end_time, max_couriers 
FROM shift_templates 
WHERE is_active = true;
```

### couriers Tablosunda on_duty_since Kontrolü
```sql
-- on_duty_since kolonu var mı?
SELECT column_name, data_type, is_nullable, column_default
FROM information_schema.columns
WHERE table_name = 'couriers' AND column_name = 'on_duty_since';

-- Aktif kuryeler ve on_duty_since
SELECT id, name, status, on_duty_since 
FROM couriers 
WHERE on_duty_since IS NOT NULL
ORDER BY on_duty_since ASC;
```

---

## 🔧 7. ENUM Tiplerini Kontrol Etme

### Sistemdeki ENUM Tiplerini Listele
```sql
SELECT t.typname, e.enumlabel, e.enumsortorder
FROM pg_type t 
JOIN pg_enum e ON t.oid = e.enumtypid  
WHERE t.typname IN ('shift_status', 'shift_role')
ORDER BY t.typname, e.enumsortorder;
```

**Çıktı Örneği:**
```
   typname    | enumlabel  | enumsortorder 
--------------+------------+---------------
 shift_role   | COURIER    |             1
 shift_role   | CAPTAIN    |             2
 shift_status | RESERVED   |             1
 shift_status | CHECKED_IN |             2
 shift_status | CHECKED_OUT|             3
 shift_status | CANCELLED  |             4
 shift_status | NO_SHOW    |             5
```

---

## 🗂️ 8. İndeksleri Görüntüleme

### Tüm İndeksler
```sql
-- psql içinde
\di

-- shifts tablosu indeksleri
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'shifts';
```

---

## 🔗 9. Foreign Key İlişkileri

### shifts Tablosu İlişkileri
```sql
SELECT
    tc.table_name, 
    kcu.column_name, 
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name 
FROM information_schema.table_constraints AS tc 
JOIN information_schema.key_column_usage AS kcu
  ON tc.constraint_name = kcu.constraint_name
JOIN information_schema.constraint_column_usage AS ccu
  ON ccu.constraint_name = tc.constraint_name
WHERE tc.constraint_type = 'FOREIGN KEY' 
  AND tc.table_name='shifts';
```

---

## 📈 10. Migration Durumunu Kontrol

### Flyway Migration Geçmişi
```sql
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- En son migration
SELECT * FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;

-- V12 migration kontrolü
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
WHERE version = '12';
```

**Beklenen:**
```
 version |     description      |     installed_on        | success 
---------+----------------------+-------------------------+---------
   12    | Create shift system  | 2025-11-12 04:35:29.064 | t
```

---

## 🎯 11. Hızlı Kontrol Komutları

### Tek Komutta Tüm Kontroller (Container Dışından)
```bash
# Tabloları listele
docker exec -it courier-postgres psql -U courier_user -d courier_db -c "\dt"

# shifts tablosu yapısı
docker exec -it courier-postgres psql -U courier_user -d courier_db -c "\d shifts"

# shift_templates verileri
docker exec -it courier-postgres psql -U courier_user -d courier_db -c "SELECT * FROM shift_templates;"

# Migration durumu
docker exec -it courier-postgres psql -U courier_user -d courier_db -c "SELECT version, description, success FROM flyway_schema_history ORDER BY installed_rank DESC LIMIT 5;"
```

---

## 🚀 12. Kullanışlı psql Komutları

```sql
\?              -- Yardım
\q              -- Çıkış
\l              -- Veritabanları listesi
\c DB_NAME      -- Veritabanı değiştir
\dt             -- Tabloları listele
\dt+            -- Tabloları detaylı listele
\d TABLE_NAME   -- Tablo yapısı
\d+ TABLE_NAME  -- Tablo yapısı (detaylı)
\di             -- İndeksler
\du             -- Kullanıcılar
\dn             -- Şemalar
\df             -- Fonksiyonlar
\dT             -- Tipler (ENUM'lar dahil)
\x              -- Expanded display (satır satır göster)
\timing         -- Sorgu süresini göster
```

---

## 📝 13. Örnek Kontrol Senaryosu

```bash
# 1. Container'a gir
docker exec -it courier-postgres psql -U courier_user -d courier_db

# 2. Tabloları kontrol et
\dt

# 3. shifts tablosunu incele
\d+ shifts

# 4. shift_templates'i incele
\d+ shift_templates

# 5. Verileri kontrol et
SELECT * FROM shift_templates;

# 6. ENUM'ları kontrol et
SELECT t.typname, e.enumlabel
FROM pg_type t 
JOIN pg_enum e ON t.oid = e.enumtypid  
WHERE t.typname LIKE 'shift%'
ORDER BY t.typname, e.enumsortorder;

# 7. Migration'ı kontrol et
SELECT version, description, installed_on, success 
FROM flyway_schema_history 
WHERE version = '12';

# 8. Çıkış
\q
```

---

## 🎨 14. Güzel Görünüm İçin

### psql'i Güzelleştir
```bash
# psql içinde
\x auto          -- Otomatik expanded display
\pset border 2   -- Çizgili tablo
\timing on       -- Sorgu sürelerini göster

# Veya .psqlrc dosyası oluştur
echo "\x auto" > ~/.psqlrc
echo "\pset border 2" >> ~/.psqlrc
echo "\timing on" >> ~/.psqlrc
```

---

## 🔧 15. Sorun Giderme

### Bağlantı Sorunu
```bash
# Container çalışıyor mu?
docker ps | grep postgres

# Logları kontrol et
docker logs courier-postgres

# Veritabanı servisi aktif mi?
docker exec -it courier-postgres pg_isready -U courier_user
```

### Şifre Hatası
```bash
# compose.yaml'dan şifreleri kontrol et
grep -A 5 "POSTGRES" compose.yaml

# Ortam değişkenleri
docker exec -it courier-postgres env | grep POSTGRES
```

---

## 📚 Özet Komutlar

```bash
# ======================================
# HIZLI BAŞLANGIÇ
# ======================================

# 1. Container'a gir ve psql başlat
docker exec -it courier-postgres psql -U courier_user -d courier_db

# 2. Tabloları göster
\dt

# 3. shifts tablosu
\d+ shifts

# 4. Verileri göster
SELECT * FROM shift_templates;

# 5. Çıkış
\q

# ======================================
# TEK SATIR KOMUTLAR (Container Dışından)
# ======================================

# Tabloları listele
docker exec courier-postgres psql -U courier_user -d courier_db -c "\dt"

# shifts yapısı
docker exec courier-postgres psql -U courier_user -d courier_db -c "\d shifts"

# shift_templates verileri
docker exec courier-postgres psql -U courier_user -d courier_db -c "SELECT * FROM shift_templates;"
```

---

**Hazırlayan:** GitHub Copilot  
**Tarih:** 14 Kasım 2025  
**Not:** Bu rehber i-need-courier projesi için hazırlanmıştır.

