# 🏛️ Hexagonal Architecture (Ports & Adapters) Rehberi

## 📚 İçindekiler
1. [Hexagonal Mimari Nedir?](#hexagonal-mimari-nedir)
2. [MVC vs Hexagonal Mimari](#mvc-vs-hexagonal-mimari)
3. [Katman Yapısı ve Sorumluluklar](#katman-yapısı-ve-sorumluluklar)
4. [Port ve Adapter Kavramları](#port-ve-adapter-kavramları)
5. [Practical Implementation](#practical-implementation)
6. [Kod Örnekleri](#kod-örnekleri)
7. [Best Practices](#best-practices)
8. [Kurye Projesi için Özel Açıklamalar](#kurye-projesi-için-özel-açıklamalar)

---

## 🎯 Hexagonal Mimari Nedir?

Hexagonal Architecture (Altıgen Mimari), Alistair Cockburn tarafından geliştirilen bir yazılım mimarisi desenidir. **"Ports and Adapters"** olarak da bilinir.

### Temel Prensip:
> **İş mantığı (Domain) merkezdedir ve dış dünyayla sadece portlar/adapterler aracılığıyla iletişim kurar.**

```
    🌐 Web UI
        ↕️ (Adapter)
    📊 Database ←→ 🎯 DOMAIN ←→ 🔔 Notifications
        ↕️ (Adapter)      ↕️ (Adapter)
    📨 Message Queue
```

### Neden Hexagonal?
- Altıgen şekli sadece görsel temsil
- Gerçekte **6 taraf** yok, **sınırsız port** olabilir
- Önemli olan **içerideki domain**'in **dışarıdan izole** olması

---

## 🔄 MVC vs Hexagonal Mimari

### 📊 MVC Mimarisinde (Bildiğin Yapı):

```
┌─────────────────────────────────────┐
│           Controller                │  ← Web istekleri buraya gelir
│   (CourierController.java)          │
├─────────────────────────────────────┤
│           Service                   │  ← İş mantığı burada
│   (CourierService.java)             │
├─────────────────────────────────────┤
│         Repository                  │  ← Database erişimi burada
│   (CourierRepository.java)          │
├─────────────────────────────────────┤
│           Model                     │  ← Entity'ler burada
│   (Courier.java)                    │
└─────────────────────────────────────┘
```

**MVC'nin Sorunları:**
- Service katmanı hem iş mantığı hem de teknoloji detayları içerir
- Database değişikliği tüm katmanları etkiler
- Test etmek zor (database'e bağımlı)
- Framework değişikliği büyük refactoring gerektirir

### 🏛️ Hexagonal Mimaride:

```
              ┌─────────────────────────┐
              │    🌐 Web Adapter       │
              │   (REST Controllers)    │
              └────────────┬────────────┘
                          │ Port
              ┌───────────▼────────────┐
              │    📋 Application      │
              │     (Use Cases)        │
              └───────────┬────────────┘
                          │
              ┌───────────▼────────────┐
              │      🎯 DOMAIN         │  ← İş mantığı burada (merkez)
              │   (Business Logic)     │
              └───────────┬────────────┘
                          │ Port
              ┌───────────▼────────────┐
              │   💾 Database Adapter  │
              │     (JPA/JDBC)         │
              └────────────────────────┘
```

**Hexagonal'ın Avantajları:**
- İş mantığı teknoloji bağımsız
- Database/Framework değişikliği sadece adapter'ı etkiler
- Her katman ayrı ayrı test edilebilir
- Yeni özellikler eklemek kolay

---

## 🏗️ Katman Yapısı ve Sorumluluklar

### 1. 🎯 **DOMAIN Katmanı** (Merkez - En Önemli!)

```java
// Domain katmanı hiçbir framework'e bağımlı OLMAZ!
// Spring, JPA, Jackson annotation'ları YASAK!

domain/
├── model/           # İş nesneleri (Courier, Order)
├── port/
│   ├── input/       # Giren portlar (UseCaselar)
│   └── output/      # Çıkan portlar (Repository interfaceleri)
├── service/         # Domain servisleri (iş kuralları)
├── valueobject/     # Value objectler (Money, Address)
├── event/           # Domain eventleri
└── exception/       # Domain exception'ları
```

**MVC'deki Model'in gelişmiş hali ama:**
- Annotation'sız (JPA, JSON vb. YOK!)
- Sadece iş mantığı
- Framework bağımsız

### 2. 📋 **APPLICATION Katmanı** (Use Cases)

```java
application/
├── usecase/         # Use case implementasyonları
├── dto/             # Uygulama DTO'ları  
└── mapper/          # DTO ↔ Domain dönüşüm
```

**MVC'deki Service'in temiz hali:**
- Sadece iş akışı koordinasyonu
- Domain'i çağırır, teknoloji bilmez
- Input/Output port'ları kullanır

### 3. 🔌 **INFRASTRUCTURE Katmanı** (Adapterler)

```java
infrastructure/
├── adapter/
│   ├── input/       # Gelen adapterler (Web, Message)
│   └── output/      # Giden adapterler (DB, External API)
└── config/          # Konfigürasyonlar
```

**MVC'deki Controller + Repository'nin genişletilmiş hali:**
- Teknoloji detayları burada
- Framework annotation'ları burada
- Database/API çağrıları burada

---

## 🚪 Port ve Adapter Kavramları

### 🚪 **PORT (Kapı)**
Port = **Interface**. Dış dünyayla iletişim kuralları.

```java
// INPUT PORT (Domain'e giren kapı)
public interface CreateCourierUseCase {
    CourierResponse createCourier(CreateCourierRequest request);
}

// OUTPUT PORT (Domain'den çıkan kapı) 
public interface CourierRepository {
    Courier save(Courier courier);
    Optional<Courier> findById(Long id);
}
```

### 🔌 **ADAPTER (Fiş)**
Adapter = **Implementation**. Port'u implement eden concrete sınıf.

```java
// INPUT ADAPTER (Web'den gelen istekleri handle eder)
@RestController
public class CourierController {
    private final CreateCourierUseCase createCourierUseCase;
    
    @PostMapping("/couriers")
    public ResponseEntity<CourierResponse> create(@RequestBody CreateCourierRequest request) {
        return createCourierUseCase.createCourier(request);
    }
}

// OUTPUT ADAPTER (Database'e giden istekleri handle eder)
@Repository  
public class JdbcCourierRepository implements CourierRepository {
    // JDBC implementasyonu burada
}
```

### 🔄 Akış Şöyle:
```
Web Request → Controller (Input Adapter) 
            → UseCase (Application) 
            → Domain Service 
            → Repository Port → Repository Adapter (Output Adapter) 
            → Database
```

---

## 💻 Practical Implementation

### MVC'den Hexagonal'a Geçiş Adımları:

#### 1️⃣ **Eski MVC Yapısı:**
```java
@Entity
@RestController
public class CourierController {
    @Autowired
    private CourierService courierService;
    
    @PostMapping("/couriers")
    public Courier create(@RequestBody Courier courier) {
        return courierService.save(courier);
    }
}

@Service
public class CourierService {
    @Autowired
    private CourierRepository repository;
    
    public Courier save(Courier courier) {
        // İş kuralları + database çağrısı karışık
        return repository.save(courier);
    }
}
```

#### 2️⃣ **Yeni Hexagonal Yapısı:**

**Domain (Merkez):**
```java
// domain/model/Courier.java - Annotation YOK!
public class Courier {
    private Long id;
    private String name;
    private String email;
    // Sadece getter/setter, iş mantığı method'ları
}

// domain/port/input/CreateCourierUseCase.java
public interface CreateCourierUseCase {
    CourierResponse createCourier(CreateCourierRequest request);
}

// domain/port/output/CourierRepository.java  
public interface CourierRepository {
    Courier save(Courier courier);
    Optional<Courier> findById(Long id);
}
```

**Application (Use Cases):**
```java
// application/usecase/CreateCourierUseCaseImpl.java
@UseCase // Custom annotation
public class CreateCourierUseCaseImpl implements CreateCourierUseCase {
    
    private final CourierRepository courierRepository;
    private final CourierMapper courierMapper;
    
    @Override
    public CourierResponse createCourier(CreateCourierRequest request) {
        // 1. DTO → Domain dönüşüm
        Courier courier = courierMapper.toDomain(request);
        
        // 2. İş kuralları (domain service çağır)
        
        // 3. Kaydet
        Courier saved = courierRepository.save(courier);
        
        // 4. Domain → DTO dönüşüm  
        return courierMapper.toResponse(saved);
    }
}
```

**Infrastructure (Adapters):**
```java
// infrastructure/adapter/input/web/controller/CourierController.java
@RestController
@RequestMapping("/api/couriers")
public class CourierController {
    
    private final CreateCourierUseCase createCourierUseCase;
    
    @PostMapping
    public ResponseEntity<CourierResponse> create(@RequestBody CreateCourierWebRequest request) {
        CreateCourierRequest useCaseRequest = CourierWebMapper.toUseCaseRequest(request);
        CourierResponse response = createCourierUseCase.createCourier(useCaseRequest);
        return ResponseEntity.ok(response);
    }
}

// infrastructure/adapter/output/persistence/repository/JdbcCourierRepository.java
@Repository
public class JdbcCourierRepository implements CourierRepository {
    
    private final JdbcTemplate jdbcTemplate;
    private final CourierEntityMapper mapper;
    
    @Override
    public Courier save(Courier courier) {
        // Domain → Entity dönüşüm
        CourierEntity entity = mapper.toEntity(courier);
        
        // JDBC kaydetme işlemi
        // ...
        
        // Entity → Domain dönüşüm
        return mapper.toDomain(savedEntity);
    }
}
```

---

## 📝 Kod Örnekleri (Kurye Projesi)

### 🎯 Domain Layer Örnekleri:

```java
// domain/model/Courier.java
public class Courier {
    private Long id;
    private String name;
    private Email email;  // Value Object
    private Phone phone;  // Value Object
    private CourierStatus status;
    private Location currentLocation;  // Value Object
    
    // İş kuralı method'ları
    public void assignToOrder(Order order) {
        if (!this.isAvailable()) {
            throw new CourierNotAvailableException("Courier is not available");
        }
        this.status = CourierStatus.BUSY;
    }
    
    public boolean isAvailable() {
        return status == CourierStatus.ONLINE && currentLocation != null;
    }
    
    // Constructor, getter/setter...
}

// domain/valueobject/Email.java
public class Email {
    private final String value;
    
    public Email(String email) {
        if (!isValidEmail(email)) {
            throw new InvalidEmailException("Invalid email format");
        }
        this.value = email;
    }
    
    private boolean isValidEmail(String email) {
        // Email validation logic
        return email.contains("@");
    }
    
    public String getValue() {
        return value;
    }
}

// domain/port/input/AssignCourierUseCase.java
public interface AssignCourierUseCase {
    void assignCourierToOrder(AssignCourierRequest request);
}

// domain/port/output/CourierRepository.java
public interface CourierRepository {
    Optional<Courier> findById(Long id);
    List<Courier> findAvailableCouriers(Location location, double radiusKm);
    Courier save(Courier courier);
}
```

### 📋 Application Layer Örnekleri:

```java
// application/usecase/AssignCourierUseCaseImpl.java
@UseCase
public class AssignCourierUseCaseImpl implements AssignCourierUseCase {
    
    private final CourierRepository courierRepository;
    private final OrderRepository orderRepository;
    private final NotificationPort notificationPort;  // Output port
    
    @Override
    public void assignCourierToOrder(AssignCourierRequest request) {
        // 1. Order'ı bul
        Order order = orderRepository.findById(request.getOrderId())
            .orElseThrow(() -> new OrderNotFoundException("Order not found"));
            
        // 2. Uygun kurye bul
        List<Courier> availableCouriers = courierRepository.findAvailableCouriers(
            order.getPickupLocation(), 5.0);  // 5km radius
            
        if (availableCouriers.isEmpty()) {
            throw new NoCourierAvailableException("No courier available");
        }
        
        Courier selectedCourier = availableCouriers.get(0);  // En yakın
        
        // 3. İş kuralı (Domain method çağır)
        selectedCourier.assignToOrder(order);
        order.assignCourier(selectedCourier);
        
        // 4. Kaydet
        courierRepository.save(selectedCourier);
        orderRepository.save(order);
        
        // 5. Bildirim gönder (Output adapter)
        notificationPort.sendCourierAssignmentNotification(selectedCourier, order);
    }
}

// application/dto/AssignCourierRequest.java
public class AssignCourierRequest {
    private Long orderId;
    private Long preferredCourierId;  // Optional
    
    // Constructor, getter/setter
}
```

### 🔌 Infrastructure Layer Örnekleri:

```java
// infrastructure/adapter/input/web/controller/CourierController.java
@RestController
@RequestMapping("/api/couriers")
public class CourierController {
    
    private final AssignCourierUseCase assignCourierUseCase;
    
    @PostMapping("/{courierId}/assign")
    public ResponseEntity<Void> assignToOrder(
            @PathVariable Long courierId,
            @RequestBody AssignCourierWebRequest request) {
        
        // Web DTO → Use Case DTO dönüşüm
        AssignCourierRequest useCaseRequest = new AssignCourierRequest();
        useCaseRequest.setOrderId(request.getOrderId());
        
        assignCourierUseCase.assignCourierToOrder(useCaseRequest);
        
        return ResponseEntity.ok().build();
    }
}

// infrastructure/adapter/output/persistence/repository/JdbcCourierRepository.java
@Repository
public class JdbcCourierRepository implements CourierRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    @Override
    public List<Courier> findAvailableCouriers(Location location, double radiusKm) {
        String sql = """
            SELECT c.* FROM couriers c 
            WHERE c.status = 'ONLINE' 
            AND c.current_latitude IS NOT NULL 
            AND c.current_longitude IS NOT NULL
            AND (6371 * acos(cos(radians(?)) * cos(radians(c.current_latitude)) * 
                cos(radians(c.current_longitude) - radians(?)) + sin(radians(?)) * 
                sin(radians(c.current_latitude)))) < ?
            ORDER BY (6371 * acos(...)) ASC  -- En yakın önce
            """;
            
        return jdbcTemplate.query(sql, courierRowMapper, 
            location.getLatitude(), location.getLongitude(), 
            location.getLatitude(), radiusKm);
    }
    
    private final RowMapper<Courier> courierRowMapper = (rs, rowNum) -> {
        // ResultSet → Domain dönüşüm
        return new Courier(
            rs.getLong("id"),
            rs.getString("name"),
            new Email(rs.getString("email")),
            new Phone(rs.getString("phone")),
            CourierStatus.valueOf(rs.getString("status")),
            new Location(rs.getDouble("current_latitude"), rs.getDouble("current_longitude"))
        );
    };
}

// infrastructure/adapter/output/messaging/kafka/CourierKafkaNotificationAdapter.java
@Component
public class CourierKafkaNotificationAdapter implements NotificationPort {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Override
    public void sendCourierAssignmentNotification(Courier courier, Order order) {
        CourierAssignmentEvent event = new CourierAssignmentEvent();
        event.setCourierId(courier.getId());
        event.setOrderId(order.getId());
        event.setTimestamp(LocalDateTime.now());
        
        kafkaTemplate.send("courier-assignment", event);
    }
}
```

---

## 🎯 Best Practices

### ✅ DO (Yapılacaklar):

1. **Domain katmanında annotation kullanma**
```java
// ✅ DOĞRU
public class Courier {
    private Long id;
    private String name;
}

// ❌ YANLIŞ  
@Entity
@Table(name = "couriers")
public class Courier {
    @Id
    @GeneratedValue
    private Long id;
}
```

2. **Interface'leri domain'de, implementation'ları infrastructure'da tanımla**
```java
// ✅ Domain'de interface
// domain/port/output/CourierRepository.java
public interface CourierRepository {
    Courier save(Courier courier);
}

// ✅ Infrastructure'da implementation
// infrastructure/adapter/output/persistence/JdbcCourierRepository.java
@Repository
public class JdbcCourierRepository implements CourierRepository {
    // Implementation burada
}
```

3. **DTO'ları katmanlar arası dönüşüm için kullan**
```java
// Web Layer DTO
public class CreateCourierWebRequest {
    private String name;
    private String email;
}

// Application Layer DTO  
public class CreateCourierRequest {
    private String name;
    private Email email;  // Value Object
}
```

4. **Mapper'ları dependency injection ile kullan**
```java
@Component
public class CourierMapper {
    public Courier toDomain(CreateCourierRequest request) {
        return new Courier(request.getName(), request.getEmail());
    }
}
```

### ❌ DON'T (Yapılmayacaklar):

1. **Domain'de framework annotation kullanma**
2. **Infrastructure'dan domain'e bağımlılık oluşturma**
3. **Cross-cutting concern'leri domain'de handle etme**
4. **Database entity'lerini domain model olarak kullanma**

---

## 🚚 Kurye Projesi için Özel Açıklamalar

### 📍 **Lokasyon Yönetimi:**
```java
// domain/valueobject/Location.java
public class Location {
    private final double latitude;
    private final double longitude;
    
    public double distanceTo(Location other) {
        // Haversine formula ile mesafe hesapla
    }
    
    public boolean isWithinRadius(Location center, double radiusKm) {
        return this.distanceTo(center) <= radiusKm;
    }
}

// domain/service/CourierAssignmentService.java
public class CourierAssignmentService {
    public Courier findNearestAvailableCourier(Location orderLocation, List<Courier> couriers) {
        return couriers.stream()
            .filter(Courier::isAvailable)
            .min((c1, c2) -> Double.compare(
                c1.getCurrentLocation().distanceTo(orderLocation),
                c2.getCurrentLocation().distanceTo(orderLocation)
            ))
            .orElseThrow(() -> new NoCourierAvailableException());
    }
}
```

### 📦 **Sipariş Durumu Yönetimi:**
```java
// domain/model/Order.java  
public class Order {
    private OrderStatus status;
    private Courier assignedCourier;
    
    public void assignCourier(Courier courier) {
        if (this.status != OrderStatus.PENDING) {
            throw new OrderAlreadyAssignedException();
        }
        this.assignedCourier = courier;
        this.status = OrderStatus.ASSIGNED;
        
        // Domain Event publish et
        DomainEvents.publish(new CourierAssignedEvent(this.id, courier.getId()));
    }
}
```

### 🔔 **Real-time Bildirimler:**
```java
// domain/port/output/NotificationPort.java
public interface NotificationPort {
    void notifyOrderStatusChange(Order order);
    void notifyCourierLocationUpdate(Courier courier);
}

// infrastructure/adapter/output/messaging/WebSocketNotificationAdapter.java
@Component
public class WebSocketNotificationAdapter implements NotificationPort {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    @Override
    public void notifyOrderStatusChange(Order order) {
        OrderStatusUpdate update = new OrderStatusUpdate(order.getId(), order.getStatus());
        messagingTemplate.convertAndSend("/topic/orders/" + order.getId(), update);
    }
}
```

### ⚡ **Performance Optimizations:**
```java
// infrastructure/adapter/output/persistence/CourierCacheRepository.java
@Repository
public class CourierCacheRepository implements CourierRepository {
    
    private final CourierRepository delegate;  // Actual repository
    private final RedisTemplate<String, Courier> redisTemplate;
    
    @Override
    public Optional<Courier> findById(Long id) {
        // Önce cache'den bak
        String cacheKey = "courier:" + id;
        Courier cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return Optional.of(cached);
        }
        
        // Cache'de yoksa database'den çek ve cache'le
        Optional<Courier> result = delegate.findById(id);
        result.ifPresent(courier -> 
            redisTemplate.opsForValue().set(cacheKey, courier, Duration.ofMinutes(10)));
        
        return result;
    }
}
```

---

## 🎓 Sonuç

Hexagonal Architecture, MVC'ye göre daha karmaşık görünebilir ama büyük faydalar sağlar:

### 🏆 **Hexagonal'ın Faydaları:**
- **Testability**: Her katman ayrı test edilebilir
- **Flexibility**: Database/Framework değişikliği kolay  
- **Maintainability**: Temiz, SOLID kod
- **Scalability**: Mikroservis mimarisine geçiş kolay
- **Business Focus**: İş mantığı teknoloji bağımsız

### 🚀 **İlk Adımlar:**
1. **Domain model'leri** oluştur (annotation'sız)
2. **Port interface'leri** tanımla  
3. **Use case'leri** implement et
4. **Adapter'ları** yaz (Controller, Repository)
5. **Mapper'ları** oluştur
6. **Test'leri** yaz

### 📚 **Öğrenme Süreci:**
- İlk başta karmaşık görünebilir
- Zamanla katmanlar arası sorumluluklar netleşir
- Clean Architecture ile birlikte öğrenmek faydalı
- Domain-Driven Design (DDD) bilgisi yardımcı olur

Bu mimari, kurye yönetim sisteminiz için en uygun yapıyı sağlayacak. Zamana yatırım yaptıkça faydalarını göreceksiniz!

---

## 📖 Ek Kaynaklar

- **Hexagonal Architecture**: Alistair Cockburn
- **Clean Architecture**: Robert C. Martin  
- **Domain-Driven Design**: Eric Evans
- **Implementing Domain-Driven Design**: Vaughn Vernon

> 💡 **İpucu**: Bu mimariye geçiş yaparken sabırlı olun. İlk projede mükemmel olmayabilir, ama zamanla çok faydasını göreceksiniz!
