# 📋 Vardiyaya Nasıl Giriş Yapılır?

## 🎯 ADIM ADIM VARDİYA SİSTEMİ

### 1️⃣ VARDİYA ŞABLONLARINI GÖRÜNTÜLE

**Endpoint:** `GET /api/v1/courier/shifts/templates`

**cURL:**
```bash
curl -X GET http://localhost:8081/api/v1/courier/shifts/templates \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "templateId": 1,
      "name": "Sabah Vardiyası",
      "startTime": "08:00",
      "endTime": "16:00",
      "description": "Sabah vardiyası (08:00-16:00)"
    },
    {
      "templateId": 2,
      "name": "Öğle Vardiyası",
      "startTime": "12:00",
      "endTime": "20:00"
    },
    {
      "templateId": 3,
      "name": "Akşam Vardiyası",
      "startTime": "16:00",
      "endTime": "00:00"
    }
  ]
}
```

---

### 2️⃣ VARDİYA REZERVE ET

**Endpoint:** `POST /api/v1/courier/shifts/reserve`

**cURL:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "shiftDate": "2025-12-04",
    "notes": "Sabah vardiyası için hazırım"
  }'
```

**Request Body:**
```json
{
  "templateId": 1,
  "shiftDate": "2025-12-04",
  "notes": "İsteğe bağlı not"
}
```

**Response:**
```json
{
  "code": 201,
  "data": {
    "shiftId": 123,
    "courierId": 4,
    "courierName": "Yasin",
    "shiftDate": "2025-12-04",
    "startTime": "08:00",
    "endTime": "16:00",
    "status": "RESERVED",
    "notes": "Sabah vardiyası için hazırım"
  },
  "message": "Vardiya başarıyla rezerve edildi"
}
```

---

### 3️⃣ VARDİYAYA GİRİŞ YAP (CHECK-IN) ⭐

**Endpoint:** `POST /api/v1/courier/shifts/{shiftId}/check-in`

**cURL:**
```bash
# Shift ID'yi bir önceki adımdan al (örn: 123)
curl -X POST http://localhost:8081/api/v1/courier/shifts/123/check-in \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "location": "Beşiktaş Merkez",
    "notes": "Hazırım, siparişleri almaya başlayabilirim"
  }'
```

**Request Body (isteğe bağlı):**
```json
{
  "location": "Beşiktaş Merkez",
  "notes": "Check-in notu"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 123,
    "status": "CHECKED_IN",
    "checkInTime": "2025-12-04T08:05:00Z",
    "notes": "Hazırım, siparişleri almaya başlayabilirim"
  },
  "message": "Vardiyaya giriş başarılı"
}
```

**Ne Olur:**
1. Shift status → `CHECKED_IN`
2. Check-in time kaydedilir
3. **ÖNEMLİ:** `on_duty_couriers` tablosuna eklenir (FIFO queue)
4. Artık sipariş atanabilir duruma gelirsin

---

### 4️⃣ AKTİF VARDİYANI GÖRÜNTÜLE

**Endpoint:** `GET /api/v1/courier/shifts/active`

**cURL:**
```bash
curl -X GET http://localhost:8081/api/v1/courier/shifts/active \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 123,
    "status": "CHECKED_IN",
    "checkInTime": "2025-12-04T08:05:00Z",
    "shiftDate": "2025-12-04",
    "startTime": "08:00",
    "endTime": "16:00"
  },
  "message": "Aktif vardiya getirildi"
}
```

---

### 5️⃣ VARDİYADAN ÇIKIŞ YAP (CHECK-OUT)

**Endpoint:** `POST /api/v1/courier/shifts/{shiftId}/check-out`

**cURL:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/shifts/123/check-out \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Vardiya tamamlandı, iyi geçti"
  }'
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 123,
    "status": "CHECKED_OUT",
    "checkOutTime": "2025-12-04T16:10:00Z"
  },
  "message": "Vardiyadan çıkış başarılı"
}
```

**Ne Olur:**
1. Shift status → `CHECKED_OUT`
2. Check-out time kaydedilir
3. `on_duty_couriers` tablosundan silinir
4. Artık sipariş atanmaz

---

## 🔄 TAM AKIŞ DİYAGRAMI

```
1. GET /shifts/templates
   ↓ (template seç)

2. POST /shifts/reserve
   ↓ (yarın için rezerve et)
   Status: RESERVED

3. POST /shifts/{id}/check-in ⭐
   ↓ (vardiya günü geldiğinde)
   Status: CHECKED_IN
   → on_duty_couriers tablosuna eklenir
   → FIFO queue'ya dahil olur
   → Sipariş atanabilir duruma gelir

4. [Siparişleri Al ve Teslim Et]
   ↓

5. POST /shifts/{id}/check-out
   Status: CHECKED_OUT
   → on_duty_couriers'dan çıkar
```

---

## ⚡ HIZLI TEST SENARYOSU

### Senaryo: Bugün için hızlı check-in

```bash
# Token
COURIER_TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQ09VUklFUiIsInVzZXJJZCI6NCwiZW1haWwiOiJ5YXNpbjNAcGFrby5jb20iLCJzdWIiOiJ5YXNpbjNAcGFrby5jb20iLCJpYXQiOjE3NjQ3MDQxNDYsImV4cCI6MTc2NDc5MDU0Nn0.N4tQ9kwolxeGEvVfGbsm6f8XdzFP4SBT_2tgrnwdIsi2yYIXNYZM2Uh_WVu7gEM-"

# 1. Templates listele
echo "=== 1. Templates ==="
curl -s -X GET http://localhost:8081/api/v1/courier/shifts/templates \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 2. Bugün için rezerve et
echo ""
echo "=== 2. Reserve Shift ==="
RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": 1,
    \"shiftDate\": \"$(date +%Y-%m-%d)\",
    \"notes\": \"Test vardiyası\"
  }")
echo "$RESPONSE" | jq
SHIFT_ID=$(echo "$RESPONSE" | jq -r '.data.shiftId')

# 3. Check-in yap
echo ""
echo "=== 3. Check-in (Shift ID: $SHIFT_ID) ==="
curl -s -X POST http://localhost:8081/api/v1/courier/shifts/$SHIFT_ID/check-in \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Hazırım!"
  }' | jq

# 4. Aktif vardiya kontrol
echo ""
echo "=== 4. Active Shift ==="
curl -s -X GET http://localhost:8081/api/v1/courier/shifts/active \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 5. On-duty kontrolü (Database)
echo ""
echo "=== 5. On-Duty Status (Database) ==="
docker exec courier-postgres psql -U courier_user -d courier_db -c \
  "SELECT * FROM on_duty_couriers WHERE courier_id = 4;"
```

---

## 🗄️ VERİTABANI KONTROLÜ

### On-Duty Couriers Kontrol:
```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT 
    odc.id,
    odc.courier_id,
    c.name AS courier_name,
    odc.shift_id,
    odc.on_duty_since,
    odc.source,
    odc.created_at
FROM on_duty_couriers odc
JOIN couriers c ON c.id = odc.courier_id
ORDER BY odc.on_duty_since ASC;
"
```

### Shifts Kontrol:
```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT 
    s.shift_id,
    c.name AS courier_name,
    s.shift_date,
    s.status,
    s.check_in_time,
    s.check_out_time
FROM shifts s
JOIN couriers c ON c.id = s.courier_id
WHERE s.courier_id = 4
ORDER BY s.shift_date DESC
LIMIT 5;
"
```

---

## ❗ ÖNEMLİ NOTLAR

### 1. Check-in Zamanı
- Vardiya saatinden **30 dakika önce** check-in yapabilirsin
- Vardiya saatinden **1 saat sonrasına kadar** check-in yapabilirsin

### 2. On-Duty Durumu
- Check-in yaptığında otomatik olarak `on_duty_couriers` tablosuna eklenirsin
- Bu tablo **FIFO sıralama** için kullanılır
- `on_duty_since` = check-in zamanın
- En eski on-duty olan kurye önce sipariş alır

### 3. Sipariş Atama
- Sadece **CHECKED_IN** durumundaki kuryeler sipariş alabilir
- Auto-assignment FIFO bazlıdır
- Kabul ettiğin her siparişten sonra kuyruğun sonuna gidersin

### 4. Manuel On-Duty Ekleme (Test için)
```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source, created_at, updated_at) 
VALUES (4, 1, now(), 'manual', now(), now())
ON CONFLICT (courier_id) DO UPDATE SET on_duty_since=now();
"
```

---

## 🔍 SORUN GİDERME

### Problem 1: "Bu vardiyaya check-in yapamazsınız"
**Çözüm:** Shift date bugün mü kontrol et, zaman aralığı uygun mu?

### Problem 2: "Aktif vardiya yok"
**Çözüm:** Önce reserve et, sonra check-in yap

### Problem 3: "Sipariş atanmıyor"
**Çözüm:**
```bash
# On-duty kontrolü
docker exec courier-postgres psql -U courier_user -d courier_db -c \
  "SELECT * FROM on_duty_couriers WHERE courier_id = 4;"

# Eğer yoksa, check-in yap veya manuel ekle
```

---

## 📚 İLGİLİ ENDPOINT'LER

| Endpoint | HTTP | Açıklama |
|----------|------|----------|
| `/shifts/templates` | GET | Vardiya şablonlarını listele |
| `/shifts/reserve` | POST | Vardiya rezerve et |
| `/shifts/{id}/check-in` | POST | ⭐ Vardiyaya giriş yap |
| `/shifts/{id}/check-out` | POST | Vardiyadan çıkış yap |
| `/shifts/active` | GET | Aktif vardiya görüntüle |
| `/shifts/upcoming` | GET | Gelecek vardiyalar |
| `/shifts/my-shifts` | GET | Tüm vardiyalarım |

---

## 🎯 ÖZET

### Vardiyaya Giriş İçin 3 Adım:

1. **Reserve Et:**
   ```bash
   POST /shifts/reserve
   {"templateId": 1, "shiftDate": "2025-12-04"}
   ```

2. **Check-in Yap:** ⭐
   ```bash
   POST /shifts/{shiftId}/check-in
   {"notes": "Hazırım!"}
   ```

3. **On-Duty Kontrol:**
   ```sql
   SELECT * FROM on_duty_couriers WHERE courier_id = 4;
   ```

✅ **Check-in yaptığında otomatik olarak sipariş almaya hazır olursun!**

---

**Last Updated:** December 3, 2025  
**Status:** ✅ Production Ready  
**Test:** Shift system fully operational

