# 🔍 PICKUP HATASI DEBUG REHBERİ

## ❌ HATA
```json
{
    "status": 500,
    "error": "Internal Server Error",
    "message": "Bu sipariş size atanmamış"
}
```

---

## 🔍 MUHTEMEL SEBEPLER

### 1. **Order'a Courier Atanmamış** (En Muhtemel!)
```sql
-- Kontrol:
SELECT id, status, courier_id FROM orders WHERE id = 4;

-- Eğer courier_id NULL ise:
-- → acceptAssignment çalışmamış veya courier set edilmemiş!
```

### 2. **Yanlış Courier ID**
```sql
-- Token'daki courier_id = 4
-- Order'daki courier_id ≠ 4
-- → Başka bir kurye'nin order'ı!
```

### 3. **Yanlış Order Status**
```sql
-- Status ASSIGNED değil (örn: PENDING)
-- → Accept edilmemiş!
```

---

## ✅ ÇÖZÜM ADIMLARI

### Adım 1: Database Kontrolü
```bash
docker exec courier-postgres psql -U courier_user -d courier_db << 'EOF'
-- Order durumu
SELECT id, order_number, status, courier_id, created_at
FROM orders 
WHERE id = 4;

-- Assignment geçmişi
SELECT id, order_id, courier_id, status, assigned_at, response_at
FROM order_assignments 
WHERE order_id = 4
ORDER BY assigned_at DESC;
EOF
```

**Beklenen:**
- Order status: `ASSIGNED`
- Order courier_id: `4` (veya token'daki courier_id)
- Assignment status: `ACCEPTED`

**Eğer courier_id NULL ise** → acceptAssignment çalışmamış!

---

### Adım 2: Yeni Test (Sıfırdan)

```bash
#!/bin/bash
COURIER_TOKEN="your_courier_token"
BUSINESS_TOKEN="your_business_token"

# 1. Yeni sipariş oluştur
ORDER_JSON=$(curl -s -X POST http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $BUSINESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "endCustomerName": "Test",
    "endCustomerPhone": "+905551234567",
    "pickupAddress": "A",
    "deliveryAddress": "B",
    "packageDescription": "Test",
    "packageCount": 1,
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }')

ORDER_ID=$(echo "$ORDER_JSON" | jq -r '.data.orderId')
echo "Order ID: $ORDER_ID"

# 2. Bekle (assignment için)
sleep 3

# 3. Pending assignments
PENDING=$(curl -s -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $COURIER_TOKEN")
ASSIGNMENT_ID=$(echo "$PENDING" | jq -r ".data[] | select(.orderId == $ORDER_ID) | .assignmentId")
echo "Assignment ID: $ASSIGNMENT_ID"

# 4. Accept
curl -s -X POST http://localhost:8081/api/v1/courier/assignments/$ASSIGNMENT_ID/accept \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 5. Database check
docker exec courier-postgres psql -U courier_user -d courier_db -c \
  "SELECT id, status, courier_id FROM orders WHERE id = $ORDER_ID;"

# 6. Pickup
curl -s -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq
```

---

### Adım 3: Log Kontrolü

```bash
# Pickup isteklerini görün
docker logs courier-backend 2>&1 | grep "Pickup request" | tail -5

# Order detaylarını görün
docker logs courier-backend 2>&1 | grep "Order found" | tail -5

# Hataları görün
docker logs courier-backend 2>&1 | grep "Pickup failed" | tail -10
```

**Beklenen Log (Başarılı):**
```
INFO  - Pickup request - Courier: 4, Order: 123, Notes: null
DEBUG - Order found - ID: 123, Status: ASSIGNED, CourierId: 4
INFO  - Pickup successful - Order 123 picked up by courier 4
```

**Hatalı Log (Courier NULL):**
```
INFO  - Pickup request - Courier: 4, Order: 123, Notes: null
DEBUG - Order found - ID: 123, Status: ASSIGNED, CourierId: NULL  ← SORUN!
ERROR - Pickup failed - Order 123 has no courier assigned!
```

---

## 🔧 SORUN acceptAssignment'ta İse

Order'a courier set edilmesi gerekiyor:

```java
// OrderAssignmentService.java - acceptAssignment()

// Courier entity'yi getir
Courier courier = courierRepository.findById(courierId)
    .orElseThrow(() -> new BusinessException("Kurye bulunamadı"));

// Order'a courier'i set et  ← KRİTİK!
order.setCourier(courier);
orderRepository.save(order);
```

**Log'da görmeli:**
```
INFO  - After update - Order 123: status=ASSIGNED, courierId=4, courier set successfully
```

---

## 📝 HIZLI DEBUG KONTROL LİSTESİ

- [ ] Backend çalışıyor mu? (`curl http://localhost:8081/actuator/health`)
- [ ] Token geçerli mi? (exp kontrol)
- [ ] Order var mı? (`SELECT * FROM orders WHERE id = X;`)
- [ ] Order'ın courier_id'si dolu mu?
- [ ] Order status ASSIGNED mi?
- [ ] Token'daki courier_id = Order'daki courier_id mi?
- [ ] Log'larda "Pickup request" görünüyor mu?
- [ ] Log'larda "CourierId: NULL" yazıyor mu?

---

## 🎯 SONUÇ

**En muhtemel sorun:** Order'a courier set edilmemiş!

**Çözüm:** 
1. acceptAssignment metodunda `order.setCourier(courier)` var mı kontrol et
2. Log'larda "After update" mesajını ara
3. Eğer yoksa, backend rebuild edilmemiş olabilir

**Rebuild:**
```bash
cd /home/yasin/Desktop/repos/i-need-courier
docker compose build backend
docker compose up -d
```

---

## 🚚 ENDPOINT AÇIKLAMALARI

### 1. `/api/v1/courier/orders/{orderId}/pickup`
**Ne İşe Yarar:** Sipariş alındı olarak işaretle (müşteriden paket alındı)

**Status Değişimi:** `ASSIGNED` → `PICKED_UP`

**Kullanım:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/orders/10/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Gerekli Koşullar:**
- ✅ Order status = `ASSIGNED` olmalı
- ✅ Order courier_id = token'daki courier_id olmalı

---

### 2. `/api/v1/courier/orders/{orderId}/start-delivery`
**Ne İşe Yarar:** Teslimat başlat (müşteriye doğru yola çıkıldı)

**Status Değişimi:** `PICKED_UP` → `IN_TRANSIT`

**Kullanım:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/orders/10/start-delivery \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Gerekli Koşullar:**
- ✅ Order status = `PICKED_UP` olmalı (önce pickup yapılmış olmalı!)
- ✅ Order courier_id = token'daki courier_id olmalı

**Hata Durumları:**
```json
// Eğer status PICKED_UP değilse:
{
  "status": 400,
  "message": "Teslimat başlatılamaz. Mevcut durum: ASSIGNED, Beklenen: PICKED_UP"
}
```

---

### 3. `/api/v1/courier/orders/{orderId}/complete`
**Ne İşe Yarar:** Teslimat tamamla (müşteriye teslim edildi)

**Status Değişimi:** `IN_TRANSIT` → `DELIVERED`

**Kullanım:**
```bash
# Parametresiz (sadece teslim et)
curl -X POST http://localhost:8081/api/v1/courier/orders/10/complete \
  -H "Authorization: Bearer $COURIER_TOKEN"

# Not ve tahsilat miktarı ile
curl -X POST "http://localhost:8081/api/v1/courier/orders/10/complete?notes=Teslim%20edildi&collectionAmount=150.50" \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Parametreler (opsiyonel):**
- `notes` - Kurye notu
- `collectionAmount` - Tahsil edilen miktar

**Gerekli Koşullar:**
- ✅ Order status = `IN_TRANSIT` olmalı (önce start-delivery yapılmış olmalı!)
- ✅ Order courier_id = token'daki courier_id olmalı

---

## 📊 TAM TESLİMAT AKIŞI

```
1. PENDING        → Business sipariş oluşturdu
2. ASSIGNED       → Kurye accept etti
   ↓ (pickup)
3. PICKED_UP      → Kurye paketi aldı
   ↓ (start-delivery)
4. IN_TRANSIT     → Kurye teslimat için yola çıktı
   ↓ (complete)
5. DELIVERED      → Müşteriye teslim edildi ✅
```

**Curl Komutları Sırası:**
```bash
COURIER_TOKEN="your_token"
ORDER_ID="10"

# 1. Assignment kabul et (önceki adım)
curl -X POST http://localhost:8081/api/v1/courier/assignments/$ASSIGNMENT_ID/accept \
  -H "Authorization: Bearer $COURIER_TOKEN"

# 2. Paketi al
curl -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN"

# 3. Teslimat başlat ← BU ENDPOINT!
curl -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/start-delivery \
  -H "Authorization: Bearer $COURIER_TOKEN"

# 4. Teslim et
curl -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/complete?collectionAmount=100 \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

---

## ⚠️ SIRA ÖNEMLİ!

Her endpoint sadece bir önceki adımdan sonra çalışır:
- ❌ `start-delivery` → Eğer `ASSIGNED` ise hata verir (önce pickup gerekli)
- ❌ `complete` → Eğer `PICKED_UP` ise hata verir (önce start-delivery gerekli)

---

**Son güncelleme:** December 6, 2025  
**Status:** ✅ Detaylı logging eklendi, custom exception'lar hazır  
**Endpoints:** ✅ pickup, start-delivery, complete açıklandı

