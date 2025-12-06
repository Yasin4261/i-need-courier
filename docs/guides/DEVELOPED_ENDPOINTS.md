# 🚀 Bu Branch'te Geliştirilen Endpoint'ler

**Branch:** main  
**Feature:** Complete Order Assignment & Delivery System  
**Date:** December 3, 2025

---

## 📊 YENİ EKLENEN ENDPOINT'LER

### 1️⃣ COURIER ASSIGNMENT ENDPOINTS (Yeni)

#### 📋 `/api/v1/courier/assignments/pending` - GET
**Amaç:** Kurye'nin bekleyen atamalarını listeler  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "assignmentId": 123,
      "orderId": 456,
      "assignedAt": "2025-12-03T10:00:00Z",
      "timeoutAt": "2025-12-03T10:02:00Z",
      "status": "PENDING"
    }
  ]
}
```

**Özellikler:**
- FIFO sıralaması
- Timeout bilgisi
- Real-time WebSocket bildirimi ile birlikte çalışır

---

#### ✅ `/api/v1/courier/assignments/{assignmentId}/accept` - POST
**Amaç:** Kurye atamayı kabul eder  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Request Body:** `{}` (boş)  
**Response:**
```json
{
  "code": 200,
  "message": "Sipariş kabul edildi"
}
```

**Ne Yapar:**
1. Assignment status → `ACCEPTED`
2. Order status → `ASSIGNED`
3. Kurye FIFO kuyruğunun sonuna taşınır
4. Business'e WebSocket bildirimi gönderilir

---

#### ❌ `/api/v1/courier/assignments/{assignmentId}/reject` - POST
**Amaç:** Kurye atamayı reddeder  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Request Body:**
```json
{
  "reason": "Araç arızalı, teslimat yapamıyorum"
}
```

**Response:**
```json
{
  "code": 200,
  "message": "Sipariş reddedildi, başka kuryeye atanıyor"
}
```

**Ne Yapar:**
1. Assignment status → `REJECTED`
2. Rejection reason kaydedilir
3. Otomatik olarak bir sonraki kuryeye atar (REASSIGNMENT)
4. WebSocket bildirimleri gönderilir

---

### 2️⃣ COURIER ORDER OPERATIONS ENDPOINTS (Yeni)

#### 👁️ `/api/v1/courier/orders/{orderId}` - GET
**Amaç:** Sipariş detaylarını görüntüler  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 456,
    "orderNumber": "ORD-20251203-001",
    "status": "ASSIGNED",
    "pickupAddress": "Restaurant A",
    "deliveryAddress": "Customer B",
    "packageDescription": "Pizza",
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }
}
```

**Güvenlik:** Sadece atanmış kurye görebilir

---

#### 📦 `/api/v1/courier/orders/{orderId}/pickup` - POST
**Amaç:** Paketi alır (PICKED_UP)  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Request Body:**
```json
{
  "notes": "2 adet pizza kutusu alındı, sıcak tutuluyor"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 456,
    "status": "PICKED_UP",
    "courierNotes": "2 adet pizza kutusu alındı..."
  },
  "message": "Sipariş alındı (PICKED_UP)"
}
```

**Validasyon:**
- Order status = `ASSIGNED` olmalı
- Kurye bu siparişe atanmış olmalı

---

#### 🚗 `/api/v1/courier/orders/{orderId}/start-delivery` - POST
**Amaç:** Teslimatı başlatır (IN_TRANSIT)  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Request Body:** Yok  
**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 456,
    "status": "IN_TRANSIT"
  },
  "message": "Teslimat başladı (IN_TRANSIT)"
}
```

**Validasyon:**
- Order status = `PICKED_UP` olmalı
- Kurye bu siparişe atanmış olmalı

---

#### ✅ `/api/v1/courier/orders/{orderId}/complete` - POST
**Amaç:** Teslimatı tamamlar (DELIVERED)  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Request Body:**
```json
{
  "notes": "Müşteriye teslim edildi, 50 TL nakit tahsil edildi",
  "collectionAmount": 50.00
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 456,
    "status": "DELIVERED",
    "courierNotes": "Müşteriye teslim edildi...",
    "collectionAmount": 50.00
  },
  "message": "Sipariş teslim edildi (DELIVERED)"
}
```

**Validasyon:**
- Order status = `IN_TRANSIT` olmalı
- Kurye bu siparişe atanmış olmalı
- Collection amount BigDecimal olarak kaydedilir

---

### 3️⃣ SHIFT MANAGEMENT ENDPOINTS (Mevcut - Referans)

#### 📋 `/api/v1/courier/shifts/templates` - GET
**Amaç:** Vardiya şablonlarını listeler  
**Kimlik Doğrulama:** Bearer Token (Courier)  
**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "templateId": 1,
      "name": "Sabah Vardiyası",
      "startTime": "08:00",
      "endTime": "16:00"
    }
  ]
}
```

---

#### 📝 `/api/v1/courier/shifts/reserve` - POST
**Amaç:** Vardiya rezerve et  
**Request Body:**
```json
{
  "templateId": 1,
  "shiftDate": "2025-12-04",
  "notes": "İsteğe bağlı"
}
```

---

#### 🚪 `/api/v1/courier/shifts/{shiftId}/check-in` - POST ⭐
**Amaç:** Vardiyaya giriş yap (FIFO queue'ya dahil ol)  
**Request Body (isteğe bağlı):**
```json
{
  "location": "Beşiktaş Merkez",
  "notes": "Hazırım!"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "shiftId": 123,
    "status": "CHECKED_IN",
    "checkInTime": "2025-12-04T08:05:00Z"
  },
  "message": "Vardiyaya giriş başarılı"
}
```

**ÖNEMLİ:** Check-in yaptığında:
1. Shift status → `CHECKED_IN`
2. `on_duty_couriers` tablosuna eklenir
3. FIFO queue'ya dahil olur
4. Sipariş atanabilir duruma gelir

---

#### 🚪 `/api/v1/courier/shifts/{shiftId}/check-out` - POST
**Amaç:** Vardiyadan çıkış yap  
**Ne Yapar:**
1. Shift status → `CHECKED_OUT`
2. `on_duty_couriers` tablosundan silinir
3. Artık sipariş atanmaz

---

#### 📊 `/api/v1/courier/shifts/active` - GET
**Amaç:** Aktif vardiyayı görüntüle  

---

### 4️⃣ BUSINESS ORDER ENDPOINT (Güncellendi)

#### 🏢 `/api/v1/business/orders` - POST
**Amaç:** Sipariş oluşturur + Otomatik FIFO Atama  
**Kimlik Doğrulama:** Bearer Token (Business)  
**Request Body:**
```json
{
  "endCustomerName": "Ahmet Yılmaz",
  "endCustomerPhone": "+905551234567",
  "pickupAddress": "Beşiktaş, Istanbul",
  "deliveryAddress": "Kadıköy, Istanbul",
  "packageDescription": "Pizza (2 adet)",
  "packageCount": 2,
  "deliveryFee": 50.00,
  "paymentType": "CASH"
}
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 456,
    "orderNumber": "ORD-20251203-001",
    "status": "PENDING",
    "courierId": null
  },
  "message": "Order created successfully"
}
```

**YENİ ÖZELLİK:**
- Sipariş oluşturulur oluşturulmaz otomatik olarak FIFO sırasındaki kuryeye atanır
- WebSocket push notification gönderilir
- 2 dakika timeout başlar

---

## 🔄 ENDPOINT AKIŞI

### Complete Delivery Flow:

```
1. Business: POST /business/orders
   ↓ (otomatik FIFO atama)
   
2. Courier: GET /courier/assignments/pending
   ↓ (listele)
   
3. Courier: POST /courier/assignments/{id}/accept
   ↓ (kabul et)
   
4. Courier: POST /courier/orders/{id}/pickup
   ↓ (paketi al)
   
5. Courier: POST /courier/orders/{id}/start-delivery
   ↓ (teslimat başlat)
   
6. Courier: POST /courier/orders/{id}/complete
   ↓ (teslim et)
   
✅ DELIVERED
```

---

## 📊 ENDPOINT İSTATİSTİKLERİ

| Kategori | Yeni Endpoint Sayısı | HTTP Metodu |
|----------|---------------------|-------------|
| Assignment Management | 3 | GET, POST, POST |
| Order Operations | 4 | GET, POST, POST, POST |
| Business Orders | 1 (güncellendi) | POST |
| **TOPLAM** | **8** | - |

---

## 🛡️ GÜVENLİK ÖZELLİKLERİ

✅ **JWT Authentication:** Tüm endpoint'ler Bearer token gerektirir  
✅ **Role-based Access:** COURIER ve BUSINESS rolleri ayrıştırıldı  
✅ **Ownership Validation:** Kurye sadece kendi siparişlerini görebilir  
✅ **Status Validation:** Her adım için status kontrolü yapılır  

---

## 🧪 TEST DURUMU

| Endpoint | Test Durumu | Test Scripti |
|----------|-------------|--------------|
| GET /assignments/pending | ✅ TESTED | test-delivery-flow.sh |
| POST /assignments/{id}/accept | ✅ TESTED | test-delivery-flow.sh |
| POST /assignments/{id}/reject | ⚠️ READY (not in script) | - |
| GET /orders/{id} | ⚠️ READY (not in script) | - |
| POST /orders/{id}/pickup | ✅ TESTED | test-delivery-flow.sh |
| POST /orders/{id}/start-delivery | ✅ TESTED | test-delivery-flow.sh |
| POST /orders/{id}/complete | ✅ TESTED | test-delivery-flow.sh |
| POST /business/orders | ✅ TESTED | test-delivery-flow.sh |

---

## 📚 POSTMAN COLLECTION

Tüm endpoint'ler için Postman collection mevcut:
- `postman/Order_Assignment_System.postman_collection.json`
- `postman/Order_Assignment_Local.postman_environment.json`

---

## 🎯 KULLANIM ÖRNEKLERİ

### Örnek 1: Kurye Pending Assignments Görür
```bash
curl -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

### Örnek 2: Kurye Kabul Eder
```bash
curl -X POST http://localhost:8081/api/v1/courier/assignments/123/accept \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -d '{}'
```

### Örnek 3: Kurye Pickup Yapar
```bash
curl -X POST http://localhost:8081/api/v1/courier/orders/456/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -d '{"notes": "Package picked up"}'
```

### Örnek 4: Full Flow Test
```bash
./scripts/test-delivery-flow.sh
```

---

## 🔥 ÖZEL ÖZELLİKLER

### 1. FIFO Auto-Assignment
- `on_duty_since` bazlı sıralama
- En eski on-duty kurye önce alır
- Kabul sonrası kuyruğun sonuna gider

### 2. WebSocket Real-time Notifications
- Yeni atama → `/queue/courier/{id}/assignments`
- Timeout → `/queue/courier/{id}/assignments`
- Status update → `/queue/business/{id}/orders`

### 3. Timeout Mechanism
- 2 dakika timeout
- @Scheduled her 30 saniyede kontrol
- Otomatik reassignment

### 4. Status Flow Validation
```
PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
```
Her adım kontrol edilir, atlama yapılamaz.

---

## 📝 NOTLAR

1. **Token Expiry:** Test token'ları expiry date'e sahip, güncellenebilir
2. **Database:** Tüm işlemler transactional
3. **Error Handling:** Tüm endpoint'ler uygun HTTP status code'ları döner
4. **Logging:** Tüm işlemler loglanır

---

**Son Güncelleme:** December 3, 2025  
**Test Durumu:** ✅ Production Ready  
**Toplam Endpoint:** 8 (7 yeni + 1 güncelleme)

