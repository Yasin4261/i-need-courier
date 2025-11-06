# Kullanıcı Türüne Göre Giriş Rehberi

## 🔐 Unified Login Sistemi

Projenizde **tek bir endpoint** ile **tüm kullanıcı türleri** giriş yapabilir.

### Endpoint:
```
POST http://localhost:8081/api/v1/auth/login
```

### Request Body:
```json
{
  "email": "kullanıcı_email",
  "password": "şifre"
}
```

## 📋 Test Kullanıcıları

### 1️⃣ BUSINESS Kullanıcıları (İşletme)

#### Yeni Pizza Restaurant
```json
{
  "email": "yeni@pizza.com",
  "password": "password123"
}
```
**Status:** ACTIVE ✅  
**User Type:** BUSINESS

#### Veritabanındaki Diğer İşletmeler
Veritabanında daha fazla işletme var ama hepsi `PENDING` statüsünde.  
Login yapabilmek için önce statülerini `ACTIVE` yapmanız gerekir:
```bash
docker exec -it courier-postgres psql -U courier_user -d courier_db -c "UPDATE businesses SET status = 'ACTIVE' WHERE email = 'ORDER_EMAIL_HERE';"
```

### 2️⃣ COURIER Kullanıcıları (Kurye)

#### Ahmet Yılmaz (Motorcycle)
```json
{
  "email": "ahmet@courier.com",
  "password": "password123"
}
```
**Status:** ONLINE ✅  
**User Type:** COURIER  
**Vehicle:** MOTORCYCLE

## 🧪 Test Adımları

### ✅ Test 1: Business Login (Postman/curl)

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "yeni@pizza.com",
    "password": "password123"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 10,
    "email": "yeni@pizza.com",
    "name": "Yeni Pizza Restaurant",
    "userType": "BUSINESS",
    "status": "ACTIVE",
    "message": "Login successful"
  },
  "message": "Login successful"
}
```

### ✅ Test 2: Courier Login

```bash
curl -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmet@courier.com",
    "password": "password123"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "userId": 4,
    "email": "ahmet@courier.com",
    "name": "Ahmet Yılmaz",
    "userType": "COURIER",
    "status": "ONLINE",
    "message": "Login successful"
  },
  "message": "Login successful"
}
```

### 📝 Test 3: Yeni Kullanıcı Kayıt

### 📝 Test 3: Yeni Kullanıcı Kayıt

#### Yeni Courier Kaydı
```bash
curl -X POST http://localhost:8081/api/v1/auth/register/courier \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Mehmet Demir",
    "email": "mehmet@courier.com",
    "password": "password123",
    "phone": "+905551234568",
    "vehicleType": "BICYCLE"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "courierId": 5,
    "name": "Mehmet Demir",
    "email": "mehmet@courier.com",
    "message": "Registration successful"
  },
  "message": "Courier registration successful"
}
```

#### Yeni Business Kaydı
```bash
curl -X POST http://localhost:8081/api/v1/auth/register/business \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Burger House",
    "email": "info@burgerhouse.com",
    "password": "password123",
    "phone": "+905559999998",
    "address": "Beşiktaş, İstanbul",
    "contactPerson": "Ali Yılmaz",
    "businessType": "Fast Food"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "businessId": 11,
    "name": "Burger House",
    "email": "info@burgerhouse.com",
    "status": "PENDING",
    "message": "Registration successful. Pending approval."
  },
  "message": "Business registration successful. Pending approval."
}
```

> ⚠️ **Not:** Business kaydından sonra login yapabilmek için statüyü ACTIVE yapmanız gerekir:
> ```bash
> docker exec -it courier-postgres psql -U courier_user -d courier_db -c "UPDATE businesses SET status = 'ACTIVE' WHERE email = 'info@burgerhouse.com';"
> ```

## 🎯 Nasıl Çalışıyor?

1. **Email Kontrolü:** Sistem girilen email'i hem `couriers` hem de `businesses` tablosunda arar
2. **Otomatik Tespit:** Hangi tabloda bulursa, o kullanıcı türü için işlem yapar
3. **JWT Token:** Token içinde kullanıcı türü (`userType`) bilgisi de bulunur
4. **Response:** Dönen cevapta kullanıcı türü (`COURIER` veya `BUSINESS`) belirtilir

## 🔍 JWT Token Kullanımı

Login sonrası aldığınız token'ı koruma altındaki endpointlerde kullanabilirsiniz:

```bash
curl -X GET http://localhost:8081/api/v1/protected-endpoint \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

## 📊 Swagger UI

Tarayıcınızda test için:
```
http://localhost:8081/swagger-ui.html
```

## ⚠️ Önemli Notlar

- Tüm test business kullanıcılarının şifresi: `password123`
- Business kullanıcıları `ACTIVE` statüsünde olmalı
- Courier kullanıcıları `AVAILABLE` statüsünde olmalı
- Kullanıcı bulunamazsa veya şifre yanlışsa: `401 Unauthorized`
- JWT token varsayılan olarak 24 saat geçerlidir

## 🐛 Hata Durumları

### Invalid Credentials
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

### Account Not Active
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Your business account is INACTIVE",
  "path": "/api/v1/auth/login"
}
```

