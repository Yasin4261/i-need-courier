# 📮 Postman Collection - Sipariş Atama ve WebSocket Sistemi

## Genel Bakış

Bu dokümantasyon, **Sipariş Atama Sistemi** (FIFO + Kurye Onayı + WebSocket + Timeout) için Postman test senaryolarını içerir.

---

## 🔧 Postman Environment Variables

Öncelikle bir **Environment** oluşturun ve aşağıdaki değişkenleri tanımlayın:

| Variable | Initial Value | Description |
|----------|---------------|-------------|
| `base_url` | `http://localhost:8081` | API base URL |
| `business_token` | `<JWT_TOKEN>` | Business JWT token |
| `courier_token` | `<JWT_TOKEN>` | Courier JWT token |
| `order_id` | | Dinamik olarak atanır |
| `assignment_id` | | Dinamik olarak atanır |

---

## 📁 Collection Structure

```
Order Assignment System
├── 1. Authentication
│   ├── Business Login
│   └── Courier Login
├── 2. Business - Create Order
│   └── Create Order (Auto-assign)
├── 3. Courier - Assignments
│   ├── Get Pending Assignments
│   ├── Accept Assignment
│   └── Reject Assignment
├── 4. WebSocket Test
│   └── WebSocket Connection Info
└── 5. Admin - Monitoring
    ├── Get Order Assignments
    └── Check Timeout Status
```

---

## 1️⃣ Authentication

### Business Login

**Request:**
```http
POST {{base_url}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "business1@test.com",
  "password": "password123"
}
```

**Test Script:**
```javascript
pm.test("Status code is 200", function () {
    pm.response.to.have.status(200);
});

pm.test("Token exists", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.token).to.exist;
    pm.environment.set("business_token", jsonData.data.token);
});
```

---

### Courier Login

**Request:**
```http
POST {{base_url}}/api/v1/auth/login
Content-Type: application/json

{
  "email": "courier1@test.com",
  "password": "password123"
}
```

**Test Script:**
```javascript
pm.test("Token exists", function () {
    var jsonData = pm.response.json();
    pm.environment.set("courier_token", jsonData.data.token);
});
```

---

## 2️⃣ Business - Create Order (Auto-assign)

**Request:**
```http
POST {{base_url}}/api/v1/business/orders
Authorization: Bearer {{business_token}}
Content-Type: application/json

{
  "endCustomerName": "Ahmet Yılmaz",
  "endCustomerPhone": "+905551234567",
  "pickupAddress": "Beşiktaş Meydanı, İstanbul",
  "deliveryAddress": "Kadıköy İskelesi, İstanbul",
  "packageDescription": "Pizza (2 kutu, sıcak)",
  "packageCount": 2,
  "packageWeight": 1.5,
  "deliveryFee": 50.00,
  "paymentType": "CASH",
  "businessNotes": "Ekstra ketçap mayonez eklendi"
}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "orderId": 123,
    "orderNumber": "ORD-20251203-001",
    "status": "PENDING",
    "assignmentId": 456,
    "assignedCourierId": 4,
    "assignedAt": "2025-12-03T10:30:00Z",
    "timeoutAt": "2025-12-03T10:32:00Z"
  },
  "message": "Sipariş oluşturuldu ve kuryeye atandı"
}
```

**Test Script:**
```javascript
pm.test("Order created", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.orderId).to.exist;
    pm.environment.set("order_id", jsonData.data.orderId);
    pm.environment.set("assignment_id", jsonData.data.assignmentId);
});

pm.test("Auto-assigned to courier", function () {
    var jsonData = pm.response.json();
    pm.expect(jsonData.data.assignedCourierId).to.exist;
});
```

---

## 3️⃣ Courier - Assignments

### Get Pending Assignments

**Request:**
```http
GET {{base_url}}/api/v1/courier/assignments/pending
Authorization: Bearer {{courier_token}}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": [
    {
      "assignmentId": 456,
      "orderId": 123,
      "orderNumber": "ORD-20251203-001",
      "assignedAt": "2025-12-03T10:30:00Z",
      "timeoutAt": "2025-12-03T10:32:00Z",
      "status": "PENDING",
      "orderDetails": {
        "pickupAddress": "Beşiktaş Meydanı, İstanbul",
        "deliveryAddress": "Kadıköy İskelesi, İstanbul",
        "packageDescription": "Pizza (2 kutu, sıcak)",
        "deliveryFee": 50.00,
        "endCustomerName": "Ahmet Yılmaz"
      },
      "remainingSeconds": 90
    }
  ],
  "message": "Bekleyen atamalar"
}
```

---

### Accept Assignment

**Request:**
```http
POST {{base_url}}/api/v1/courier/assignments/{{assignment_id}}/accept
Authorization: Bearer {{courier_token}}
Content-Type: application/json

{}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": null,
  "message": "Sipariş kabul edildi"
}
```

**Test Script:**
```javascript
pm.test("Assignment accepted", function () {
    pm.response.to.have.status(200);
});
```

---

### Reject Assignment

**Request:**
```http
POST {{base_url}}/api/v1/courier/assignments/{{assignment_id}}/reject
Authorization: Bearer {{courier_token}}
Content-Type: application/json

{
  "reason": "Araç arızalı, şu an teslimat yapamıyorum"
}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": {
    "reassignedToNewCourier": true,
    "newCourierId": 5,
    "newAssignmentId": 457
  },
  "message": "Sipariş reddedildi, başka kuryeye atanıyor"
}
```

---

## 4️⃣ WebSocket Test

### WebSocket Connection

**Endpoint:**
```
ws://localhost:8081/ws
```

**STOMP Subscribe (Courier):**
```javascript
// JavaScript client örneği
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function(frame) {
    console.log('Connected: ' + frame);
    
    // Kurye'ye özel kuyruk (courier_id = 4)
    stompClient.subscribe('/queue/courier/4/assignments', function(message) {
        const notification = JSON.parse(message.body);
        console.log('New assignment:', notification);
        
        if (notification.type === 'NEW_ASSIGNMENT') {
            alert('Yeni sipariş atandı: ' + notification.assignmentId);
        } else if (notification.type === 'ASSIGNMENT_TIMEOUT') {
            alert('Atama zaman aşımına uğradı');
        }
    });
});
```

**STOMP Subscribe (Business):**
```javascript
// Business için sipariş durumu bildirimleri
stompClient.subscribe('/queue/business/' + businessId + '/orders', function(message) {
    const notification = JSON.parse(message.body);
    console.log('Order status update:', notification);
    
    if (notification.type === 'ORDER_STATUS_UPDATE') {
        console.log('Order ' + notification.orderId + ': ' + notification.status);
    }
});
```

**WebSocket Message Format (NEW_ASSIGNMENT):**
```json
{
  "type": "NEW_ASSIGNMENT",
  "assignmentId": 456,
  "orderId": 123,
  "assignedAt": "2025-12-03T10:30:00Z",
  "timeoutAt": "2025-12-03T10:32:00Z",
  "orderDetails": {
    "pickupAddress": "Beşiktaş Meydanı, İstanbul",
    "deliveryAddress": "Kadıköy İskelesi, İstanbul",
    "packageDescription": "Pizza (2 kutu)",
    "deliveryFee": 50.00,
    "endCustomerName": "Ahmet Yılmaz"
  }
}
```

**WebSocket Message Format (ASSIGNMENT_TIMEOUT):**
```json
{
  "type": "ASSIGNMENT_TIMEOUT",
  "assignmentId": 456,
  "message": "Atama zaman aşımına uğradı"
}
```

---

## 5️⃣ Admin - Monitoring

### Get Order Assignments

**Request:**
```http
GET {{base_url}}/api/v1/admin/orders/{{order_id}}/assignments
Authorization: Bearer {{admin_token}}
```

**Expected Response:**
```json
{
  "code": 200,
  "data": [
    {
      "assignmentId": 456,
      "courierId": 4,
      "courierName": "Ali Kurye",
      "assignedAt": "2025-12-03T10:30:00Z",
      "status": "TIMEOUT",
      "responseAt": "2025-12-03T10:32:05Z",
      "assignmentType": "AUTO"
    },
    {
      "assignmentId": 457,
      "courierId": 5,
      "courierName": "Ayşe Kurye",
      "assignedAt": "2025-12-03T10:32:10Z",
      "status": "ACCEPTED",
      "responseAt": "2025-12-03T10:32:30Z",
      "assignmentType": "REASSIGNMENT"
    }
  ],
  "message": "Sipariş atama geçmişi"
}
```

---

## 🧪 Test Senaryoları

### Senaryo 1: Başarılı Atama ve Kabul

```
1. Business paket yükler → POST /business/orders
2. Sistem FIFO'dan ilk kuryeyi seçer
3. Kurye WebSocket'ten bildirim alır
4. Kurye bekleyen atamaları görür → GET /courier/assignments/pending
5. Kurye kabul eder → POST /assignments/{id}/accept
6. Business WebSocket'ten "ASSIGNED" bildirimi alır
✅ Sipariş durumu: ASSIGNED
```

### Senaryo 2: Red ve Yeniden Atama

```
1. Business paket yükler
2. Kurye 1'e atanır
3. Kurye 1 reddeder → POST /assignments/{id}/reject
4. Sistem otomatik olarak Kurye 2'ye atar
5. Kurye 2 WebSocket bildirimi alır
6. Kurye 2 kabul eder
✅ Sipariş Kurye 2'ye atandı
```

### Senaryo 3: Timeout ve Otomatik Yeniden Atama

```
1. Business paket yükler
2. Kurye 1'e atanır (timeout: 2 dakika)
3. Kurye 1 cevap vermez
4. 2 dakika sonra sistem otomatik timeout yapar
5. Kurye 1'e timeout bildirimi gider (WebSocket)
6. Sistem otomatik olarak Kurye 2'ye atar
7. Kurye 2 kabul eder
✅ Sipariş Kurye 2'ye atandı
```

---

## 📊 Performance Test

### Load Test Script (K6)

```javascript
import http from 'k6/http';
import { check, sleep } from 'k6';

export const options = {
  stages: [
    { duration: '30s', target: 10 },
    { duration: '1m', target: 50 },
    { duration: '30s', target: 0 },
  ],
};

export default function () {
  const url = 'http://localhost:8081/api/v1/business/orders';
  const payload = JSON.stringify({
    endCustomerName: 'Test Customer',
    endCustomerPhone: '+905551234567',
    pickupAddress: 'Pickup Location',
    deliveryAddress: 'Delivery Location',
    packageDescription: 'Test Package',
    deliveryFee: 50.00,
    paymentType: 'CASH',
  });

  const params = {
    headers: {
      'Content-Type': 'application/json',
      'Authorization': 'Bearer <TOKEN>',
    },
  };

  const res = http.post(url, payload, params);
  check(res, { 'status was 200': (r) => r.status == 200 });
  sleep(1);
}
```

---

## 🔍 Troubleshooting

### WebSocket bağlanmıyor

**Çözüm:**
```bash
# CORS ayarlarını kontrol et
# WebSocketConfig.java içinde setAllowedOriginPatterns("*")
```

### Timeout çalışmıyor

**Çözüm:**
```bash
# application.properties
spring.task.scheduling.pool.size=5
order.assignment.timeout.minutes=2
```

### Bildirim gelmiyor

**Çözüm:**
```bash
# WebSocket bağlantısını test et
# Browser console:
const socket = new SockJS('http://localhost:8081/ws');
socket.onopen = () => console.log('Connected!');
```

---

## 📝 Notlar

1. **Timeout Süresi**: Varsayılan 2 dakika (`application.properties` ile değiştirilebilir)
2. **WebSocket Protokol**: STOMP over SockJS
3. **Queue Naming**: `/queue/courier/{courierId}/assignments`
4. **FIFO Sıralama**: `on_duty_since` ASC
5. **Reassignment**: Otomatik (red veya timeout sonrası)

---

## 🚀 Postman Collection Import

**JSON Export (örnek):**
```json
{
  "info": {
    "name": "Order Assignment System",
    "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"
  },
  "item": [
    {
      "name": "1. Authentication",
      "item": [...]
    },
    {
      "name": "2. Business - Create Order",
      "request": {
        "method": "POST",
        "header": [
          {
            "key": "Authorization",
            "value": "Bearer {{business_token}}"
          }
        ],
        "url": {
          "raw": "{{base_url}}/api/v1/business/orders",
          "host": ["{{base_url}}"],
          "path": ["api", "v1", "business", "orders"]
        },
        "body": {
          "mode": "raw",
          "raw": "{\n  \"endCustomerName\": \"Ahmet Yılmaz\",\n  \"pickupAddress\": \"Beşiktaş\",\n  \"deliveryAddress\": \"Kadıköy\",\n  \"packageDescription\": \"Pizza\",\n  \"deliveryFee\": 50.00\n}"
        }
      }
    }
  ]
}
```

---

**Bu döküman ile Postman'de tüm API'leri test edebilir ve WebSocket bildirimleri izleyebilirsiniz!** ✅

