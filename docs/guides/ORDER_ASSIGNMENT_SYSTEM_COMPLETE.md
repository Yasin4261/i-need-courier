# 🚀 Sipariş Atama Sistemi - WebSocket + Timeout + FIFO

## ✅ Eklenen Özellikler

### 1. **WebSocket Push Bildirimleri**
- **Kurye Bildirimleri**: Yeni sipariş atandığında anlık bildirim
- **Business Bildirimleri**: Sipariş durumu değişikliklerinde bildirim
- **Timeout Bildirimleri**: Atama süresi dolduğunda uyarı

### 2. **Timeout Mekanizması**
- **Otomatik Kontrol**: Her 30 saniyede bir timeout kontrolü
- **Varsayılan Süre**: 2 dakika (ayarlanabilir)
- **Otomatik Yeniden Atama**: Timeout durumunda bir sonraki kuryeye atar

### 3. **FIFO Sipariş Atama**
- **Sıra Tabanlı**: `on_duty_since` ile en eski kurye önce
- **Otomatik Atama**: Business sipariş oluşturur, sistem kuryeye atar
- **Kabul/Red Mekanizması**: Kurye onayı gerekli

---

## 📁 Eklenen Dosyalar

### Database Migration
```
src/main/resources/db/migration/
└── V15__Create_order_assignments_table.sql
```

### Model & Enum
```
src/main/java/com/api/pako/model/
├── OrderAssignment.java
└── enums/
    ├── AssignmentStatus.java
    └── AssignmentType.java
```

### Repository
```
src/main/java/com/api/pako/repository/
└── OrderAssignmentRepository.java
```

### Services
```
src/main/java/com/api/pako/service/
├── OrderAssignmentService.java (YENİ - timeout + assignment logic)
├── WebSocketNotificationService.java (YENİ - push notifications)
└── OnDutyService.java (GÜNCELLENDİ - getNextInQueue, moveToEndOfQueue)
```

### Configuration
```
src/main/java/com/api/pako/config/
└── WebSocketConfig.java
```

### Documentation
```
docs/guides/
└── ORDER_ASSIGNMENT_POSTMAN.md (Postman test dökümanı)
```

### Configuration Files
```
src/main/resources/
├── application.properties (timeout ayarları eklendi)
└── INeedCourierApplication.java (@EnableScheduling eklendi)
```

---

## 🔧 Konfigürasyon

### application.properties
```properties
# Order Assignment Configuration
order.assignment.timeout.minutes=2

# Scheduling Configuration
spring.task.scheduling.pool.size=5
```

### WebSocket Endpoint
```
ws://localhost:8081/ws
```

### STOMP Topics
- Courier: `/queue/courier/{courierId}/assignments`
- Business: `/queue/business/{businessId}/orders`

---

## 🌊 İŞ AKIŞI

### 1. Sipariş Oluşturma (Business)
```
Business → POST /api/v1/business/orders
    ↓
OrderAssignmentService.assignToNextAvailableCourier()
    ↓
FIFO → OnDutyService.getNextInQueue()
    ↓
Order → status: PENDING
OrderAssignment → status: PENDING, timeout: now() + 2 min
    ↓
WebSocket → Kurye'ye bildirim gönder
```

### 2. Kurye Onayı
```
Kurye → WebSocket bildirimi alır
    ↓
Kurye → GET /api/v1/courier/assignments/pending
    ↓
Kurye → POST /assignments/{id}/accept
    ↓
OrderAssignment → status: ACCEPTED
Order → status: ASSIGNED
    ↓
OnDutyService.moveToEndOfQueue() → Kurye sıranın sonuna
    ↓
WebSocket → Business'e "ASSIGNED" bildirimi
```

### 3. Kurye Reddi
```
Kurye → POST /assignments/{id}/reject
    ↓
OrderAssignment → status: REJECTED
    ↓
OrderAssignmentService.assignToNextAvailableCourier(REASSIGNMENT)
    ↓
Bir sonraki kurye'ye ata (FIFO devam eder)
```

### 4. Timeout (Otomatik)
```
@Scheduled (her 30 saniye)
    ↓
checkTimeouts() → timeout_at < now() olan kayıtları bul
    ↓
OrderAssignment → status: TIMEOUT
    ↓
WebSocket → Kurye'ye timeout bildirimi
    ↓
OrderAssignmentService.assignToNextAvailableCourier(REASSIGNMENT)
    ↓
Bir sonraki kurye'ye ata
```

---

## 🧪 TEST ADIMLAR

### Önkoşullar
```bash
# 1. Migration çalıştır
./mvnw flyway:migrate

# 2. Uygulamayı başlat
./mvnw spring-boot:run

# 3. En az 2 kurye check-in yapmış olmalı
```

### Test 1: Başarılı Atama
```bash
# Business token al
BUSINESS_TOKEN="<token>"

# Sipariş oluştur
curl -X POST http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $BUSINESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "endCustomerName": "Test User",
    "pickupAddress": "Beşiktaş",
    "deliveryAddress": "Kadıköy",
    "packageDescription": "Pizza",
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }'

# Kurye bekleyen atamaları görür
COURIER_TOKEN="<token>"
curl -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $COURIER_TOKEN"

# Kurye kabul eder
curl -X POST http://localhost:8081/api/v1/courier/assignments/1/accept \
  -H "Authorization: Bearer $COURIER_TOKEN"
```

### Test 2: Red Senaryosu
```bash
# Kurye reddeder
curl -X POST http://localhost:8081/api/v1/courier/assignments/1/reject \
  -H "Authorization: Bearer $COURIER_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"reason": "Araç arızalı"}'

# Sistem otomatik olarak bir sonraki kuryeye atar
```

### Test 3: Timeout Senaryosu
```bash
# Sipariş oluştur
curl -X POST http://localhost:8081/api/v1/business/orders ...

# 2 dakika bekle (hiçbir şey yapma)
# Sistem otomatik olarak timeout yapar ve yeniden atar

# Veritabanında kontrol et
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT id, order_id, courier_id, status, assigned_at, timeout_at
FROM order_assignments
WHERE order_id = 1
ORDER BY assigned_at DESC;
"
```

---

## 📊 VERİTABANI SORGU ÖRNEKLERİ

### Sipariş Atama Geçmişi
```sql
SELECT 
    oa.id,
    oa.order_id,
    o.order_number,
    oa.courier_id,
    c.name AS courier_name,
    oa.status,
    oa.assignment_type,
    oa.assigned_at,
    oa.response_at,
    oa.rejection_reason
FROM order_assignments oa
JOIN orders o ON o.id = oa.order_id
JOIN couriers c ON c.id = oa.courier_id
WHERE oa.order_id = 123
ORDER BY oa.assigned_at DESC;
```

### Timeout Olan Atamalar
```sql
SELECT 
    oa.id,
    oa.order_id,
    oa.courier_id,
    oa.assigned_at,
    oa.timeout_at,
    NOW() - oa.timeout_at AS exceeded_by
FROM order_assignments oa
WHERE oa.status = 'TIMEOUT'
ORDER BY oa.timeout_at DESC
LIMIT 10;
```

### Kurye Kabul/Red İstatistikleri
```sql
SELECT 
    c.id,
    c.name,
    COUNT(*) FILTER (WHERE oa.status = 'ACCEPTED') AS accepted,
    COUNT(*) FILTER (WHERE oa.status = 'REJECTED') AS rejected,
    COUNT(*) FILTER (WHERE oa.status = 'TIMEOUT') AS timeouts,
    ROUND(100.0 * COUNT(*) FILTER (WHERE oa.status = 'ACCEPTED') / 
          NULLIF(COUNT(*), 0), 2) AS acceptance_rate
FROM couriers c
LEFT JOIN order_assignments oa ON oa.courier_id = c.id
GROUP BY c.id, c.name
ORDER BY acceptance_rate DESC;
```

---

## 🔌 WebSocket Client Örneği (JavaScript)

### HTML + JavaScript
```html
<!DOCTYPE html>
<html>
<head>
    <title>Courier WebSocket Test</title>
    <script src="https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/stompjs@2.3.3/lib/stomp.min.js"></script>
</head>
<body>
    <h1>Courier Assignment Notifications</h1>
    <div id="notifications"></div>

    <script>
        const courierId = 4; // Your courier ID
        const socket = new SockJS('http://localhost:8081/ws');
        const stompClient = Stomp.over(socket);

        stompClient.connect({}, function(frame) {
            console.log('Connected: ' + frame);
            
            stompClient.subscribe('/queue/courier/' + courierId + '/assignments', 
                function(message) {
                    const notification = JSON.parse(message.body);
                    console.log('Notification received:', notification);
                    
                    const div = document.getElementById('notifications');
                    const notifDiv = document.createElement('div');
                    notifDiv.style.border = '1px solid #ccc';
                    notifDiv.style.padding = '10px';
                    notifDiv.style.margin = '5px';
                    
                    if (notification.type === 'NEW_ASSIGNMENT') {
                        notifDiv.innerHTML = `
                            <h3>🆕 Yeni Sipariş!</h3>
                            <p><strong>Assignment ID:</strong> ${notification.assignmentId}</p>
                            <p><strong>Order ID:</strong> ${notification.orderId}</p>
                            <p><strong>Pickup:</strong> ${notification.orderDetails.pickupAddress}</p>
                            <p><strong>Delivery:</strong> ${notification.orderDetails.deliveryAddress}</p>
                            <p><strong>Fee:</strong> ${notification.orderDetails.deliveryFee} TL</p>
                            <p><strong>Timeout:</strong> ${notification.timeoutAt}</p>
                        `;
                        notifDiv.style.backgroundColor = '#d4edda';
                    } else if (notification.type === 'ASSIGNMENT_TIMEOUT') {
                        notifDiv.innerHTML = `
                            <h3>⏰ Atama Zaman Aşımı</h3>
                            <p><strong>Assignment ID:</strong> ${notification.assignmentId}</p>
                            <p>${notification.message}</p>
                        `;
                        notifDiv.style.backgroundColor = '#f8d7da';
                    }
                    
                    div.prepend(notifDiv);
                });
        });
    </script>
</body>
</html>
```

---

## 📝 ÖNEMLİ NOTLAR

### 1. Timeout Süresi Değiştirme
```properties
# application.properties
order.assignment.timeout.minutes=5  # 5 dakika yap
```

### 2. Scheduled Task Sıklığı
```java
// OrderAssignmentService.java
@Scheduled(fixedDelay = 60000) // 60 saniyede bir yap
public void checkTimeouts() { ... }
```

### 3. WebSocket CORS
```java
// WebSocketConfig.java
registry.addEndpoint("/ws")
    .setAllowedOriginPatterns("*")  // Production'da spesifik domain kullan
    .withSockJS();
```

### 4. Performance Tuning
```properties
# Çok sayıda eşzamanlı atama için
spring.task.scheduling.pool.size=10
```

---

## 🚨 Troubleshooting

### Problem: WebSocket bağlantı hatası
**Çözüm:**
```bash
# CORS ayarlarını kontrol et
# Browser console'da error mesajını oku
# SockJS fallback kontrol et
```

### Problem: Timeout çalışmıyor
**Çözüm:**
```bash
# @EnableScheduling eklendi mi kontrol et
# Loglarda "Found X timed out assignments" mesajını ara
tail -f logs/app.log | grep -i timeout
```

### Problem: Bildirim gelmiyor
**Çözüm:**
```bash
# WebSocket bağlantısını test et
# STOMP subscribe doğru topic'e yapılmış mı kontrol et
# SimpMessagingTemplate bean oluştu mu kontrol et
```

---

## ✅ ÖZET CHECKLIST

- [x] Migration V15 oluşturuldu (`order_assignments`)
- [x] Entity & Enums eklendi (`OrderAssignment`, `AssignmentStatus`, `AssignmentType`)
- [x] Repository eklendi (`OrderAssignmentRepository`)
- [x] WebSocket konfigürasyonu (`WebSocketConfig`)
- [x] Bildirim servisi (`WebSocketNotificationService`)
- [x] Atama servisi (`OrderAssignmentService`)
- [x] Timeout mekanizması (`@Scheduled checkTimeouts()`)
- [x] FIFO metodları (`getNextInQueue`, `moveToEndOfQueue`)
- [x] Postman dökümanı (`ORDER_ASSIGNMENT_POSTMAN.md`)
- [x] application.properties güncellendi
- [x] @EnableScheduling eklendi

---

**Sistem hazır! Business sipariş oluşturduğunda otomatik olarak FIFO sırasındaki kuryeye atanır, WebSocket bildirimi gider ve timeout kontrolü çalışır.** 🎉

