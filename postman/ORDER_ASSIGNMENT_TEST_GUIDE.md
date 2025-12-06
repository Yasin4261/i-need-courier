# 🧪 Order Assignment System - Postman Test Rehberi

## 📦 Dosyalar

- `Order_Assignment_System.postman_collection.json` - Test collection
- `Order_Assignment_Local.postman_environment.json` - Environment variables
- `websocket-test.html` - WebSocket test sayfası

---

## 🚀 Hızlı Başlangıç

### 1. Import

**Postman'de:**
1. File → Import
2. `Order_Assignment_System.postman_collection.json` seç
3. `Order_Assignment_Local.postman_environment.json` seç
4. Environment'ı aktif et (sağ üst dropdown)

### 2. Önkoşullar

```bash
# Uygulama çalışmalı
./mvnw spring-boot:run

# Migration uygulanmalı
./mvnw flyway:migrate

# En az 1 kurye on-duty olmalı
docker exec courier-postgres psql -U courier_user -d courier_db -c "
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source) 
VALUES (4, 1, now(), 'manual_test') 
ON CONFLICT (courier_id) DO UPDATE SET on_duty_since=now();
"
```

---

## 🎯 Test Adımları

### Adım 1: Authentication
1. **Business Login** → Send → Token `{{business_token}}` değişkenine kaydedilir
2. **Courier Login** → Send → Token `{{courier_token}}` değişkenine kaydedilir

### Adım 2: Order Assignment Test
1. **Create Order (Auto-Assign)** → Send
   - Sipariş oluşturulur
   - FIFO sırasındaki kuryeye atanır
   - WebSocket bildirimi gider
   - `{{order_id}}` ve `{{assignment_id}}` kaydedilir

2. **Get Pending Assignments** → Send
   - Kurye bekleyen atamalarını görür

3. **Accept Assignment** VEYA **Reject Assignment** → Send
   - Accept: Order status → ASSIGNED, kurye sıranın sonuna
   - Reject: Otomatik olarak bir sonraki kuryeye atar

### Adım 3: Timeout Test
1. Sipariş oluştur
2. 2 dakika bekle
3. Sistem otomatik timeout yapar ve yeniden atar

---

## 🔌 WebSocket Test

### Option 1: HTML Page
```bash
# Browser'da aç:
cd /home/yasin/Desktop/repos/i-need-courier/postman
python3 -m http.server 8000

# Tarayıcı: http://localhost:8000/websocket-test.html
```

### Option 2: Browser Console
```javascript
const socket = new SockJS('http://localhost:8081/ws');
const stompClient = Stomp.over(socket);

stompClient.connect({}, function() {
    stompClient.subscribe('/queue/courier/4/assignments', function(msg) {
        console.log('Bildirim:', JSON.parse(msg.body));
    });
});
```

---

## 📊 Test Senaryoları

### ✅ Senaryo 1: Happy Path
```
Business Login → Courier Login → Create Order → 
Get Pending → Accept → ✅ ASSIGNED
```

### ❌ Senaryo 2: Rejection
```
Create Order → Reject → 
Otomatik olarak bir sonraki kuryeye atar
```

### ⏱️ Senaryo 3: Timeout
```
Create Order → 2 dakika bekle → 
Timeout → Otomatik reassignment
```

---

## 🐛 Troubleshooting

**Problem: "Aktif kurye yok"**
```bash
docker exec courier-postgres psql -U courier_user -d courier_db -c "
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source) 
VALUES (4, 1, now(), 'manual_test');
"
```

**Problem: WebSocket bağlanmıyor**
- Uygulama çalışıyor mu? `curl http://localhost:8081/actuator/health`
- Browser console'da error var mı?

**Problem: Timeout çalışmıyor**
- `@EnableScheduling` eklendi mi?
- `application.properties`: `order.assignment.timeout.minutes=2`

---

## ✅ Test Checklist

- [ ] Migration V15 uygulandı
- [ ] En az 1 kurye on-duty
- [ ] Business/Courier login başarılı
- [ ] Sipariş oluşturuldu (auto-assign)
- [ ] WebSocket bildirimi geldi
- [ ] Accept çalıştı
- [ ] Reject çalıştı
- [ ] Timeout test edildi

**Tüm testler başarılı → Production Ready!** 🎉

