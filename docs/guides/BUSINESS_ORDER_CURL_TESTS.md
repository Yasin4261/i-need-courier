# 🧪 Business Order API - Curl Test Rehberi

## 📋 Hazırlık

### 1. Backend'in Çalıştığını Kontrol Et
```bash
curl http://localhost:8081/actuator/health
```

**Beklenen Sonuç:**
```json
{
  "status": "UP"
}
```

---

## 🔐 Adım 1: Business Login (Token Al)

### İşletme Girişi
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

### Token'ı Kaydet
```bash
# Token'ı değişkene kaydet
TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQlVTSU5FU1MiLCJ1c2VySWQiOjEwLCJlbWFpbCI6InllbmlAcGl6emEuY29tIiwic3ViIjoieWVuaUBwaXp6YS5jb20iLCJpYXQiOjE3NjI0NzI2NTIsImV4cCI6MTc2MjU1OTA1Mn0.fCcCijZCn-Dl3rv1rmgiNORNUJfbyV6Tr78JDbz0P_7YfP3j0-OD1hIZBV5VGgBl"

# Ya da otomatik al (jq gerekli)
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')

echo "Token: $TOKEN"
```

---

## 📦 Adım 2: Order Oluştur (CREATE)

### Basit Order
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy Moda Caddesi No:123, Istanbul",
    "deliveryAddress": "Beşiktaş Barbaros Bulvarı No:45, Istanbul",
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "priority": "NORMAL",
    "paymentType": "CASH",
    "deliveryFee": 35.50
  }'
```

### Detaylı Order (Tüm Alanlar)
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy Moda Caddesi No:123, Istanbul",
    "pickupAddressDescription": "Köşe bina, kırmızı kapı",
    "pickupContactPerson": "Ali Veli",
    "deliveryAddress": "Beşiktaş Barbaros Bulvarı No:45, Istanbul",
    "deliveryAddressDescription": "Ofis binası 3. kat",
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "packageDescription": "2x Pizza Margherita, 1x Coca Cola",
    "packageWeight": 1.5,
    "packageCount": 3,
    "priority": "NORMAL",
    "paymentType": "CASH",
    "deliveryFee": 35.50,
    "collectionAmount": 0,
    "businessNotes": "Sıcak tutulmalı, 30 dakikada teslim"
  }'
```

### Urgent Priority Order
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy, Istanbul",
    "deliveryAddress": "Beşiktaş, Istanbul",
    "endCustomerName": "Mehmet Öz",
    "endCustomerPhone": "+905559876543",
    "priority": "URGENT",
    "paymentType": "CREDIT_CARD",
    "deliveryFee": 50.00,
    "businessNotes": "ACİL TESLİMAT!"
  }'
```

### Cash on Delivery Order
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy, Istanbul",
    "deliveryAddress": "Şişli, Istanbul",
    "endCustomerName": "Ayşe Demir",
    "endCustomerPhone": "+905551112233",
    "priority": "NORMAL",
    "paymentType": "CASH_ON_DELIVERY",
    "deliveryFee": 25.00,
    "collectionAmount": 150.00,
    "businessNotes": "Müşteriden 150 TL tahsil edilecek"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 201,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "PENDING",
    "priority": "NORMAL",
    "businessId": 10,
    "businessName": "Yeni Pizza Restaurant",
    "pickupAddress": "Kadıköy Moda Caddesi No:123, Istanbul",
    "deliveryAddress": "Beşiktaş Barbaros Bulvarı No:45, Istanbul",
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "paymentType": "CASH",
    "deliveryFee": 35.50,
    "createdAt": "2025-11-07T03:15:30",
    "updatedAt": "2025-11-07T03:15:30"
  },
  "message": "Order created successfully"
}
```

### Order ID'yi Kaydet
```bash
# Son oluşturulan order'ın ID'sini kaydet
ORDER_ID=1  # Yukarıdaki response'dan al

# Ya da otomatik al
ORDER_ID=$(curl -s -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"pickupAddress":"Kadıköy","deliveryAddress":"Beşiktaş","endCustomerName":"Test","endCustomerPhone":"+905551234567","priority":"NORMAL","paymentType":"CASH","deliveryFee":25}' \
  | jq -r '.data.orderId')

echo "Order ID: $ORDER_ID"
```

---

## 📋 Adım 3: Order Listele (READ)

### Tüm Orderları Listele
```bash
curl -X GET http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Status'e Göre Filtrele - PENDING
```bash
curl -X GET "http://localhost:8081/api/v1/business/orders?status=PENDING" \
  -H "Authorization: Bearer $TOKEN"
```

### Status'e Göre Filtrele - ASSIGNED
```bash
curl -X GET "http://localhost:8081/api/v1/business/orders?status=ASSIGNED" \
  -H "Authorization: Bearer $TOKEN"
```

### Status'e Göre Filtrele - DELIVERED
```bash
curl -X GET "http://localhost:8081/api/v1/business/orders?status=DELIVERED" \
  -H "Authorization: Bearer $TOKEN"
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": [
    {
      "orderId": 1,
      "orderNumber": "ORD-20251107-001",
      "status": "PENDING",
      ...
    },
    {
      "orderId": 2,
      "orderNumber": "ORD-20251107-002",
      "status": "PENDING",
      ...
    }
  ],
  "message": "Orders fetched successfully"
}
```

---

## 🔍 Adım 4: Tek Order Detayı (READ BY ID)

```bash
curl -X GET http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "PENDING",
    "priority": "NORMAL",
    "businessId": 10,
    "businessName": "Yeni Pizza Restaurant",
    "pickupAddress": "Kadıköy Moda Caddesi No:123, Istanbul",
    "deliveryAddress": "Beşiktaş Barbaros Bulvarı No:45, Istanbul",
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "packageDescription": "2x Pizza Margherita",
    "packageWeight": 1.5,
    "packageCount": 2,
    "paymentType": "CASH",
    "deliveryFee": 35.50,
    "collectionAmount": 0.0,
    "businessNotes": "Sıcak tutulmalı",
    "createdAt": "2025-11-07T03:15:30",
    "updatedAt": "2025-11-07T03:15:30"
  },
  "message": "Order fetched successfully"
}
```

---

## ✏️ Adım 5: Order Güncelle (UPDATE)

⚠️ **Not:** Sadece PENDING durumundaki orderlar güncellenebilir!

### Paket Açıklaması Güncelle
```bash
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "packageDescription": "3x Pizza Margherita (GÜNCELLEME)"
  }'
```

### Teslimat Adresi Değiştir
```bash
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "deliveryAddress": "Taksim Meydanı No:1, Istanbul",
    "deliveryAddressDescription": "Meydan girişi"
  }'
```

### Öncelik ve Not Güncelle
```bash
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "priority": "HIGH",
    "businessNotes": "ACİL TESLİMAT GEREKLİ!"
  }'
```

### Müşteri Telefonu Güncelle
```bash
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "endCustomerPhone": "+905559999999"
  }'
```

### Çoklu Alan Güncelleme
```bash
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "packageDescription": "4x Pizza + 2x İçecek",
    "packageWeight": 2.5,
    "packageCount": 6,
    "deliveryFee": 45.00,
    "businessNotes": "Paket içeriği değişti"
  }'
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "PENDING",
    "packageDescription": "3x Pizza Margherita (GÜNCELLEME)",
    "updatedAt": "2025-11-07T03:20:15",
    ...
  },
  "message": "Order updated successfully"
}
```

---

## ❌ Adım 6: Order İptal Et (CANCEL)

⚠️ **Not:** PENDING veya ASSIGNED durumundaki orderlar iptal edilebilir!

### Basit İptal
```bash
curl -X POST http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel \
  -H "Authorization: Bearer $TOKEN"
```

### Sebep Belirterek İptal
```bash
curl -X POST "http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel?reason=Müşteri%20vazgeçti" \
  -H "Authorization: Bearer $TOKEN"
```

### Farklı İptal Sebepleri
```bash
# Yanlış adres
curl -X POST "http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel?reason=Yanlış%20adres%20girildi" \
  -H "Authorization: Bearer $TOKEN"

# Stok yok
curl -X POST "http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel?reason=Ürün%20stokta%20yok" \
  -H "Authorization: Bearer $TOKEN"

# Müşteri isteği
curl -X POST "http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel?reason=Müşteri%20iptali" \
  -H "Authorization: Bearer $TOKEN"
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "CANCELLED",
    "businessNotes": "Cancelled by business. Reason: Müşteri vazgeçti",
    "updatedAt": "2025-11-07T03:25:00",
    ...
  },
  "message": "Order cancelled successfully"
}
```

---

## 🗑️ Adım 7: Order Sil (DELETE)

⚠️ **Not:** Sadece PENDING durumundaki orderlar silinebilir!

```bash
curl -X DELETE http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN"
```

**Beklenen Cevap (Başarılı):**
```json
{
  "success": true,
  "code": 200,
  "data": null,
  "message": "Order deleted successfully"
}
```

**Hata Durumu (CANCELLED order silinemez):**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot delete order in status: CANCELLED. Only PENDING orders can be deleted.",
  "path": "/api/v1/business/orders/1",
  "timestamp": "2025-11-07T03:30:00"
}
```

---

## 📊 Adım 8: İstatistikleri Getir (STATISTICS)

```bash
curl -X GET http://localhost:8081/api/v1/business/orders/statistics \
  -H "Authorization: Bearer $TOKEN"
```

**Beklenen Cevap:**
```json
{
  "success": true,
  "code": 200,
  "data": {
    "totalOrders": 25,
    "pendingOrders": 8,
    "assignedOrders": 4,
    "inTransitOrders": 2,
    "deliveredOrders": 9,
    "cancelledOrders": 2
  },
  "message": "Statistics fetched successfully"
}
```

---

## 🧪 Test Senaryoları

### Senaryo 1: Tam İş Akışı
```bash
# 1. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')

# 2. Order oluştur
ORDER_ID=$(curl -s -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"pickupAddress":"Kadıköy","deliveryAddress":"Beşiktaş","endCustomerName":"Test User","endCustomerPhone":"+905551234567","priority":"NORMAL","paymentType":"CASH","deliveryFee":30}' \
  | jq -r '.data.orderId')

echo "Order oluşturuldu: $ORDER_ID"

# 3. Order'ı görüntüle
curl -X GET http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Authorization: Bearer $TOKEN" | jq .

# 4. Order'ı güncelle
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"businessNotes":"Test güncelleme"}' | jq .

# 5. İstatistikleri gör
curl -X GET http://localhost:8081/api/v1/business/orders/statistics \
  -H "Authorization: Bearer $TOKEN" | jq .

# 6. Order'ı iptal et
curl -X POST "http://localhost:8081/api/v1/business/orders/$ORDER_ID/cancel?reason=Test" \
  -H "Authorization: Bearer $TOKEN" | jq .
```

### Senaryo 2: Çoklu Order Oluştur
```bash
# Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')

# 5 adet order oluştur
for i in {1..5}; do
  curl -s -X POST http://localhost:8081/api/v1/business/orders \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d "{
      \"pickupAddress\": \"Kadıköy $i. Sokak\",
      \"deliveryAddress\": \"Beşiktaş $i. Cadde\",
      \"endCustomerName\": \"Müşteri $i\",
      \"endCustomerPhone\": \"+9055512340$i\",
      \"priority\": \"NORMAL\",
      \"paymentType\": \"CASH\",
      \"deliveryFee\": $((25 + i * 5))
    }" | jq -r '.data.orderNumber'
  sleep 1
done

# Tüm orderları listele
curl -s -X GET http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $TOKEN" | jq '.data | length'
```

### Senaryo 3: Hata Testleri

#### Yanlış Token
```bash
curl -X GET http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer YANLIS_TOKEN"
```

#### Olmayan Order
```bash
curl -X GET http://localhost:8081/api/v1/business/orders/99999 \
  -H "Authorization: Bearer $TOKEN"
```

#### ASSIGNED Order'ı Güncelleme (Başarısız Olmalı)
```bash
# Önce bir order oluştur ve status'unu manuel ASSIGNED yap
# Sonra güncellemeyi dene - hata vermeli
curl -X PUT http://localhost:8081/api/v1/business/orders/$ORDER_ID \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"packageDescription":"test"}'
```

---

## 📋 Hızlı Komut Listesi

```bash
# Token al ve kaydet
export TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')

# Order oluştur
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"pickupAddress":"A","deliveryAddress":"B","endCustomerName":"C","endCustomerPhone":"+905551234567","priority":"NORMAL","paymentType":"CASH","deliveryFee":25}'

# Orderları listele
curl -X GET http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $TOKEN" | jq .

# İstatistik
curl -X GET http://localhost:8081/api/v1/business/orders/statistics \
  -H "Authorization: Bearer $TOKEN" | jq .
```

---

## 💡 İpuçları

### JSON Formatı İçin jq Kullan
```bash
# Güzel görünüm
curl ... | jq .

# Sadece order number'ları göster
curl ... | jq '.data[].orderNumber'

# Sadece PENDING olanları filtrele
curl ... | jq '.data[] | select(.status=="PENDING")'
```

### Response Time Ölç
```bash
curl -w "\nTime: %{time_total}s\n" ...
```

### Verbose Mode (Debug)
```bash
curl -v ...
```

### Header'ları Göster
```bash
curl -i ...
```

---

## ✅ Test Kontrol Listesi

- [ ] Health check çalışıyor
- [ ] Login başarılı, token alınıyor
- [ ] Order oluşturma çalışıyor (201 Created)
- [ ] Order listeme çalışıyor
- [ ] Status filtreleme çalışıyor
- [ ] Tek order getirme çalışıyor
- [ ] Order güncelleme çalışıyor (PENDING)
- [ ] Order iptal etme çalışıyor
- [ ] Order silme çalışıyor (PENDING)
- [ ] İstatistikler çalışıyor
- [ ] Hata durumları doğru handle ediliyor

---

**🎉 Test Başarılı Olursa:**
Tüm CRUD operasyonları çalışıyor demektir! 🚀

