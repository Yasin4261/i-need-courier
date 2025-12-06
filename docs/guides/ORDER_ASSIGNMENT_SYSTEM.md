# Sipariş Atama Sistemi (Order Assignment System)

## 📋 İçindekiler

1. [Genel Bakış](#genel-bakış)
2. [Mimari](#mimari)
3. [Atama Algoritmaları](#atama-algoritmaları)
4. [Kurulum](#kurulum)
5. [Kullanım](#kullanım)
6. [API Endpoints](#api-endpoints)
7. [Konfigürasyon](#konfigürasyon)
8. [Test](#test)

---

## 🎯 Genel Bakış

Sipariş Atama Sistemi, oluşturulan siparişleri otomatik veya manuel olarak kuryelere atayan modüler bir sistemdir. Farklı atama algoritmaları ile iş gereksinimlerinize göre özelleştirilebilir.

### Özellikler

- ✅ **4 Farklı Atama Algoritması** (Round Robin, Load Balanced, Priority Based, Manual)
- ✅ **Strategy Pattern** ile genişletilebilir mimari
- ✅ **Business Rules** ile güvenli atama
- ✅ **Auto-assignment** desteği
- ✅ **Reassignment** özelliği
- ✅ **RESTful API** ile kolay entegrasyon

---

## 🏗️ Mimari

### Strategy Pattern

Sistem, Strategy Design Pattern kullanarak farklı atama algoritmalarını destekler:

```
AssignmentStrategy (Interface)
    ├── RoundRobinStrategy
    ├── LoadBalancedStrategy
    ├── PriorityBasedStrategy
    └── NearestCourierStrategy (TODO)
```

### Komponenler

```
OrderAssignmentController
    └── OrderAssignmentService
        ├── AssignmentStrategyFactory
        │   └── AssignmentStrategy (implementations)
        ├── OrderRepository
        └── CourierRepository
```

---

## 🧮 Atama Algoritmaları

### 1. Round Robin (Sırayla Atama)

**Açıklama:** Siparişleri kuryelere sırayla, adil bir şekilde dağıtır.

**Kullanım Senaryoları:**
- Tüm kuryelerin eşit yük alması istendiğinde
- Basit ve öngörülebilir dağıtım gerektiğinde

**Kurallar:**
- Sadece ONLINE durumundaki kuryeler
- Maksimum 5 aktif sipariş/kurye

**Kod:**
```java
@Component
public class RoundRobinStrategy implements AssignmentStrategy {
    private final AtomicInteger currentIndex = new AtomicInteger(0);
    
    @Override
    public Optional<Courier> assignCourier(Order order, List<Courier> availableCouriers) {
        int index = currentIndex.getAndUpdate(i -> (i + 1) % availableCouriers.size());
        return Optional.of(availableCouriers.get(index));
    }
}
```

---

### 2. Load Balanced (Yük Dengeleme)

**Açıklama:** Siparişi en az aktif siparişe sahip kuryeye atar.

**Kullanım Senaryoları:**
- Kuryelerin iş yükünü dengelemek istendiğinde
- Bazı kuryeler daha hızlı bitirdiğinde

**Kurallar:**
- Sadece ONLINE durumundaki kuryeler
- Maksimum 10 aktif sipariş/kurye

**Örnek:**
```
Kurye A: 2 aktif sipariş
Kurye B: 5 aktif sipariş
Kurye C: 3 aktif sipariş

→ Yeni sipariş Kurye A'ya atanır (en az yük)
```

**Kod:**
```java
@Component
public class LoadBalancedStrategy implements AssignmentStrategy {
    @Override
    public Optional<Courier> assignCourier(Order order, List<Courier> availableCouriers) {
        return availableCouriers.stream()
                .min(Comparator.comparingLong(courier -> 
                        orderRepository.countActiveByCourierId(courier.getId())
                ));
    }
}
```

---

### 3. Priority Based (Öncelik Bazlı)

**Açıklama:** Sipariş önceliğine göre deneyimli kuryelere atar.

**Kullanım Senaryoları:**
- Acil siparişler için deneyimli kuryeler
- VIP müşteri siparişleri
- Değerli/hassas kargolar

**Kurallar:**
- URGENT siparişler → En fazla tamamlanmış siparişi olan kurye
- NORMAL/LOW siparişler → Herhangi bir uygun kurye
- URGENT için maksimum 8, diğerleri için 5 aktif sipariş

**Örnek:**
```
Sipariş: URGENT öncelik
Kurye A: 150 tamamlanmış sipariş
Kurye B: 80 tamamlanmış sipariş
Kurye C: 200 tamamlanmış sipariş

→ Kurye C'ye atanır (en deneyimli)
```

**Kod:**
```java
@Component
public class PriorityBasedStrategy implements AssignmentStrategy {
    @Override
    public Optional<Courier> assignCourier(Order order, List<Courier> availableCouriers) {
        if (order.getPriority() == OrderPriority.URGENT) {
            return availableCouriers.stream()
                    .max(Comparator.comparingLong(courier -> 
                            orderRepository.countCompletedByCourierId(courier.getId())
                    ));
        }
        return availableCouriers.stream().findFirst();
    }
}
```

---

### 4. Manual (Manuel Atama)

**Açıklama:** Admin tarafından manuel olarak kurye seçilir.

**Kullanım Senaryoları:**
- Özel durumlar
- Belirli bir kurye tercih edildiğinde
- Otomatik atama başarısız olduğunda

---

## 🚀 Kurulum

### 1. Gerekli Dosyaları Oluşturun

Terminal'de şu komutu çalıştırın:

```bash
cd /home/yasin/Desktop/repos/i-need-courier

# 1. Enum
cat > src/main/java/com/api/demo/model/enums/AssignmentAlgorithm.java << 'EOF'
package com.api.demo.model.enums;

public enum AssignmentAlgorithm {
    ROUND_ROBIN,
    LOAD_BALANCED,
    PRIORITY_BASED,
    NEAREST_COURIER,
    MANUAL
}
EOF

# 2. Strategy klasörü
mkdir -p src/main/java/com/api/demo/service/assignment

# 3. Interface
cat > src/main/java/com/api/demo/service/assignment/AssignmentStrategy.java << 'EOF'
package com.api.demo.service.assignment;

import com.api.demo.model.Courier;
import com.api.demo.model.Order;
import java.util.List;
import java.util.Optional;

public interface AssignmentStrategy {
    Optional<Courier> assignCourier(Order order, List<Courier> availableCouriers);
    boolean validateAssignment(Order order, Courier courier);
    String getAlgorithmName();
}
EOF

# 4-6. Strategy implementasyonları (RoundRobin, LoadBalanced, PriorityBased)
# 7. StrategyFactory
# 8. OrderAssignmentService
# 9. OrderAssignmentController

# Tüm dosyalar için yukarıdaki kod örneklerini kullanın
```

### 2. Repository Methodlarını Ekleyin

**OrderRepository.java** dosyasının sonuna ekleyin:

```java
/**
 * Count active orders for a courier
 */
@Query("SELECT COUNT(o) FROM Order o WHERE o.courier.id = :courierId " +
       "AND o.status IN ('ASSIGNED', 'PICKED_UP', 'IN_TRANSIT')")
long countActiveByCourierId(@Param("courierId") Long courierId);

/**
 * Count completed orders for a courier
 */
@Query("SELECT COUNT(o) FROM Order o WHERE o.courier.id = :courierId " +
       "AND o.status = 'DELIVERED'")
long countCompletedByCourierId(@Param("courierId") Long courierId);

/**
 * Find all pending orders
 */
List<Order> findByStatus(OrderStatus status);
```

**CourierRepository.java** dosyasının sonuna ekleyin:

```java
/**
 * Find all couriers with specific status
 */
List<Courier> findByStatus(Courier.CourierStatus status);
```

### 3. Order Entity'ye Field Ekleyin

**Order.java** dosyasında `updatedAt` field'ından sonra ekleyin:

```java
@Column(name = "assigned_at")
private LocalDateTime assignedAt;

// Getter/Setter
public LocalDateTime getAssignedAt() {
    return assignedAt;
}

public void setAssignedAt(LocalDateTime assignedAt) {
    this.assignedAt = assignedAt;
}
```

### 4. Database Migration

**V15__Add_assigned_at_to_orders.sql** oluşturun:

```sql
-- V15__Add_assigned_at_to_orders.sql
ALTER TABLE orders 
ADD COLUMN IF NOT EXISTS assigned_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_orders_assigned_at 
ON orders(assigned_at);

COMMENT ON COLUMN orders.assigned_at IS 'Timestamp when order was assigned to courier';
```

### 5. Configuration

**application.properties** dosyasına ekleyin:

```properties
# Order Assignment Configuration
order.assignment.default-algorithm=ROUND_ROBIN
order.assignment.max-orders-per-courier=5
order.assignment.auto-assign-enabled=true
```

### 6. Build ve Migration

```bash
# Compile
./mvnw clean compile

# Migration
./mvnw flyway:migrate

# Test
./mvnw test
```

---

## 💻 Kullanım

### Java Code

```java
@Autowired
private OrderAssignmentService assignmentService;

// Otomatik atama (default algorithm)
Optional<Courier> courier = assignmentService.autoAssignOrder(orderId);

// Belirli algorithm ile atama
Optional<Courier> courier = assignmentService.assignOrder(
    orderId, 
    AssignmentAlgorithm.LOAD_BALANCED
);

// Manuel atama
boolean success = assignmentService.manualAssignOrder(orderId, courierId);

// Yeniden atama
Optional<Courier> courier = assignmentService.reassignOrder(
    orderId, 
    AssignmentAlgorithm.PRIORITY_BASED
);
```

### Business Service Entegrasyonu

Sipariş oluştururken otomatik atama:

```java
@Service
public class BusinessOrderServiceImpl implements BusinessOrderService {
    
    @Autowired
    private OrderAssignmentService assignmentService;
    
    @Transactional
    public OrderResponse createOrder(OrderCreateRequest request) {
        // 1. Sipariş oluştur
        Order order = orderRepository.save(newOrder);
        
        // 2. Otomatik ata
        assignmentService.autoAssignOrder(order.getId());
        
        return mapToResponse(order);
    }
}
```

---

## 🌐 API Endpoints

### 1. Otomatik Atama

**Endpoint:** `POST /api/v1/orders/assignment/{orderId}/auto`

**Authorization:** ADMIN veya BUSINESS rolü gerekli

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/orders/assignment/123/auto \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response (Success):**
```json
{
  "success": true,
  "message": "Order assigned successfully",
  "courierId": 456,
  "courierName": "Ahmet Yılmaz"
}
```

**Response (Failed):**
```json
{
  "success": false,
  "message": "No available courier found"
}
```

---

### 2. Algoritma ile Atama

**Endpoint:** `POST /api/v1/orders/assignment/{orderId}?algorithm={ALGORITHM}`

**Authorization:** ADMIN rolü gerekli

**Parametreler:**
- `algorithm`: `ROUND_ROBIN`, `LOAD_BALANCED`, `PRIORITY_BASED`

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/orders/assignment/123?algorithm=LOAD_BALANCED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Order assigned using LOAD_BALANCED",
  "courierId": 456,
  "courierName": "Mehmet Demir",
  "algorithm": "LOAD_BALANCED"
}
```

---

### 3. Manuel Atama

**Endpoint:** `POST /api/v1/orders/assignment/{orderId}/manual/{courierId}`

**Authorization:** ADMIN rolü gerekli

**Request:**
```bash
curl -X POST http://localhost:8080/api/v1/orders/assignment/123/manual/456 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Order manually assigned"
}
```

---

### 4. Yeniden Atama

**Endpoint:** `POST /api/v1/orders/assignment/{orderId}/reassign?algorithm={ALGORITHM}`

**Authorization:** ADMIN rolü gerekli

**Request:**
```bash
curl -X POST "http://localhost:8080/api/v1/orders/assignment/123/reassign?algorithm=PRIORITY_BASED" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

**Response:**
```json
{
  "success": true,
  "message": "Order reassigned successfully",
  "courierId": 789
}
```

---

## ⚙️ Konfigürasyon

### Application Properties

```properties
# Default atama algoritması
order.assignment.default-algorithm=ROUND_ROBIN

# Kurye başına maksimum aktif sipariş sayısı
order.assignment.max-orders-per-courier=5

# Otomatik atama aktif mi?
order.assignment.auto-assign-enabled=true

# Timeout ayarları (ms)
order.assignment.timeout=5000

# Retry ayarları
order.assignment.max-retries=3
order.assignment.retry-delay=1000
```

### Algoritma Seçim Stratejisi

Hangi algoritmayı ne zaman kullanmalı?

| Durum | Önerilen Algoritma | Sebep |
|-------|-------------------|-------|
| Normal günlük işlem | ROUND_ROBIN | Adil dağıtım |
| Yoğun saatler | LOAD_BALANCED | Yük dengeleme |
| Acil siparişler | PRIORITY_BASED | Deneyimli kurye |
| Özel durumlar | MANUAL | İnsan kontrolü |

---

## 🧪 Test

### Unit Test Örneği

```java
@SpringBootTest
class OrderAssignmentServiceTest {
    
    @Autowired
    private OrderAssignmentService assignmentService;
    
    @Test
    void testAutoAssignOrder() {
        // Given
        Long orderId = 123L;
        
        // When
        Optional<Courier> courier = assignmentService.autoAssignOrder(orderId);
        
        // Then
        assertThat(courier).isPresent();
        assertThat(courier.get().getStatus()).isEqualTo(CourierStatus.ONLINE);
    }
    
    @Test
    void testLoadBalancedAssignment() {
        // Given
        Long orderId = 124L;
        
        // When
        Optional<Courier> courier = assignmentService.assignOrder(
            orderId, 
            AssignmentAlgorithm.LOAD_BALANCED
        );
        
        // Then
        assertThat(courier).isPresent();
        // Verify it's the courier with least active orders
    }
}
```

### Integration Test

```bash
# 1. Kurye oluştur (ONLINE)
curl -X POST http://localhost:8080/api/v1/couriers \
  -H "Content-Type: application/json" \
  -d '{"name":"Test Kurye","email":"test@test.com","phone":"+905551234567"}'

# 2. Sipariş oluştur
curl -X POST http://localhost:8080/api/v1/business/orders \
  -H "Authorization: Bearer BUSINESS_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"pickupAddress":"A","deliveryAddress":"B","deliveryFee":50.00}'

# 3. Otomatik ata
curl -X POST http://localhost:8080/api/v1/orders/assignment/1/auto \
  -H "Authorization: Bearer ADMIN_TOKEN"

# 4. Kontrol et
curl http://localhost:8080/api/v1/orders/1 \
  -H "Authorization: Bearer TOKEN"
```

### Load Test

```bash
# Apache Bench ile yük testi
ab -n 1000 -c 10 -H "Authorization: Bearer TOKEN" \
  http://localhost:8080/api/v1/orders/assignment/auto

# Expected results:
# - All requests successful
# - No database overload
# - Fair distribution among couriers
```

---

## 📊 Monitoring

### Log Örnekleri

```
INFO  OrderAssignmentService - Assigning order 123 using ROUND_ROBIN
INFO  OrderAssignmentService - Order 123 assigned to courier 456 using ROUND_ROBIN
WARN  OrderAssignmentService - No available couriers for order 124
ERROR OrderAssignmentService - Failed to assign order 125 using PRIORITY_BASED
```

### Metrics

İzlenmesi gereken metrikler:

- **Assignment Success Rate:** Başarılı atama oranı
- **Average Assignment Time:** Ortalama atama süresi
- **Courier Utilization:** Kurye kullanım oranı
- **Algorithm Performance:** Algoritma performansı
- **Failed Assignments:** Başarısız atama sayısı

---

## 🔧 Troubleshooting

### Yaygın Sorunlar

#### 1. "No available courier found"

**Sebep:** Hiç ONLINE kurye yok

**Çözüm:**
```sql
-- Aktif kuryeleri kontrol et
SELECT * FROM couriers WHERE status = 'ONLINE';

-- Kurye durumunu güncelle
UPDATE couriers SET status = 'ONLINE' WHERE id = 1;
```

#### 2. "Order not in PENDING status"

**Sebep:** Sipariş zaten atanmış veya iptal edilmiş

**Çözüm:**
```sql
-- Sipariş durumunu kontrol et
SELECT id, order_number, status FROM orders WHERE id = 123;

-- Gerekirse yeniden atama yap
POST /api/v1/orders/assignment/123/reassign?algorithm=ROUND_ROBIN
```

#### 3. "Courier not ONLINE"

**Sebep:** Seçilen kurye offline

**Çözüm:**
```bash
# Başka bir kuryeye manuel ata
curl -X POST http://localhost:8080/api/v1/orders/assignment/123/manual/456
```

---

## 🚀 Gelecek Geliştirmeler

### 1. Nearest Courier Algorithm

GPS lokasyonu kullanarak en yakın kuryeye atama:

```java
@Component
public class NearestCourierStrategy implements AssignmentStrategy {
    @Override
    public Optional<Courier> assignCourier(Order order, List<Courier> couriers) {
        // TODO: PostGIS kullanarak mesafe hesapla
        // TODO: Google Maps API entegrasyonu
        return courriers.stream()
            .min(Comparator.comparingDouble(courier -> 
                calculateDistance(order.getPickupAddress(), courier.getCurrentLocation())
            ));
    }
}
```

### 2. Machine Learning Tabanlı Atama

Geçmiş verilere göre en iyi kuryeyi tahmin etme:

```java
@Component
public class MLBasedStrategy implements AssignmentStrategy {
    @Autowired
    private MLModel model;
    
    @Override
    public Optional<Courier> assignCourier(Order order, List<Courier> couriers) {
        return model.predictBestCourier(order, couriers);
    }
}
```

### 3. Kafka Entegrasyonu

Async ve non-blocking atama için:

```java
@Service
public class AsyncOrderAssignmentService {
    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;
    
    public void publishOrderForAssignment(Long orderId) {
        kafkaTemplate.send("order.assignment", new OrderEvent(orderId));
    }
}
```

### 4. Real-time Dashboard

Admin panel'de gerçek zamanlı atama görüntüleme:
- Bekleyen siparişler
- Aktif kuryeler
- Atama istatistikleri
- Algoritma performans karşılaştırması

---

## 📚 Referanslar

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Strategy Design Pattern](https://refactoring.guru/design-patterns/strategy)
- [Order Management Best Practices](https://example.com)

---

## 📝 Changelog

### v1.0.0 (2025-12-02)
- ✅ Round Robin algoritması
- ✅ Load Balanced algoritması
- ✅ Priority Based algoritması
- ✅ Manuel atama
- ✅ RESTful API endpoints
- ✅ Dokümantasyon

### Gelecek Versiyonlar
- 🔄 v1.1.0: Nearest Courier algoritması
- 🔄 v1.2.0: Kafka async processing
- 🔄 v2.0.0: ML-based assignment

---

## 👥 Katkıda Bulunma

Yeni algoritma eklemek için:

1. `AssignmentStrategy` interface'ini implement edin
2. `@Component` annotation ekleyin
3. `getAlgorithmName()` metodunda enum adını dönün
4. Test yazın
5. Dokümante edin

---

## 📄 License

MIT License - Proje lisansı

---

## 🆘 Destek

Sorularınız için:
- Email: support@i-need-courier.com
- Slack: #order-assignment-help
- Issue: GitHub Issues

---

**Son Güncelleme:** 2 Aralık 2025
**Versiyon:** 1.0.0
**Yazar:** Development Team

