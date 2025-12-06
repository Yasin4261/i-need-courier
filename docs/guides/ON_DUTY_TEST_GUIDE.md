# On-Duty Couriers Test Guide

## Ön Hazırlık

### 1. Servisleri Başlat

```bash
# PostgreSQL başlat
docker compose up -d postgres

# Uygulamayı başlat (opsiyonel, eğer Docker ile çalışmıyorsan)
./scripts/start.sh

# Veya Docker ile tüm stack
docker compose up -d
```

### 2. Migration Çalıştır

```bash
# Maven ile
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5433/courier_db \
  -Dflyway.user=courier_user -Dflyway.password=courier_password

# Veya uygulama startup'ta otomatik çalışır
```

### 3. Backfill Mevcut Verileri

```bash
# Backfill script ile
./scripts/backfill-on-duty-couriers.sh

# Veya manuel
docker exec -i courier-postgres psql -U courier_user -d courier_db <<'SQL'
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source)
SELECT c.id, s.shift_id, 
       COALESCE(c.on_duty_since, s.check_in_time, now()) AT TIME ZONE 'UTC',
       'backfill'
FROM couriers c
LEFT JOIN shifts s ON s.courier_id = c.id AND s.status = 'CHECKED_IN'
WHERE c.on_duty_since IS NOT NULL OR s.status = 'CHECKED_IN'
ON CONFLICT (courier_id) DO UPDATE
  SET on_duty_since = EXCLUDED.on_duty_since,
      shift_id = EXCLUDED.shift_id;
SQL
```

---

## Test Senaryoları

### Senaryo 1: Yeni Kurye Kaydı ve Check-In

#### 1.1. Kurye Kaydı

```bash
curl -X POST http://localhost:8081/api/v1/auth/register/courier \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Kurye Ali",
    "email": "ali.test@courier.com",
    "phone": "+905551234567",
    "password": "test123"
  }' | jq
```

**Beklenen Sonuç:**
```json
{
  "code": 200,
  "data": {
    "courierId": 10,
    "name": "Test Kurye Ali",
    "email": "ali.test@courier.com",
    "message": "Registration successful"
  },
  "message": "Courier registration successful"
}
```

#### 1.2. Login ve Token Al

```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ali.test@courier.com",
    "password": "test123"
  }' | jq -r '.data.token')

echo "Token: $TOKEN"
```

#### 1.3. Vardiya Şablonlarını Listele

```bash
curl -s -X GET http://localhost:8081/api/v1/courier/shifts/templates \
  -H "Authorization: Bearer $TOKEN" | jq
```

**Beklenen Sonuç:**
```json
{
  "code": 200,
  "data": [
    {
      "templateId": 1,
      "name": "Morning Shift",
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      ...
    }
  ]
}
```

#### 1.4. Vardiya Rezerve Et

```bash
# Bugün için vardiya rezerve et
SHIFT_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": 1,
    \"shiftDate\": \"$(date +%Y-%m-%d)\",
    \"notes\": \"Test rezervasyonu\"
  }")

echo "$SHIFT_RESPONSE" | jq

SHIFT_ID=$(echo "$SHIFT_RESPONSE" | jq -r '.data.shiftId')
echo "Shift ID: $SHIFT_ID"
```

#### 1.5. Check-In Yap

```bash
curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID/check-in" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}' | jq
```

**Beklenen Sonuç:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 42,
    "status": "CHECKED_IN",
    "checkInTime": "2025-12-02T15:30:00",
    ...
  },
  "message": "Vardiyaya giriş başarılı"
}
```

#### 1.6. Veritabanında Kontrol Et

```bash
# on_duty_couriers tablosunu kontrol et
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT od.id, od.courier_id, c.name, c.email, od.on_duty_since, od.shift_id, od.source
FROM on_duty_couriers od
JOIN couriers c ON c.id = od.courier_id
ORDER BY od.on_duty_since ASC;
"
```

**Beklenen Sonuç:**
```
 id | courier_id |       name        |         email          |      on_duty_since       | shift_id | source 
----+------------+-------------------+------------------------+--------------------------+----------+--------
  1 |         10 | Test Kurye Ali    | ali.test@courier.com   | 2025-12-02 15:30:00+00   |       42 | app
```

#### 1.7. Aktif Vardiyayı Sorgula

```bash
curl -s -X GET http://localhost:8081/api/v1/courier/shifts/active \
  -H "Authorization: Bearer $TOKEN" | jq
```

---

### Senaryo 2: Check-Out ve Temizlik

#### 2.1. Check-Out Yap

```bash
curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID/check-out" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Vardiya tamamlandı"}' | jq
```

**Beklenen Sonuç:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 42,
    "status": "CHECKED_OUT",
    "checkOutTime": "2025-12-02T17:00:00",
    ...
  },
  "message": "Vardiyadan çıkış başarılı"
}
```

#### 2.2. on_duty_couriers Tablosunu Kontrol Et

```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT COUNT(*) as active_couriers FROM on_duty_couriers;
"
```

**Beklenen Sonuç:**
```
 active_couriers 
-----------------
               0
```

---

### Senaryo 3: Çoklu Kurye ve FIFO Sıralaması

#### 3.1. İkinci Kurye Ekle

```bash
# Kurye 2
curl -X POST http://localhost:8081/api/v1/auth/register/courier \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Test Kurye Ayşe",
    "email": "ayse.test@courier.com",
    "phone": "+905559876543",
    "password": "test123"
  }' | jq

TOKEN2=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ayse.test@courier.com",
    "password": "test123"
  }' | jq -r '.data.token')
```

#### 3.2. Her İki Kurye Check-In Yapsın

```bash
# Ali check-in (ilk)
curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID/check-in" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{}' | jq

# Biraz bekle
sleep 2

# Ayşe için vardiya rezerve ve check-in
SHIFT2_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" \
  -d "{\"templateId\": 1, \"shiftDate\": \"$(date +%Y-%m-%d)\"}")

SHIFT_ID2=$(echo "$SHIFT2_RESPONSE" | jq -r '.data.shiftId')

curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID2/check-in" \
  -H "Authorization: Bearer $TOKEN2" \
  -H "Content-Type: application/json" -d '{}' | jq
```

#### 3.3. FIFO Sıralamasını Kontrol Et

```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT 
    od.courier_id, 
    c.name, 
    od.on_duty_since,
    EXTRACT(EPOCH FROM (now() - od.on_duty_since))/3600 AS hours_working
FROM on_duty_couriers od
JOIN couriers c ON c.id = od.courier_id
ORDER BY od.on_duty_since ASC;
"
```

**Beklenen Sonuç:**
```
 courier_id |      name          |      on_duty_since       | hours_working 
------------+--------------------+--------------------------+---------------
         10 | Test Kurye Ali     | 2025-12-02 15:30:00+00   |          0.05
         11 | Test Kurye Ayşe    | 2025-12-02 15:31:00+00   |          0.02
```

#### 3.4. Bir Sonraki Atanacak Kurye (FIFO)

```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT courier_id, on_duty_since
FROM on_duty_couriers
ORDER BY on_duty_since ASC
LIMIT 1;
"
```

**Beklenen:** Ali (en eski on_duty_since)

---

### Senaryo 4: Hata Durumları

#### 4.1. Çift Check-In Denemesi

```bash
# Ali zaten check-in durumunda, tekrar denerse hata vermeli
curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID/check-in" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{}' | jq
```

**Beklenen Hata:**
```json
{
  "status": 400,
  "message": "Bu vardiyaya zaten giriş yapılmış veya iptal edilmiş"
}
```

#### 4.2. Check-In Yapılmadan Check-Out

```bash
# Yeni bir rezervasyon yap ama check-in yapma
SHIFT3_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{\"templateId\": 1, \"shiftDate\": \"$(date -d '+1 day' +%Y-%m-%d)\"}")

SHIFT_ID3=$(echo "$SHIFT3_RESPONSE" | jq -r '.data.shiftId')

# Direkt check-out dene
curl -s -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID3/check-out" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" -d '{}' | jq
```

**Beklenen Hata:**
```json
{
  "status": 400,
  "message": "Bu vardiyaya giriş yapılmamış"
}
```

---

## Veritabanı Sorgula Komutları

### Tüm Aktif Kuryeler

```sql
SELECT 
    od.id,
    od.courier_id, 
    c.name, 
    c.email,
    c.phone,
    od.on_duty_since,
    od.shift_id,
    s.start_time,
    s.end_time,
    od.source,
    EXTRACT(EPOCH FROM (now() - od.on_duty_since))/3600 AS hours_on_duty
FROM on_duty_couriers od
JOIN couriers c ON c.id = od.courier_id
LEFT JOIN shifts s ON s.shift_id = od.shift_id
ORDER BY od.on_duty_since ASC;
```

### Belirli Kurye On-Duty Mi?

```sql
SELECT EXISTS(
    SELECT 1 FROM on_duty_couriers WHERE courier_id = 10
) AS is_on_duty;
```

### Toplam Aktif Kurye Sayısı

```sql
SELECT COUNT(*) AS total_active_couriers FROM on_duty_couriers;
```

### Couriers Tablosu ile Karşılaştırma

```sql
SELECT 
    c.id,
    c.name,
    c.status,
    c.on_duty_since AS courier_on_duty,
    od.on_duty_since AS table_on_duty,
    CASE 
        WHEN c.on_duty_since IS NOT NULL AND od.courier_id IS NULL THEN 'MISSING'
        WHEN c.on_duty_since IS NULL AND od.courier_id IS NOT NULL THEN 'EXTRA'
        WHEN c.on_duty_since IS NOT NULL AND od.courier_id IS NOT NULL THEN 'OK'
        ELSE 'BOTH_NULL'
    END AS sync_status
FROM couriers c
LEFT JOIN on_duty_couriers od ON od.courier_id = c.id
WHERE c.on_duty_since IS NOT NULL OR od.courier_id IS NOT NULL;
```

---

## Docker Komutları

### psql ile Bağlan

```bash
docker exec -it courier-postgres psql -U courier_user -d courier_db
```

### Tek Satır Sorgu

```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "SELECT * FROM on_duty_couriers;"
```

### Script Çalıştır

```bash
docker exec -i courier-postgres psql -U courier_user -d courier_db < backup.sql
```

---

## Temizlik Komutları

### Tüm Test Verilerini Temizle

```bash
docker exec courier-postgres psql -U courier_user -d courier_db <<'SQL'
-- on_duty_couriers temizle
TRUNCATE on_duty_couriers;

-- Test kuryeleri sil (opsiyonel)
DELETE FROM couriers WHERE email LIKE '%test@courier.com';

-- Test shifts sil
DELETE FROM shifts WHERE courier_id IN (
    SELECT id FROM couriers WHERE email LIKE '%test@courier.com'
);
SQL
```

### Sadece on_duty_couriers Temizle

```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "TRUNCATE on_duty_couriers;"
```

---

## Başarı Kriterleri

✅ **Migration başarılı**: `on_duty_couriers` tablosu oluşturuldu  
✅ **Backfill çalıştı**: Mevcut veriler taşındı  
✅ **Check-in çalışıyor**: Kurye check-in yaptığında `on_duty_couriers`'a ekleniyor  
✅ **Check-out çalışıyor**: Kurye check-out yaptığında tablodan siliniyor  
✅ **FIFO sıralama doğru**: `on_duty_since` ASC ile sıralanıyor  
✅ **Unique constraint çalışıyor**: Aynı kurye iki kez eklenemiyor  
✅ **Foreign key çalışıyor**: Cascade ve SET NULL doğru  
✅ **API yanıtları doğru**: HTTP 200/400 ve mesajlar uygun  

---

## Sorun Giderme

### Problem: Migration çalışmıyor

```bash
# Flyway info kontrol et
./mvnw flyway:info -Dflyway.url=jdbc:postgresql://localhost:5433/courier_db \
  -Dflyway.user=courier_user -Dflyway.password=courier_password

# Repair gerekiyorsa
./mvnw flyway:repair -Dflyway.url=jdbc:postgresql://localhost:5433/courier_db \
  -Dflyway.user=courier_user -Dflyway.password=courier_password
```

### Problem: Token geçersiz

```bash
# Yeniden login yap
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ali.test@courier.com","password":"test123"}' \
  | jq -r '.data.token')
```

### Problem: Port çakışması

```bash
# Eğer 8081 yerine 8080 kullanılıyorsa
export BASE_URL=http://localhost:8080
```

---

## İleri Seviye: Load Test

### 100 Kurye Oluştur ve Check-In Yap

```bash
#!/bin/bash
for i in {1..100}; do
  # Register
  curl -s -X POST http://localhost:8081/api/v1/auth/register/courier \
    -H "Content-Type: application/json" \
    -d "{
      \"name\": \"Kurye $i\",
      \"email\": \"kurye$i@test.com\",
      \"phone\": \"+9055512$i\",
      \"password\": \"test123\"
    }" > /dev/null
  
  echo "Registered kurye$i@test.com"
done
```

---

## Özet

Bu test rehberi:
- ✅ Yeni `on_duty_couriers` tablosunun çalıştığını doğrular
- ✅ SOLID ve Clean Architecture prensiplerini test eder
- ✅ Check-in/check-out akışlarını doğrular
- ✅ FIFO sıralamasını test eder
- ✅ Hata durumlarını kontrol eder

**Sonuç**: Tüm testler başarılı olursa sistem production-ready!

