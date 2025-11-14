# i-need-courier API Testing Guide

Bu klasörde projemizin API endpoint'lerini test etmek için Postman collection'ları bulunmaktadır.

## 📋 İçindekiler

- [Kurulum](#kurulum)
- [Base URL](#base-url)
- [Authentication](#authentication)
- [Hata Yönetimi](#hata-yönetimi)
- [API Endpoint Kategorileri](#api-endpoint-kategorileri)
- [Test Senaryoları](#test-senaryoları)

---

## 🚀 Kurulum

### 1. Postman Collection'ları İçe Aktar

```bash
# Postman'de File > Import menüsünden şu dosyaları import edin:
- Shift_Management_API.postman_collection.json
- Business_Orders_API.postman_collection.json (varsa)
```

### 2. Environment Ayarları

Development.postman_environment.json ve Production.postman_environment.json dosyalarını import edin.

**Development Environment:**
```json
{
  "base_url": "http://localhost:8081",
  "db_host": "localhost:5433"
}
```

**Production Environment:**
```json
{
  "base_url": "https://api.yourproduction.com",
  "db_host": "production-db-host"
}
```

---

## 🌐 Base URL

- **Local Development (Docker Compose):** `http://localhost:8081`
- **Local Development (Direct Run):** `http://localhost:8080`
- **Production:** `https://api.yourproduction.com` (değiştirin)

---

## 🔐 Authentication

### Unified Login (Önerilen)

Tüm kullanıcı tipleri için tek endpoint:

**Endpoint:** `POST /api/v1/auth/login`

**Request Body:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Başarılı Response (200 OK):**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 6,
    "email": "yasin3@pako.com",
    "name": "Yasin Kurye",
    "userType": "COURIER",
    "status": "ONLINE",
    "message": "Login successful"
  },
  "message": "Login successful"
}
```

**Hatalı Credentials (401 Unauthorized):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login",
  "timestamp": "2025-11-14T01:10:06.853761547",
  "validationErrors": []
}
```

### Token Kullanımı

Login sonrası aldığınız token'ı tüm korumalı endpoint'lerde şu şekilde kullanın:

**Header:**
```
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

**Postman'de:**
1. Authorization tab'ine gidin
2. Type: Bearer Token seçin
3. Token field'ına token'ınızı yapıştırın

---

## ⚠️ Hata Yönetimi

### HTTP Status Code'ları

| Status | Açıklama | Ne Zaman Döner |
|--------|----------|----------------|
| 200 | OK | İstek başarılı |
| 201 | Created | Yeni kaynak oluşturuldu |
| 400 | Bad Request | Geçersiz request (validation hatası, malformed JSON, vb.) |
| 401 | Unauthorized | Kimlik doğrulama başarısız (geçersiz token/credentials) |
| 403 | Forbidden | Yetki yok (doğru giriş yapmış ama erişim izni yok) |
| 404 | Not Found | Kaynak bulunamadı |
| 405 | Method Not Allowed | Yanlış HTTP method (örn: GET yerine POST) |
| 409 | Conflict | Çakışma (örn: email zaten kayıtlı) |
| 415 | Unsupported Media Type | Content-Type eksik veya yanlış |
| 500 | Internal Server Error | Sunucu hatası |

### Standart Hata Response Formatı

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Detaylı hata mesajı",
  "path": "/api/v1/endpoint",
  "timestamp": "2025-11-14T01:10:06.853761547",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid"
    },
    {
      "field": "password",
      "message": "Password is required"
    }
  ]
}
```

### Yaygın Hatalar ve Çözümleri

#### 1. Malformed JSON (400)

**Hata:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid JSON syntax. Check for missing quotes, commas, or brackets.",
  "path": "/api/v1/auth/login"
}
```

**Sebep:**
- JSON syntax hatası (eksik virgül, tırnak, parantez)
- JSON içinde yorum satırı (`//` kullanımı - JSON'da yorum olamaz!)

**Çözüm:**
```json
// ❌ YANLIŞ
{
  //"username": "test",  // yorum olamaz
  "email": "test@test.com"
  "password": "123"      // virgül eksik
}

// ✅ DOĞRU
{
  "email": "test@test.com",
  "password": "123"
}
```

#### 2. Missing Content-Type (415)

**Hata:**
```json
{
  "status": 415,
  "error": "Unsupported Media Type",
  "message": "Content type 'null' is not supported. Supported types: application/json"
}
```

**Çözüm:**
Header'a ekleyin:
```
Content-Type: application/json
```

#### 3. Wrong HTTP Method (405)

**Hata:**
```json
{
  "status": 405,
  "error": "Method Not Allowed",
  "message": "HTTP method 'GET' is not supported for this endpoint. Supported methods: POST"
}
```

**Çözüm:**
Doğru HTTP method kullanın (dokümantasyonu kontrol edin).

#### 4. Validation Errors (400)

**Hata:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid"
    },
    {
      "field": "phone",
      "message": "Phone number must be valid"
    }
  ]
}
```

**Çözüm:**
- Email formatını kontrol edin: `user@domain.com`
- Telefon formatı: `+905551234567` (uluslararası format)
- Zorunlu alanları doldurun

#### 5. Token Errors (401)

**Hata:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password"
}
```

**Çözüm:**
- Token'ı doğru kopyaladığınızdan emin olun
- Token süresi dolmuş olabilir (24 saat geçerli) - yeniden login yapın
- Authorization header formatı: `Bearer <token>`

---

## 📚 API Endpoint Kategorileri

### 1. Authentication & Registration

#### Courier Registration
```http
POST /api/v1/auth/register/courier
Content-Type: application/json

{
  "name": "Ahmet Yılmaz",
  "email": "ahmet@courier.com",
  "phone": "+905551234567",
  "password": "password123"
}
```

**Başarılı Response (200 OK):**
```json
{
  "code": 200,
  "data": {
    "courierId": 1,
    "name": "Ahmet Yılmaz",
    "email": "ahmet@courier.com",
    "message": "Registration successful"
  },
  "message": "Courier registration successful"
}
```

**Hata: Email zaten kayıtlı (409 Conflict):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Courier already exists with email: ahmet@courier.com"
}
```

#### Business Registration
```http
POST /api/v1/auth/register/business
Content-Type: application/json

{
  "name": "Acme Restaurant",
  "email": "acme@restaurant.com",
  "phone": "+905559876543",
  "password": "secure123",
  "address": "Kadıköy, İstanbul",
  "contactPerson": "Mehmet Yılmaz",
  "businessType": "Restaurant"
}
```

**Başarılı Response (200 OK):**
```json
{
  "code": 200,
  "data": {
    "id": 1,
    "name": "Acme Restaurant",
    "email": "acme@restaurant.com",
    "status": "PENDING",
    "message": "Registration successful. Pending approval."
  },
  "message": "Business registration successful. Pending approval."
}
```

#### Unified Login
```http
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "ahmet@courier.com",
  "password": "password123"
}
```

---

### 2. Shift Management (Vardiya Yönetimi)

Tüm shift endpoint'leri için **Authorization header gereklidir**.

#### Get Shift Templates
```http
GET /api/v1/courier/shifts/templates
Authorization: Bearer <token>
```

**Başarılı Response (200 OK):**
```json
{
  "code": 200,
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
    }
  ],
  "message": "Vardiya şablonları başarıyla getirildi"
}
```

#### Reserve Shift
```http
POST /api/v1/courier/shifts/reserve
Authorization: Bearer <token>
Content-Type: application/json

{
  "templateId": 1,
  "shiftDate": "2025-11-15",
  "notes": "İlk vardiyam"
}
```

**Başarılı Response (201 Created):**
```json
{
  "code": 200,
  "data": {
    "shiftId": 1,
    "courierId": 6,
    "courierName": "Yasin Kurye",
    "startTime": "2025-11-15T09:00:00",
    "endTime": "2025-11-15T17:00:00",
    "shiftRole": "COURIER",
    "status": "RESERVED",
    "checkInTime": null,
    "checkOutTime": null,
    "notes": "İlk vardiyam",
    "createdAt": "2025-11-14T04:30:00"
  },
  "message": "Vardiya başarıyla rezerve edildi"
}
```

**Hata: Zaman çakışması (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Bu zaman aralığında zaten bir vardiya rezervasyonunuz var"
}
```

**Hata: Geçmiş tarih (400 Bad Request):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Geçmiş tarihli vardiya rezerve edilemez"
}
```

---

## 🧪 Test Senaryoları

### Senaryo 1: Courier Kaydı ve İlk Vardiya

1. **Courier kaydı yap** → `POST /api/v1/auth/register/courier`
2. **Login ol ve token al** → `POST /api/v1/auth/login`
3. **Vardiya şablonlarını listele** → `GET /api/v1/courier/shifts/templates`
4. **Yarın için vardiya rezerve et** → `POST /api/v1/courier/shifts/reserve`
5. **Gelecek vardiyalarımı kontrol et** → `GET /api/v1/courier/shifts/upcoming`

### Senaryo 2: Vardiya Check-In/Out

1. **Aktif vardiyamı kontrol et** → `GET /api/v1/courier/shifts/active`
2. **Vardiyaya giriş yap (check-in)** → `POST /api/v1/courier/shifts/{shiftId}/check-in`
3. **Vardiyadan çıkış yap (check-out)** → `POST /api/v1/courier/shifts/{shiftId}/check-out`

---

## 🐛 Debugging İpuçları

### 1. Backend Loglarını İnceleyin

```bash
# Docker Compose kullanıyorsanız
docker compose logs -f backend
```

### 2. Veritabanını Kontrol Edin

```bash
# PostgreSQL'e bağlan
docker compose exec postgres psql -U courier_user -d courier_db

# Courier'leri listele
SELECT id, email, name, status FROM couriers;
```

---

**Son Güncelleme:** 14 Kasım 2025  
**API Version:** v1

