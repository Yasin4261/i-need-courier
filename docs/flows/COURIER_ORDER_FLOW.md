# 🚚 KURYE SİPARİŞ AKIŞI - TAM REHBER

## 📋 ÖZET

Bir sipariş şu aşamalardan geçer:

```
PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
```

---

## 🔄 ADIM ADIM SİPARİŞ AKIŞI

### **1️⃣ Business Sipariş Oluşturur**

**Endpoint:** `POST /api/v1/business/orders`

**Kullanan:** Business (İşletme)

**Ne Olur:**
- Yeni sipariş oluşturulur
- Status: `PENDING`
- Otomatik olarak sıradaki kuryeye atanır (FIFO)
- Assignment oluşturulur (status: `PENDING`)
- Kuryeye WebSocket bildirimi gider

**Örnek:**
```bash
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $BUSINESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "pickupAddress": "Kadıköy Moda No:123",
    "deliveryAddress": "Beşiktaş Barbaros No:45",
    "packageDescription": "Elektronik",
    "packageCount": 1,
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }'
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 123,
    "orderNumber": "ORD-20251206-123",
    "status": "PENDING"
  }
}
```

---

### **2️⃣ Kurye Bekleyen Atamaları Görür**

**Endpoint:** `GET /api/v1/courier/assignments/pending`

**Kullanan:** Courier (Kurye)

**Ne Olur:**
- Kuryeye atanmış pending assignment'lar listelenir
- Timeout olmamış olanlar gösterilir

**Örnek:**
```bash
curl -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": [
    {
      "assignmentId": 456,
      "orderId": 123,
      "status": "PENDING",
      "assignedAt": "2025-12-06T12:00:00Z",
      "timeoutAt": "2025-12-06T12:04:00Z",
      "remainingSeconds": 180
    }
  ]
}
```

---

### **3️⃣ Kurye Atamayı Kabul Eder** ⭐

**Endpoint:** `POST /api/v1/courier/assignments/{assignmentId}/accept`

**Kullanan:** Courier (Kurye)

**Ne Olur:**
- Assignment status: `PENDING` → `ACCEPTED`
- **Order status: `PENDING` → `ASSIGNED`** ✅
- **Order'a courier atanır** ✅ (`order.courier_id = courier_id`)
- Kurye kuyruğun sonuna gider
- Business'e bildirim gider

**Örnek:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/assignments/456/accept \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "message": "Atama kabul edildi"
}
```

**⚠️ ÖNEMLİ:** Bu adımdan sonra order'ın courier_id'si set edilir!

---

### **4️⃣ Kurye Paketi Alır (Pickup)** 📦

**Endpoint:** `POST /api/v1/courier/orders/{orderId}/pickup`

**Kullanan:** Courier (Kurye)

**Ne İşe Yarar:** Kuryenin müşteriden/işletmeden paketi aldığını işaretler

**Ne Olur:**
- Order status: `ASSIGNED` → `PICKED_UP`
- İsteğe bağlı not eklenebilir

**Gerekli Koşullar:**
- ✅ Order status = `ASSIGNED` olmalı
- ✅ Order courier_id = token'daki courier_id olmalı

**Örnek:**
```bash
# Notsu
curl -X POST http://localhost:8081/api/v1/courier/orders/123/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN"

# Notlu
curl -X POST "http://localhost:8081/api/v1/courier/orders/123/pickup?notes=Paket alındı" \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "orderNumber": "ORD-20251206-123",
    "status": "PICKED_UP"
  },
  "message": "Sipariş alındı (PICKED_UP)"
}
```

---

### **5️⃣ Kurye Teslimat Başlatır (Start Delivery)** 🚗

**Endpoint:** `POST /api/v1/courier/orders/{orderId}/start-delivery`

**Kullanan:** Courier (Kurye)

**Ne İşe Yarar:** Kuryenin son müşteriye doğru yola çıktığını işaretler

**Ne Olur:**
- Order status: `PICKED_UP` → `IN_TRANSIT`

**Gerekli Koşullar:**
- ✅ Order status = `PICKED_UP` olmalı (önce pickup yapılmış olmalı!)
- ✅ Order courier_id = token'daki courier_id olmalı

**Örnek:**
```bash
curl -X POST http://localhost:8081/api/v1/courier/orders/123/start-delivery \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "orderNumber": "ORD-20251206-123",
    "status": "IN_TRANSIT"
  },
  "message": "Teslimat başladı (IN_TRANSIT)"
}
```

---

### **6️⃣ Kurye Teslimatı Tamamlar (Complete)** ✅

**Endpoint:** `POST /api/v1/courier/orders/{orderId}/complete`

**Kullanan:** Courier (Kurye)

**Ne İşe Yarar:** Kuryenin son müşteriye paketi teslim ettiğini işaretler

**Ne Olur:**
- Order status: `IN_TRANSIT` → `DELIVERED`
- İsteğe bağlı not eklenebilir
- İsteğe bağlı tahsilat miktarı kaydedilebilir

**Gerekli Koşullar:**
- ✅ Order status = `IN_TRANSIT` olmalı (önce start-delivery yapılmış olmalı!)
- ✅ Order courier_id = token'daki courier_id olmalı

**Parametreler (opsiyonel):**
- `notes` (String) - Kurye notu (örn: "Teslim edildi", "Komşuya verildi")
- `collectionAmount` (Double) - Tahsil edilen miktar (CASH ödeme için)

**Örnek:**
```bash
# Basit (sadece teslim et)
curl -X POST http://localhost:8081/api/v1/courier/orders/123/complete \
  -H "Authorization: Bearer $COURIER_TOKEN"

# Notlu
curl -X POST "http://localhost:8081/api/v1/courier/orders/123/complete?notes=Müşteriye teslim edildi" \
  -H "Authorization: Bearer $COURIER_TOKEN"

# Not ve tahsilat ile
curl -X POST "http://localhost:8081/api/v1/courier/orders/123/complete?notes=Teslim&collectionAmount=150.50" \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

**Response:**
```json
{
  "code": 200,
  "data": {
    "id": 123,
    "orderNumber": "ORD-20251206-123",
    "status": "DELIVERED",
    "courierNotes": "Müşteriye teslim edildi",
    "collectionAmount": 150.50
  },
  "message": "Sipariş teslim edildi (DELIVERED)"
}
```

---

## 📊 TAM AKIŞ DİYAGRAMI

```
┌─────────────────────────────────────────────────────────────┐
│ BUSINESS                                                     │
│ POST /api/v1/business/orders                                │
│ ↓                                                            │
│ Order oluştur (status: PENDING)                             │
│ Otomatik assignment oluştur → FIFO kuryeye ata             │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ COURIER                                                      │
│ GET /api/v1/courier/assignments/pending                     │
│ ↓                                                            │
│ Bekleyen atamaları gör                                       │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ COURIER - ADIM 1                                            │
│ POST /api/v1/courier/assignments/{assignmentId}/accept     │
│ ↓                                                            │
│ Assignment: PENDING → ACCEPTED                              │
│ Order: PENDING → ASSIGNED ⭐                                │
│ order.courier_id SET EDİLİR ⭐                              │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ COURIER - ADIM 2                                            │
│ POST /api/v1/courier/orders/{orderId}/pickup               │
│ ↓                                                            │
│ Order: ASSIGNED → PICKED_UP 📦                              │
│ (Paket müşteriden/işletmeden alındı)                        │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ COURIER - ADIM 3                                            │
│ POST /api/v1/courier/orders/{orderId}/start-delivery       │
│ ↓                                                            │
│ Order: PICKED_UP → IN_TRANSIT 🚗                            │
│ (Son müşteriye doğru yola çıkıldı)                          │
└─────────────────────────────────────────────────────────────┘
                           ↓
┌─────────────────────────────────────────────────────────────┐
│ COURIER - ADIM 4                                            │
│ POST /api/v1/courier/orders/{orderId}/complete             │
│ ↓                                                            │
│ Order: IN_TRANSIT → DELIVERED ✅                            │
│ (Müşteriye teslim edildi)                                   │
│ + İsteğe bağlı: notes, collectionAmount                     │
└─────────────────────────────────────────────────────────────┘
```

---

## 🎯 DOĞRU SIRA (Kurye için)

```bash
#!/bin/bash
COURIER_TOKEN="eyJhbGciOiJIUzM4NCJ9..."

# 1️⃣ Bekleyen atamaları gör
PENDING=$(curl -s -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $COURIER_TOKEN")
echo "$PENDING" | jq

# 2️⃣ Atamayı kabul et
ASSIGNMENT_ID=456
curl -s -X POST http://localhost:8081/api/v1/courier/assignments/$ASSIGNMENT_ID/accept \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 3️⃣ Paketi al (pickup)
ORDER_ID=123
curl -s -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/pickup \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 4️⃣ Teslimat başlat
curl -s -X POST http://localhost:8081/api/v1/courier/orders/$ORDER_ID/start-delivery \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq

# 5️⃣ Teslim et (complete)
curl -s -X POST "http://localhost:8081/api/v1/courier/orders/$ORDER_ID/complete?notes=Teslim&collectionAmount=100" \
  -H "Authorization: Bearer $COURIER_TOKEN" | jq
```

---

## ⚠️ SIRA ÖNEMLİ! HATALAR

### ❌ Yanlış Sıra Örneği 1:
```bash
# Accept etmeden direkt pickup yapmak
curl -X POST /api/v1/courier/orders/123/pickup
```
**Hata:**
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Bu sipariş henüz bir kuryeye atanmamış. Lütfen önce siparişi kabul edin."
}
```

### ❌ Yanlış Sıra Örneği 2:
```bash
# Pickup yapmadan start-delivery
curl -X POST /api/v1/courier/orders/123/start-delivery
```
**Hata:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Teslimat başlatılamaz. Mevcut durum: ASSIGNED, Beklenen: PICKED_UP"
}
```

### ❌ Yanlış Sıra Örneği 3:
```bash
# Start-delivery yapmadan complete
curl -X POST /api/v1/courier/orders/123/complete
```
**Hata:**
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Teslimat tamamlanamaz. Mevcut durum: PICKED_UP, Beklenen: IN_TRANSIT"
}
```

---

## 📝 STATUS GEÇİŞ KURALLARI

| Mevcut Status | İzin Verilen İşlem | Sonraki Status |
|---------------|-------------------|----------------|
| `PENDING` | accept | `ASSIGNED` |
| `ASSIGNED` | pickup | `PICKED_UP` |
| `PICKED_UP` | start-delivery | `IN_TRANSIT` |
| `IN_TRANSIT` | complete | `DELIVERED` |

**Kural:** Her status sadece bir sonraki status'e geçebilir, atlanamaz!

---

## 💡 ÖZET

### `/complete` Endpoint'i:
- **Amaç:** Teslimatı tamamla (müşteriye teslim edildi)
- **Status:** `IN_TRANSIT` → `DELIVERED`
- **Parametreler:** `notes` (opsiyonel), `collectionAmount` (opsiyonel)
- **Sırada:** 4. ve SON adım

### Kurye İçin Tam Sıra:
1. ✅ **Accept** → Assignment kabul et
2. ✅ **Pickup** → Paketi al
3. ✅ **Start Delivery** → Yola çık
4. ✅ **Complete** → Teslim et

---

---

## 🤔 SORU: START-DELIVERY NEDEN VAR? GEREKLİ Mİ?

### Kısa Cevap: EVET, GEREKLİ! (ama opsiyonel yapılabilir)

### Gerçek Dünya Sebepleri:

#### 1️⃣ **Çok Paketli Teslimat**
```
Kurye sabah 5 paket aldı (pickup):
- Her paket için ayrı start-delivery ve complete

10:30 → Paket A için start-delivery
11:00 → Paket A complete ✅
11:15 → Paket B için start-delivery
11:45 → Paket B complete ✅
```

#### 2️⃣ **Müşteri Bilgilendirme**
```
PICKED_UP:
  "Siparişiniz hazırlanıyor, kurye henüz yola çıkmadı"

IN_TRANSIT: (start-delivery sonrası)
  "🚗 Kuryeniz yolda! Tahmini 15 dakika"
  [Canlı GPS Haritası]

DELIVERED:
  "✅ Teslim edildi"
```

#### 3️⃣ **İptal Kontrolü**
```
PICKED_UP: ✅ İptal edilebilir (kurye henüz yolda değil)
IN_TRANSIT: ❌ İptal edilemez (kurye zaten yolda)
```

#### 4️⃣ **Zaman Analizi**
```
Pickup → Start-delivery: Hazırlık süresi (örn: 30 dk)
Start-delivery → Complete: Teslimat süresi (örn: 20 dk)

KPI'lar için önemli!
```

---

### Basitleştirilebilir mi? ✅ EVET!

**3 Adımlı Sistem:**
```
Accept → Pickup → Complete (start-delivery atla)
```

**Kod değişikliği gerekir:**
```java
// complete metodunda:
if (order.getStatus() != OrderStatus.PICKED_UP && 
    order.getStatus() != OrderStatus.IN_TRANSIT) {
    // Her iki durumu da kabul et
}
```

---

### 📊 Hangi Sistemi Seçmeli?

| Durum | Öneri |
|-------|-------|
| Mobil app + GPS + Canlı takip | **4 Adım** (Accept, Pickup, Start, Complete) ✅ |
| Basit MVP/Backend only | **3 Adım** (Accept, Pickup, Complete) ✅ |
| Çok paketli teslimat | **4 Adım** ✅ |
| Tek paket + basit sistem | **3 Adım** ✅ |

**Mevcut Sistem:** 4 adımlı ✅ (profesyonel ve esnek)

**Detaylı Açıklama:** `WHY_START_DELIVERY_EXISTS.md` dosyasına bak!

---

**Güncelleme:** December 6, 2025  
**Status:** ✅ Tüm endpoint'ler açıklandı  
**Test:** ✅ Curl örnekleri hazır  
**Açıklama:** ✅ Start-delivery neden var açıklandı

