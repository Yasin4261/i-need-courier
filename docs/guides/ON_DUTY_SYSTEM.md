# On-Duty Couriers System

## Overview

The `on_duty_couriers` table provides a fast, dedicated view of currently active (on-duty) couriers in the system. This design follows **SOLID principles** and **Clean Layered Architecture**.

## Architecture

### Why Separate Table?

1. **Single Responsibility Principle (SRP)**: Dedicated table for active courier state
2. **Performance**: Fast queries without joins for order assignment
3. **Scalability**: Can be cached/indexed independently
4. **Audit Trail**: Clear source tracking (app, backfill, etc.)

### Clean Layered Design

```
┌─────────────────────────────────────────┐
│ Controller (CourierShiftController)     │  ← REST endpoints
├─────────────────────────────────────────┤
│ Service (ShiftService + OnDutyService)  │  ← Business logic
├─────────────────────────────────────────┤
│ Repository (OnDutyCourierRepository)    │  ← Data access
├─────────────────────────────────────────┤
│ Model (OnDutyCourier entity)            │  ← Domain model
├─────────────────────────────────────────┤
│ Database (on_duty_couriers table)       │  ← Persistence
└─────────────────────────────────────────┘
```

## Database Schema

### Table: `on_duty_couriers`

```sql
CREATE TABLE on_duty_couriers (
  id BIGSERIAL PRIMARY KEY,
  courier_id BIGINT NOT NULL UNIQUE,
  shift_id BIGINT,
  on_duty_since TIMESTAMPTZ NOT NULL,
  source VARCHAR(32) NOT NULL DEFAULT 'app',
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  
  CONSTRAINT fk_on_duty_courier FOREIGN KEY (courier_id) 
    REFERENCES couriers(id) ON DELETE CASCADE,
  CONSTRAINT fk_on_duty_shift FOREIGN KEY (shift_id) 
    REFERENCES shifts(shift_id) ON DELETE SET NULL
);

CREATE INDEX idx_on_duty_on_duty_since ON on_duty_couriers (on_duty_since);
CREATE INDEX idx_on_duty_shift_id ON on_duty_couriers (shift_id);
```

### Columns

| Column | Type | Description |
|--------|------|-------------|
| `id` | BIGSERIAL | Primary key |
| `courier_id` | BIGINT | FK to couriers.id (UNIQUE) |
| `shift_id` | BIGINT | FK to shifts.shift_id (nullable) |
| `on_duty_since` | TIMESTAMPTZ | When courier went on-duty (FIFO key) |
| `source` | VARCHAR(32) | 'app', 'backfill', 'reconciliation' |
| `created_at` | TIMESTAMPTZ | Record creation time |
| `updated_at` | TIMESTAMPTZ | Last update time |

## Code Components

### 1. Entity (`OnDutyCourier.java`)

```java
@Entity
@Table(name = "on_duty_couriers")
public class OnDutyCourier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "courier_id", nullable = false)
    private Long courierId;
    
    @Column(name = "shift_id")
    private Long shiftId;
    
    @Column(name = "on_duty_since", nullable = false)
    private OffsetDateTime onDutySince;
    
    @Column(name = "source", nullable = false)
    private String source = "app";
    
    // ... getters/setters
}
```

### 2. Repository (`OnDutyCourierRepository.java`)

```java
@Repository
public interface OnDutyCourierRepository extends JpaRepository<OnDutyCourier, Long> {
    Optional<OnDutyCourier> findByCourierId(Long courierId);
    List<OnDutyCourier> findAllByOrderByOnDutySinceAsc();
    void deleteByCourierId(Long courierId);
}
```

### 3. Service (`OnDutyService.java`)

**Responsibilities** (SRP):
- Manage on-duty courier lifecycle
- Provide FIFO-ordered active courier list
- Encapsulate on-duty business rules

```java
@Service
public class OnDutyService {
    private final OnDutyCourierRepository repository;
    
    // Get all active couriers ordered by on_duty_since (FIFO)
    public List<OnDutyCourier> getAllActiveOrdered();
    
    // Check if courier is on-duty
    public boolean isCourierOnDuty(Long courierId);
    
    // Add or update on-duty record
    @Transactional
    public OnDutyCourier upsertOnDuty(Long courierId, Long shiftId);
    
    // Remove on-duty record
    @Transactional
    public void removeOnDuty(Long courierId);
}
```

### 4. Integration in `ShiftService`

```java
@Service
public class ShiftService {
    private final OnDutyService onDutyService;
    
    @Transactional
    public ShiftDTO checkIn(Long courierId, Long shiftId, CheckInRequest request) {
        // 1. Update shift status
        shift.setStatus(CHECKED_IN);
        shiftRepository.save(shift);
        
        // 2. Update on_duty_couriers (via OnDutyService - SRP)
        onDutyService.upsertOnDuty(courierId, shiftId);
        
        // 3. Update courier status
        courier.setStatus(ONLINE);
        courierRepository.save(courier);
        
        return dto;
    }
    
    @Transactional
    public ShiftDTO checkOut(Long courierId, Long shiftId, CheckOutRequest request) {
        // 1. Update shift status
        shift.setStatus(CHECKED_OUT);
        shiftRepository.save(shift);
        
        // 2. Remove from on_duty_couriers
        onDutyService.removeOnDuty(courierId);
        
        // 3. Update courier status
        courier.setStatus(OFFLINE);
        courierRepository.save(courier);
        
        return dto;
    }
}
```

## SOLID Principles Applied

### 1. Single Responsibility Principle (SRP)
- ✅ `OnDutyService`: Only manages on-duty state
- ✅ `ShiftService`: Manages shift lifecycle
- ✅ `OnDutyCourierRepository`: Only data access for on-duty records

### 2. Open/Closed Principle (OCP)
- ✅ Easy to extend with new on-duty sources (Redis, events)
- ✅ Repository methods can be added without changing existing code

### 3. Liskov Substitution Principle (LSP)
- ✅ Repository extends JpaRepository correctly
- ✅ Service interfaces can be mocked for testing

### 4. Interface Segregation Principle (ISP)
- ✅ Repository has only needed methods
- ✅ Service has focused public API

### 5. Dependency Inversion Principle (DIP)
- ✅ `ShiftService` depends on `OnDutyService` abstraction
- ✅ Services depend on repository interfaces, not implementations

## Usage Examples

### API: Check-in to Shift

```bash
# 1. Register courier
curl -X POST http://localhost:8081/api/v1/auth/register/courier \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ali Kurye",
    "email": "ali@test.com",
    "phone": "+905551112233",
    "password": "secret123"
  }'

# 2. Login
TOKEN=$(curl -s -X POST http://localhost:8081/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ali@test.com","password":"secret123"}' \
  | jq -r '.data.token')

# 3. Reserve shift
SHIFT=$(curl -s -X POST http://localhost:8081/api/v1/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"templateId":1,"shiftDate":"2025-12-03"}' \
  | jq -r '.data.shiftId')

# 4. Check-in
curl -X POST "http://localhost:8081/api/v1/courier/shifts/$SHIFT/check-in" \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{}'
```

### Database: Query Active Couriers (FIFO)

```sql
-- Get all active couriers ordered by on_duty_since
SELECT 
    od.courier_id, 
    c.name, 
    c.email,
    od.on_duty_since,
    od.shift_id,
    EXTRACT(EPOCH FROM (now() - od.on_duty_since))/3600 AS hours_on_duty
FROM on_duty_couriers od
JOIN couriers c ON c.id = od.courier_id
ORDER BY od.on_duty_since ASC;

-- Get next courier for assignment (earliest on-duty)
SELECT courier_id, on_duty_since
FROM on_duty_couriers
ORDER BY on_duty_since ASC
LIMIT 1;

-- Check if specific courier is on-duty
SELECT EXISTS(
    SELECT 1 FROM on_duty_couriers WHERE courier_id = 123
);
```

## Maintenance

### Backfill Existing Data

```bash
# Run backfill script
./scripts/backfill-on-duty-couriers.sh

# Or manually via psql
docker exec -i courier-postgres psql -U courier_user -d courier_db <<'SQL'
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source)
SELECT c.id, s.shift_id, 
       COALESCE(c.on_duty_since, s.check_in_time, now()) AT TIME ZONE 'UTC',
       'backfill'
FROM couriers c
LEFT JOIN shifts s ON s.courier_id = c.id AND s.status = 'CHECKED_IN'
WHERE c.on_duty_since IS NOT NULL OR s.status = 'CHECKED_IN'
ON CONFLICT (courier_id) DO UPDATE
  SET on_duty_since = EXCLUDED.on_duty_since,
      shift_id = EXCLUDED.shift_id;
SQL
```

### Reconciliation (Scheduled Job)

```java
@Scheduled(fixedRate = 60000) // Every minute
public void reconcileOnDuty() {
    // 1. Find couriers with on_duty_since but not in on_duty_couriers
    // 2. Find records in on_duty_couriers but courier.on_duty_since is null
    // 3. Fix inconsistencies
}
```

## Testing

### Unit Test Example

```java
@Test
void testCheckInAddsToOnDuty() {
    // Given
    Long courierId = 1L;
    Long shiftId = 42L;
    
    // When
    shiftService.checkIn(courierId, shiftId, new CheckInRequest());
    
    // Then
    assertTrue(onDutyService.isCourierOnDuty(courierId));
}

@Test
void testCheckOutRemovesFromOnDuty() {
    // Given
    Long courierId = 1L;
    Long shiftId = 42L;
    shiftService.checkIn(courierId, shiftId, new CheckInRequest());
    
    // When
    shiftService.checkOut(courierId, shiftId, new CheckOutRequest());
    
    // Then
    assertFalse(onDutyService.isCourierOnDuty(courierId));
}
```

## Migration

### V14__Create_on_duty_couriers_table.sql

Already applied in `src/main/resources/db/migration/`.

To apply migration:

```bash
# Via Maven
./mvnw flyway:migrate -Dflyway.url=jdbc:postgresql://localhost:5433/courier_db \
  -Dflyway.user=courier_user -Dflyway.password=courier_password

# Or via Docker (application startup)
docker compose up -d
```

## Performance Considerations

1. **Index on `on_duty_since`**: Fast FIFO queries
2. **Unique constraint on `courier_id`**: Prevents duplicates
3. **Cascade delete**: Auto-cleanup when courier deleted
4. **Optional Redis cache**: Can add write-through cache layer

## Future Enhancements

1. **Redis Integration**: Cache active couriers for ultra-fast reads
2. **Event Sourcing**: Publish events on check-in/check-out
3. **Analytics**: Track on-duty duration, patterns
4. **Auto Reconciliation**: Scheduled job to fix inconsistencies

## References

- Migration: `src/main/resources/db/migration/V14__Create_on_duty_couriers_table.sql`
- Entity: `src/main/java/com/api/demo/model/OnDutyCourier.java`
- Repository: `src/main/java/com/api/demo/repository/OnDutyCourierRepository.java`
- Service: `src/main/java/com/api/demo/service/OnDutyService.java`
- Integration: `src/main/java/com/api/demo/service/ShiftService.java`
- Backfill: `scripts/backfill-on-duty-couriers.sh`

