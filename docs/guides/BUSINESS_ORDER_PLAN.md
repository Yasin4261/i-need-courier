# 📦 Güncellenmiş Plan - İşletme Paket/Sipariş Yönetimi

## ✅ Mevcut Durum Analizi

### 🗄️ Veritabanı Durumu

**ORDERS TABLOSU ZATEN VAR!** ✅

Mevcut `orders` tablosu çok kapsamlı ve ihtiyaçlarımızı karşılıyor:

```sql
orders tablosu:
├── id (BIGSERIAL PRIMARY KEY)
├── order_number (VARCHAR UNIQUE)
├── status (order_status ENUM)
├── priority (order_priority ENUM)
├── business_id (FOREIGN KEY → businesses)
├── courier_id (FOREIGN KEY → couriers)
├── coordinator_id (FOREIGN KEY → coordinators)
│
├── 📍 Pickup Bilgileri:
│   ├── pickup_address
│   ├── pickup_address_description
│   └── pickup_contact_person
│
├── 📍 Delivery Bilgileri:
│   ├── delivery_address
│   ├── delivery_address_description
│   ├── end_customer_name
│   └── end_customer_phone
│
├── 📦 Paket Bilgileri:
│   ├── package_description
│   ├── package_weight
│   └── package_count
│
├── 💰 Ödeme Bilgileri:
│   ├── payment_type (ENUM)
│   ├── delivery_fee
│   └── collection_amount
│
├── 📝 Notlar:
│   ├── courier_notes
│   └── business_notes
│
└── ⏰ Zaman Bilgileri:
    ├── scheduled_pickup_time
    ├── estimated_delivery_time
    ├── order_date
    ├── created_at
    └── updated_at
```

### 📊 Mevcut Enum'lar

```sql
✅ order_status ENUM:
   - PENDING
   - ASSIGNED
   - PICKED_UP
   - IN_TRANSIT
   - DELIVERED
   - CANCELLED
   - RETURNED

✅ order_priority ENUM:
   - NORMAL
   - HIGH
   - URGENT

✅ payment_type ENUM:
   - CASH
   - CREDIT_CARD
   - BUSINESS_ACCOUNT
   - CASH_ON_DELIVERY
   - ONLINE
```

---

## 🎯 YENİ PLAN - Database Güncellemesi Gerekmez!

### ✅ Yapacaklarımız

#### 1. **Order Model'i Oluştur** (JPA Entity)
Veritabanındaki `orders` tablosu için Java entity

#### 2. **Order CRUD İşlemleri**
Business kullanıcıları için sipariş yönetimi

#### 3. **Clean Folder Structure**
```
src/main/java/com/api/demo/
│
├── 📦 business/
│   ├── controller/
│   │   └── BusinessOrderController.java
│   ├── service/
│   │   ├── BusinessOrderService.java (interface)
│   │   └── impl/
│   │       └── BusinessOrderServiceImpl.java
│   └── dto/
│       ├── OrderCreateRequest.java
│       ├── OrderUpdateRequest.java
│       ├── OrderResponse.java
│       └── OrderListResponse.java
│
├── 📦 model/
│   ├── Order.java              ← YENİ (orders tablosu için)
│   ├── OrderTracking.java      ← YENİ (takip için)
│   ├── Business.java
│   ├── Courier.java
│   └── enums/
│       ├── OrderStatus.java    ← YENİ
│       ├── OrderPriority.java  ← YENİ
│       └── PaymentType.java    ← YENİ
│
└── 📦 repository/
    ├── OrderRepository.java         ← YENİ
    └── OrderTrackingRepository.java ← YENİ
```

---

## 🔧 İhtiyaç Duyulan Geliştirmeler

### Phase 1: Order Entity & Repository ✅

1. **Order.java** - JPA Entity
   ```java
   @Entity
   @Table(name = "orders")
   public class Order {
       @Id
       @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(name = "order_number", unique = true)
       private String orderNumber;
       
       @ManyToOne
       @JoinColumn(name = "business_id")
       private Business business;
       
       @Enumerated(EnumType.STRING)
       private OrderStatus status;
       
       // ... tüm diğer alanlar
   }
   ```

2. **OrderRepository.java**
   ```java
   public interface OrderRepository extends JpaRepository<Order, Long> {
       List<Order> findByBusinessId(Long businessId);
       List<Order> findByBusinessIdAndStatus(Long businessId, OrderStatus status);
       Optional<Order> findByOrderNumber(String orderNumber);
   }
   ```

3. **Enum Classes**
   - OrderStatus
   - OrderPriority  
   - PaymentType

### Phase 2: Business Order Service ✅

**BusinessOrderService.java**
```java
public interface BusinessOrderService {
    // CREATE
    OrderResponse createOrder(OrderCreateRequest request, Long businessId);
    
    // READ
    OrderResponse getOrderById(Long orderId, Long businessId);
    List<OrderResponse> getAllOrders(Long businessId, OrderStatus status);
    List<OrderResponse> getOrdersByStatus(Long businessId, OrderStatus status);
    
    // UPDATE
    OrderResponse updateOrder(Long orderId, OrderUpdateRequest request, Long businessId);
    
    // DELETE
    void deleteOrder(Long orderId, Long businessId);
    
    // CANCEL
    OrderResponse cancelOrder(Long orderId, Long businessId, String reason);
}
```

### Phase 3: REST Controller ✅

**BusinessOrderController.java**
```java
@RestController
@RequestMapping("/api/v1/business/orders")
public class BusinessOrderController {
    
    // POST /api/v1/business/orders
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
        @RequestBody @Valid OrderCreateRequest request,
        @AuthenticationPrincipal JwtAuthenticationToken auth
    );
    
    // GET /api/v1/business/orders
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders(
        @RequestParam(required = false) OrderStatus status
    );
    
    // GET /api/v1/business/orders/{orderId}
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
        @PathVariable Long orderId
    );
    
    // PUT /api/v1/business/orders/{orderId}
    @PutMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
        @PathVariable Long orderId,
        @RequestBody @Valid OrderUpdateRequest request
    );
    
    // DELETE /api/v1/business/orders/{orderId}
    @DeleteMapping("/{orderId}")
    public ResponseEntity<ApiResponse<Void>> deleteOrder(
        @PathVariable Long orderId
    );
    
    // POST /api/v1/business/orders/{orderId}/cancel
    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
        @PathVariable Long orderId,
        @RequestParam(required = false) String reason
    );
}
```

---

## 📝 DTO Tasarımı

### OrderCreateRequest.java
```java
public class OrderCreateRequest {
    @NotBlank
    private String pickupAddress;
    
    private String pickupAddressDescription;
    
    private String pickupContactPerson;
    
    @NotBlank
    private String deliveryAddress;
    
    private String deliveryAddressDescription;
    
    @NotBlank
    private String endCustomerName;
    
    @NotBlank
    @Pattern(regexp = "^\\+90\\d{10}$")
    private String endCustomerPhone;
    
    private String packageDescription;
    
    @Positive
    private BigDecimal packageWeight;
    
    @Min(1)
    private Integer packageCount = 1;
    
    @NotNull
    private OrderPriority priority = OrderPriority.NORMAL;
    
    @NotNull
    private PaymentType paymentType;
    
    @NotNull
    @Positive
    private BigDecimal deliveryFee;
    
    private BigDecimal collectionAmount = BigDecimal.ZERO;
    
    private String businessNotes;
    
    private LocalDateTime scheduledPickupTime;
}
```

### OrderResponse.java
```java
public class OrderResponse {
    private Long orderId;
    private String orderNumber;
    private OrderStatus status;
    private OrderPriority priority;
    
    // Business info
    private Long businessId;
    private String businessName;
    
    // Courier info (nullable)
    private Long courierId;
    private String courierName;
    
    // Addresses
    private String pickupAddress;
    private String deliveryAddress;
    
    // Customer
    private String endCustomerName;
    private String endCustomerPhone;
    
    // Package
    private String packageDescription;
    private BigDecimal packageWeight;
    private Integer packageCount;
    
    // Payment
    private PaymentType paymentType;
    private BigDecimal deliveryFee;
    private BigDecimal collectionAmount;
    
    // Notes
    private String businessNotes;
    private String courierNotes;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime scheduledPickupTime;
    private LocalDateTime estimatedDeliveryTime;
}
```

---

## 🔒 Authorization Rules

### Business Users Can:
- ✅ Create orders
- ✅ View **ONLY their own orders**
- ✅ Update orders **ONLY in PENDING status**
- ✅ Cancel orders **ONLY in PENDING/ASSIGNED status**
- ✅ Delete orders **ONLY in PENDING status**
- ❌ Cannot modify orders after PICKED_UP

### Implementation:
```java
// Her istekte JWT'den business_id alınır
Long businessId = extractBusinessIdFromJWT(token);

// Order ownership kontrolü
if (!order.getBusiness().getId().equals(businessId)) {
    throw new UnauthorizedAccessException("You can only access your own orders");
}

// Status-based operations
if (order.getStatus() != OrderStatus.PENDING) {
    throw new BusinessException("Cannot modify order in current status");
}
```

---

## 📊 Status Flow

```
Business Actions:
─────────────────
CREATE → PENDING
         │
         ├→ UPDATE (allowed in PENDING)
         ├→ DELETE (allowed in PENDING)
         └→ CANCEL (allowed in PENDING/ASSIGNED)

Courier/System Actions:
───────────────────────
PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED
                ↓
            CANCELLED / RETURNED
```

---

## 🧪 API Examples

### 1. Create Order
```bash
POST /api/v1/business/orders
Authorization: Bearer {business_token}

{
  "pickupAddress": "Kadıköy, Istanbul",
  "pickupContactPerson": "Mehmet",
  "deliveryAddress": "Beşiktaş, Istanbul",
  "endCustomerName": "Ahmet Yılmaz",
  "endCustomerPhone": "+905551234567",
  "packageDescription": "2x Pizza",
  "packageWeight": 1.5,
  "packageCount": 2,
  "priority": "NORMAL",
  "paymentType": "CASH",
  "deliveryFee": 25.00,
  "businessNotes": "Sıcak tutulmalı"
}

Response: 201 Created
{
  "success": true,
  "code": 201,
  "data": {
    "orderId": 1,
    "orderNumber": "ORD-20251107-001",
    "status": "PENDING",
    "createdAt": "2025-11-07T10:30:00"
  }
}
```

### 2. Get All Orders
```bash
GET /api/v1/business/orders
GET /api/v1/business/orders?status=PENDING

Response: 200 OK
{
  "success": true,
  "data": [
    {
      "orderId": 1,
      "orderNumber": "ORD-20251107-001",
      "status": "PENDING",
      ...
    }
  ]
}
```

---

## ✅ Yapılacaklar Listesi

### Adım 1: Model & Repository
- [ ] Order.java entity
- [ ] OrderTracking.java entity
- [ ] OrderStatus enum
- [ ] OrderPriority enum
- [ ] PaymentType enum
- [ ] OrderRepository interface
- [ ] OrderTrackingRepository interface

### Adım 2: DTOs
- [ ] OrderCreateRequest
- [ ] OrderUpdateRequest
- [ ] OrderResponse
- [ ] OrderListResponse

### Adım 3: Service Layer
- [ ] BusinessOrderService interface
- [ ] BusinessOrderServiceImpl
- [ ] Order validation
- [ ] Authorization helpers

### Adım 4: Controller
- [ ] BusinessOrderController
- [ ] Exception handlers
- [ ] JWT extraction

### Adım 5: Testing
- [ ] Unit tests
- [ ] Integration tests
- [ ] API tests

---

## ❓ Sorular

1. **Order Number Format**: 
   - Auto-generate: `ORD-20251107-001` şeklinde mi?

2. **Delivery Fee**:
   - Business mi belirleyecek?
   - Yoksa otomatik hesaplama mı? (mesafe/ağırlık bazlı)

3. **Scheduled Pickup Time**:
   - Zorunlu mu? Yoksa opsiyonel mi?

4. **Collection Amount**:
   - CASH_ON_DELIVERY için kullanılacak mı?

5. **Priority**:
   - URGENT orders için özel işlem var mı?

---

## 🚀 Başlamak İçin Onay

**DATABASE HAZIR!** ✅ Migration gerekmez.

Sadece:
1. Order entity & repository
2. Business package altında controller/service/dto
3. Authorization logic
4. API endpoints

Hepsini yapmam için izin ver! 💪

Başlayalım mı bebeğim? 🎯

