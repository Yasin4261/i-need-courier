# Kurye Vardiya Sistemi Test Rehberi

## 📋 Vardiya Sistemi Özellikleri

Kuryeler artık vardiya rezerve edebilir, vardiyaya giriş/çıkış yapabilir ve vardiyalarını yönetebilir.

### 🎯 Özellikler
- ✅ Vardiya şablonlarını görüntüleme
- ✅ Vardiya rezerve etme
- ✅ Vardiyaya check-in (giriş) yapma
- ✅ Vardiyadan check-out (çıkış) yapma
- ✅ Vardiya rezervasyonunu iptal etme
- ✅ Aktif ve gelecek vardiyaları görüntüleme

---

## 🔐 1. Kurye Girişi

Önce bir kurye girişi yapmanız gerekiyor:

```bash
# Kurye Login
curl -X POST http://localhost:8080/api/v1/auth/courier/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.yilmaz@example.com",
    "password": "password123"
  }'
```

**Response:**
```json
{
  "token": "eyJhbGciOiJIUzM4NCJ9...",
  "type": "Bearer",
  "courierId": 1,
  "email": "ahmet.yilmaz@example.com",
  "name": "Ahmet Yılmaz"
}
```

**Token'ı kaydedin!** Tüm vardiya işlemlerinde bu token'ı kullanacağız.

---

## 📅 2. Vardiya Şablonlarını Görüntüle

Sistemdeki mevcut vardiya şablonlarını listeleyin:

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."  # Login'den aldığınız token

curl -X GET http://localhost:8080/api/v1/courier/shifts/templates \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Vardiya şablonları başarıyla getirildi",
  "data": [
    {
      "templateId": 1,
      "name": "Sabah Vardiyası",
      "description": "Sabah teslimatları için erken vardiya",
      "startTime": "09:00:00",
      "endTime": "17:00:00",
      "defaultRole": "COURIER",
      "maxCouriers": 15,
      "isActive": true
    },
    {
      "templateId": 2,
      "name": "Akşam Vardiyası",
      "description": "Öğleden sonra ve akşam teslimatları",
      "startTime": "14:00:00",
      "endTime": "22:00:00",
      "defaultRole": "COURIER",
      "maxCouriers": 12,
      "isActive": true
    }
  ]
}
```

---

## 🗓️ 3. Vardiya Rezerve Et

Gelecek bir tarih için vardiya rezerve edin:

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

# Yarın için sabah vardiyası rezerve et
curl -X POST http://localhost:8080/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "templateId": 1,
    "shiftDate": "2025-11-13",
    "notes": "İlk vardiyam"
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Vardiya başarıyla rezerve edildi",
  "data": {
    "shiftId": 1,
    "courierId": 1,
    "courierName": "Ahmet Yılmaz",
    "startTime": "2025-11-13T09:00:00",
    "endTime": "2025-11-13T17:00:00",
    "shiftRole": "COURIER",
    "status": "RESERVED",
    "checkInTime": null,
    "checkOutTime": null,
    "notes": "İlk vardiyam"
  }
}
```

---

## 👁️ 4. Gelecek Vardiyalarımı Görüntüle

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

curl -X GET http://localhost:8080/api/v1/courier/shifts/upcoming \
  -H "Authorization: Bearer $TOKEN"
```

---

## ✅ 5. Vardiyaya Check-In (Giriş) Yap

Vardiya zamanı geldiğinde (başlangıçtan 30 dakika öncesinden itibaren) giriş yapabilirsiniz:

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."
SHIFT_ID=1  # Rezerve ettiğiniz vardiya ID'si

curl -X POST http://localhost:8080/api/v1/courier/shifts/$SHIFT_ID/check-in \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Vardiyaya başladım",
    "latitude": 41.0082,
    "longitude": 28.9784
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Vardiyaya giriş başarılı",
  "data": {
    "shiftId": 1,
    "courierId": 1,
    "courierName": "Ahmet Yılmaz",
    "startTime": "2025-11-13T09:00:00",
    "endTime": "2025-11-13T17:00:00",
    "shiftRole": "COURIER",
    "status": "CHECKED_IN",
    "checkInTime": "2025-11-13T08:55:23",
    "checkOutTime": null
  }
}
```

**Not:** Check-in yaptığınızda:
- Vardiya durumu `CHECKED_IN` olur
- Courier durumu `ONLINE` olur
- `on_duty_since` alanı set edilir (sıra tabanlı atama için)

---

## 🔍 6. Aktif Vardiyamı Görüntüle

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

curl -X GET http://localhost:8080/api/v1/courier/shifts/active \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🚪 7. Vardiyadan Check-Out (Çıkış) Yap

Vardiyayı bitirdiğinizde çıkış yapın:

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."
SHIFT_ID=1

curl -X POST http://localhost:8080/api/v1/courier/shifts/$SHIFT_ID/check-out \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "notes": "Vardiya tamamlandı",
    "latitude": 41.0082,
    "longitude": 28.9784
  }'
```

**Response:**
```json
{
  "success": true,
  "message": "Vardiyadan çıkış başarılı",
  "data": {
    "shiftId": 1,
    "status": "CHECKED_OUT",
    "checkInTime": "2025-11-13T08:55:23",
    "checkOutTime": "2025-11-13T17:05:12"
  }
}
```

**Not:** Check-out yaptığınızda:
- Vardiya durumu `CHECKED_OUT` olur
- Courier durumu `OFFLINE` olur
- `on_duty_since` alanı temizlenir

---

## ❌ 8. Vardiya Rezervasyonunu İptal Et

Henüz başlamamış bir vardiyayı iptal edebilirsiniz (başlangıçtan en az 2 saat önce):

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."
SHIFT_ID=2  # İptal etmek istediğiniz vardiya ID'si

curl -X DELETE http://localhost:8080/api/v1/courier/shifts/$SHIFT_ID/cancel \
  -H "Authorization: Bearer $TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Vardiya rezervasyonu iptal edildi",
  "data": null
}
```

---

## 📊 9. Tüm Vardiyalarımı Görüntüle

```bash
TOKEN="eyJhbGciOiJIUzM4NCJ9..."

# Tüm vardiyalar
curl -X GET http://localhost:8080/api/v1/courier/shifts/my-shifts \
  -H "Authorization: Bearer $TOKEN"

# Sadece aktif vardiyalar
curl -X GET "http://localhost:8080/api/v1/courier/shifts/my-shifts?status=CHECKED_IN" \
  -H "Authorization: Bearer $TOKEN"

# Sadece tamamlanan vardiyalar
curl -X GET "http://localhost:8080/api/v1/courier/shifts/my-shifts?status=CHECKED_OUT" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 🎬 Komple Test Senaryosu

```bash
#!/bin/bash

# 1. Login
echo "1. Kurye girişi yapılıyor..."
LOGIN_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/auth/courier/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet.yilmaz@example.com",
    "password": "password123"
  }')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.token')
echo "Token alındı: ${TOKEN:0:20}..."

# 2. Vardiya şablonlarını listele
echo -e "\n2. Vardiya şablonları listeleniyor..."
curl -s -X GET http://localhost:8080/api/v1/courier/shifts/templates \
  -H "Authorization: Bearer $TOKEN" | jq

# 3. Yarın için vardiya rezerve et
echo -e "\n3. Yarın için vardiya rezerve ediliyor..."
TOMORROW=$(date -d "+1 day" +%Y-%m-%d)
RESERVE_RESPONSE=$(curl -s -X POST http://localhost:8080/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": 1,
    \"shiftDate\": \"$TOMORROW\",
    \"notes\": \"Test vardiyası\"
  }")

echo $RESERVE_RESPONSE | jq
SHIFT_ID=$(echo $RESERVE_RESPONSE | jq -r '.data.shiftId')
echo "Vardiya ID: $SHIFT_ID"

# 4. Gelecek vardiyaları görüntüle
echo -e "\n4. Gelecek vardiyalar listeleniyor..."
curl -s -X GET http://localhost:8080/api/v1/courier/shifts/upcoming \
  -H "Authorization: Bearer $TOKEN" | jq

echo -e "\n✅ Test tamamlandı!"
```

---

## ⚠️ Önemli Kurallar

### Check-In Kuralları:
- ✅ Vardiya başlangıcından 30 dakika öncesinden itibaren check-in yapılabilir
- ❌ Sadece `RESERVED` durumundaki vardiyalara check-in yapılabilir
- ❌ Zaman çakışması varsa check-in yapılamaz

### Check-Out Kuralları:
- ✅ Sadece `CHECKED_IN` durumundaki vardiyalardan check-out yapılabilir
- ✅ İstediğiniz zaman (erken veya geç) check-out yapabilirsiniz

### İptal Kuralları:
- ✅ Sadece `RESERVED` durumundaki vardiyalar iptal edilebilir
- ❌ Vardiya başlangıcına 2 saatten az kaldıysa iptal edilemez

### Rezervasyon Kuralları:
- ❌ Geçmiş tarihli vardiya rezerve edilemez
- ❌ Aynı zaman diliminde birden fazla vardiya rezerve edilemez

---

## 🗄️ Veritabanı Yapısı

### `shifts` Tablosu
- `shift_id`: Vardiya ID
- `courier_id`: Kurye ID
- `start_time`: Planlanan başlangıç
- `end_time`: Planlanan bitiş
- `shift_role`: COURIER | CAPTAIN
- `status`: RESERVED | CHECKED_IN | CHECKED_OUT | CANCELLED | NO_SHOW
- `check_in_time`: Gerçek giriş zamanı
- `check_out_time`: Gerçek çıkış zamanı

### `shift_templates` Tablosu
- `template_id`: Şablon ID
- `name`: Vardiya adı
- `start_time`: Başlangıç saati (TIME)
- `end_time`: Bitiş saati (TIME)
- `default_role`: Varsayılan rol
- `max_couriers`: Maksimum kurye sayısı

---

## 🔄 Sıra Tabanlı Atama

Vardiyaya check-in yaptığınızda:
- `couriers.on_duty_since` alanı doldurulur
- Bu alan paket atama sisteminde kullanılır (FIFO - First In First Out)
- En uzun süredir çalışan kurye önce paket alır

Check-out yaptığınızda:
- `on_duty_since` alanı temizlenir
- Kurye atama sırasından çıkar

---

## 📝 Notlar

- Token 24 saat geçerlidir
- Tüm tarihler ISO 8601 formatındadır
- Zaman dilimleri UTC+0 olarak saklanır
- Latitude/Longitude opsiyoneldir

---

**Test edildi ve çalışıyor! ✅**

