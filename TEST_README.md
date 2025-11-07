# 🧪 Business Order API - Test Rehberi

## 📚 Test Dokümantasyonları

### 1. **CURL Test Rehberi** (Detaylı)
📄 **Dosya:** `docs/guides/BUSINESS_ORDER_CURL_TESTS.md`

Tüm endpoint'ler için detaylı curl komutları:
- Health check
- Login
- Order CRUD operations
- Farklı senaryolar
- Hata testleri
- Hızlı komutlar

### 2. **Postman Collection**
📄 **Dosya:** `Business_Orders_API.postman_collection.json`

17 hazır istek:
- Business login (otomatik token kaydetme)
- 4 farklı order create örneği
- Order listeleme ve filtreleme
- Order güncelleme (4 farklı senaryo)
- Order iptal etme
- Order silme
- İstatistikler

**Postman'de İçe Aktar:**
1. Postman aç
2. File → Import
3. `Business_Orders_API.postman_collection.json` seç
4. Import et

### 3. **Python Test Script**
📄 **Dosya:** `test_business_orders.py`

Otomatik entegrasyon testi:
```bash
python3 test_business_orders.py
```

### 4. **Bash Test Script**
📄 **Dosya:** `test-business-orders.sh`

Bash tabanlı test:
```bash
chmod +x test-business-orders.sh
./test-business-orders.sh
```

---

## 🚀 Hızlı Başlangıç

### 1. Backend'i Başlat
```bash
docker compose up -d
```

### 2. Health Check
```bash
curl http://localhost:8081/actuator/health
```

### 3. Login ve Token Al
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')

echo $TOKEN
```

### 4. İlk Order'ı Oluştur
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy, Istanbul",
    "deliveryAddress": "Beşiktaş, Istanbul",
    "endCustomerName": "Test User",
    "endCustomerPhone": "+905551234567",
    "priority": "NORMAL",
    "paymentType": "CASH",
    "deliveryFee": 30.00
  }'
```

---

## 📋 API Endpoints Özeti

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| POST | `/api/v1/auth/login` | Business login |
| POST | `/api/v1/business/orders` | Order oluştur |
| GET | `/api/v1/business/orders` | Tüm orderları listele |
| GET | `/api/v1/business/orders?status=PENDING` | Filtreleme |
| GET | `/api/v1/business/orders/{id}` | Tek order detayı |
| PUT | `/api/v1/business/orders/{id}` | Order güncelle |
| DELETE | `/api/v1/business/orders/{id}` | Order sil |
| POST | `/api/v1/business/orders/{id}/cancel` | Order iptal |
| GET | `/api/v1/business/orders/statistics` | İstatistikler |

---

## 🎯 Test Kullanıcısı

**Email:** `yeni@pizza.com`  
**Password:** `password123`  
**Business ID:** `10`  
**Status:** `ACTIVE`

---

## 💡 İpuçları

### JSON Formatı İçin jq Kullan
```bash
curl ... | jq .
```

### Token'ı Environment Variable Olarak Kaydet
```bash
export TOKEN="your_token_here"
```

### Otomatik Token Alma
```bash
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}' \
  | jq -r '.data.token')
```

---

## 📊 Beklenen Sonuçlar

### ✅ Başarılı İşlemler

**Create Order:**
```json
{
  "success": true,
  "code": 201,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "PENDING",
    ...
  },
  "message": "Order created successfully"
}
```

**List Orders:**
```json
{
  "success": true,
  "code": 200,
  "data": [...],
  "message": "Orders fetched successfully"
}
```

### ❌ Hata Durumları

**401 Unauthorized:**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login"
}
```

**400 Bad Request:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Cannot modify order in current status",
  "path": "/api/v1/business/orders/1"
}
```

**404 Not Found:**
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Order not found with ID: 99999",
  "path": "/api/v1/business/orders/99999"
}
```

---

## 🔒 Authorization Kuralları

### Business Kullanıcısı:

✅ **Yapabilir:**
- Kendi orderlarını oluşturabilir
- Kendi orderlarını görüntüleyebilir
- PENDING durumundaki orderları güncelleyebilir
- PENDING durumundaki orderları silebilir
- PENDING/ASSIGNED durumundaki orderları iptal edebilir

❌ **Yapamaz:**
- Başka business'ın orderlarına erişemez
- ASSIGNED/PICKED_UP sonrası orderları güncelleyemez
- CANCELLED/DELIVERED orderları silemez

---

## 📝 Test Senaryoları

### Senaryo 1: Temel İş Akışı
1. Login yap → Token al
2. Order oluştur → Order ID al
3. Order'ı görüntüle
4. Order'ı güncelle
5. İstatistikleri kontrol et

### Senaryo 2: Order İptali
1. Order oluştur
2. Order'ı iptal et (reason ile)
3. İptal edilen order'ı silmeyi dene (başarısız olmalı)

### Senaryo 3: Çoklu Order
1. 5 adet order oluştur
2. Hepsini listele
3. PENDING olanları filtrele
4. İstatistikleri kontrol et

---

## 🆘 Sorun Giderme

### Backend Çalışmıyor
```bash
docker compose logs backend
docker compose restart backend
```

### Token Expired
```bash
# Yeni token al
TOKEN=$(curl -s ...)
```

### jq Kurulu Değil
```bash
# Ubuntu/Debian
sudo apt-get install jq

# Mac
brew install jq
```

---

## 📞 Destek

Sorun yaşarsan:
1. `docker compose logs backend` ile logları kontrol et
2. `docs/guides/BUSINESS_ORDER_CURL_TESTS.md` detaylı rehbere bak
3. Postman collection'ı kullan (hata mesajları daha net)

---

**🎉 Başarılı Testler Dilerim! 🚀**

