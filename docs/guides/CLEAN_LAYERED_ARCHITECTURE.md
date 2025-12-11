# Clean Layered Architecture Guide

## 📚 Overview

This project uses **Clean Layered Architecture** - a simplified, practical approach that's easier to understand and maintain than Hexagonal Architecture.

## 🏗️ Architecture Layers

```
┌─────────────────────────────────────┐
│     Presentation Layer              │  ← Controllers (REST API)
│     (controller/)                   │
├─────────────────────────────────────┤
│     Application Layer               │  ← Services (Business Logic)
│     (service/)                      │
├─────────────────────────────────────┤
│     Persistence Layer               │  ← Repositories (Data Access)
│     (repository/)                   │
├─────────────────────────────────────┤
│     Domain Layer                    │  ← Entities & Models
│     (model/)                        │
└─────────────────────────────────────┘

Supporting Components:
├── dto/         → Data Transfer Objects
├── exception/   → Custom Exceptions
├── config/      → Configuration
└── security/    → Security Components
```

## 📂 Package Structure

```
src/main/java/com/api/pako/
├── controller/          # Presentation Layer
│   └── CourierAuthController.java
├── service/            # Application Layer
│   └── CourierAuthService.java
├── repository/         # Persistence Layer
│   └── CourierRepository.java
├── model/             # Domain Layer
│   └── Courier.java
├── dto/               # Data Transfer Objects
│   ├── CourierRegistrationRequest.java
│   ├── CourierRegistrationResponse.java
│   ├── CourierLoginRequest.java
│   ├── CourierLoginResponse.java
│   └── ApiResponse.java
├── exception/         # Custom Exceptions
│   ├── CourierAlreadyExistsException.java
│   ├── InvalidCredentialsException.java
│   └── GlobalExceptionHandler.java
├── config/           # Configuration
│   └── SecurityConfig.java
└── security/         # Security Components
    └── JwtTokenProvider.java
```

## 🔄 Layer Responsibilities

### 1. Presentation Layer (Controller)

**Purpose**: Handle HTTP requests/responses and API endpoints

**Responsibilities**:
- Receive HTTP requests
- Validate input (using @Valid)
- Call service methods
- Return HTTP responses with proper status codes
- Handle REST API concerns

**Example**:
```java
@RestController
@RequestMapping("/api/v1/auth")
public class CourierAuthController {
    private final CourierAuthService courierAuthService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CourierRegistrationResponse>> register(
            @Valid @RequestBody CourierRegistrationRequest request) {
        CourierRegistrationResponse response = courierAuthService.register(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Success"));
    }
}
```

**Rules**:
- ✅ Only handle HTTP concerns
- ✅ Use DTOs for request/response
- ✅ Call service layer methods
- ❌ No business logic
- ❌ No direct database access
- ❌ No domain entities in responses

### 2. Application Layer (Service)

**Purpose**: Implement business logic and orchestrate operations

**Responsibilities**:
- Business logic implementation
- Transaction management (@Transactional)
- Coordinate between repositories
- Apply business rules
- Exception handling

**Example**:
```java
@Service
@Transactional
public class CourierAuthService {
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    
    public CourierRegistrationResponse register(CourierRegistrationRequest request) {
        // Business logic here
        if (courierRepository.existsByEmail(request.getEmail())) {
            throw new CourierAlreadyExistsException("Email exists");
        }
        
        Courier courier = new Courier();
        courier.setEmail(request.getEmail());
        courier.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        
        Courier saved = courierRepository.save(courier);
        return new CourierRegistrationResponse(saved.getId(), saved.getEmail());
    }
}
```

**Rules**:
- ✅ Contains business logic
- ✅ Manages transactions
- ✅ Throws custom exceptions
- ✅ Converts between entities and DTOs
- ❌ No HTTP concerns
- ❌ No SQL queries

### 3. Persistence Layer (Repository)

**Purpose**: Data access and database operations

**Responsibilities**:
- Database queries
- CRUD operations
- Custom query methods
- Data persistence

**Example**:
```java
@Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByPhone(String phone);
}
```

**Rules**:
- ✅ Only database operations
- ✅ Return domain entities
- ✅ Use Spring Data JPA
- ❌ No business logic
- ❌ No DTOs

### 4. Domain Layer (Model)

**Purpose**: Core business entities and domain objects

**Responsibilities**:
- Represent business entities
- Define entity relationships
- Entity-level validation
- Database mappings

**Example**:
```java
@Entity
@Table(name = "couriers")
public class Courier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String email;
    
    @Column(name = "password_hash")
    private String passwordHash;
    
    @Enumerated(EnumType.STRING)
    private CourierStatus status;
    
    public enum CourierStatus {
        INACTIVE, ONLINE, OFFLINE, BUSY, SUSPENDED
    }
    
    // Getters, setters, etc.
}
```

**Rules**:
- ✅ Pure domain entities
- ✅ JPA annotations
- ✅ Enums and value objects
- ❌ No business logic
- ❌ No dependencies on other layers

## 🎯 Data Flow

### Registration Flow
```
1. Client → POST /api/v1/auth/register
           ↓
2. CourierAuthController.register()
   - Validates input
   - Calls service
           ↓
3. CourierAuthService.register()
   - Checks if email exists (via repository)
   - Creates Courier entity
   - Encodes password
   - Saves to database (via repository)
   - Converts to DTO
           ↓
4. CourierRepository.save()
   - Saves to database
   - Returns saved entity
           ↓
5. Response ← ApiResponse<CourierRegistrationResponse>
```

### Login Flow
```
1. Client → POST /api/v1/auth/login
           ↓
2. CourierAuthController.login()
           ↓
3. CourierAuthService.login()
   - Finds user by email (via repository)
   - Verifies password
   - Generates JWT token
   - Returns response DTO
           ↓
4. Response ← ApiResponse<CourierLoginResponse> with JWT
```

## 📦 Supporting Components

### DTOs (Data Transfer Objects)

**Purpose**: Transfer data between layers

```java
public class CourierRegistrationRequest {
    @NotBlank
    @Email
    private String email;
    
    @NotBlank
    @Size(min = 6, max = 20)
    private String password;
    
    // Getters, setters
}
```

**Why use DTOs?**
- Decouple API from domain entities
- Control what data is exposed
- Add validation annotations
- Version API independently

### Exceptions

**Purpose**: Handle errors consistently

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(CourierAlreadyExistsException.class)
    public ResponseEntity<ApiResponse<Void>> handleCourierExists(Exception ex) {
        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(ApiResponse.error(409, ex.getMessage()));
    }
}
```

### Configuration

**Purpose**: Application configuration

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) {
        // Security configuration
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

## ✨ Benefits of Clean Layered Architecture

### ✅ Easy to Understand
- Clear separation of concerns
- Straightforward layer boundaries
- Familiar to most developers

### ✅ Easy to Maintain
- Each layer has single responsibility
- Easy to locate code
- Simple to make changes

### ✅ Easy to Test
- Mock dependencies easily
- Test layers independently
- Clear test boundaries

### ✅ Scalable
- Add new features easily
- Extend without breaking existing code
- Parallel development possible

## 🆚 Comparison: Clean Layered vs Hexagonal

| Aspect | Clean Layered | Hexagonal |
|--------|--------------|-----------|
| Complexity | ⭐ Simple | ⭐⭐⭐ Complex |
| Learning Curve | ⭐ Easy | ⭐⭐⭐ Steep |
| Folder Structure | 3-4 levels | 5-6 levels |
| Abstractions | Minimal | Heavy (ports/adapters) |
| Best For | Small-Medium projects | Large enterprise projects |

## 🛠️ Adding New Features

### Example: Adding Order Management

#### 1. Create Entity (Domain Layer)
```java
@Entity
public class Order {
    @Id
    private Long id;
    private Long courierId;
    private String deliveryAddress;
    // ...
}
```

#### 2. Create Repository (Persistence Layer)
```java
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByCourierId(Long courierId);
}
```

#### 3. Create Service (Application Layer)
```java
@Service
public class OrderService {
    private final OrderRepository orderRepository;
    
    public OrderResponse createOrder(OrderRequest request) {
        // Business logic
    }
}
```

#### 4. Create Controller (Presentation Layer)
```java
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final OrderService orderService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderRequest request) {
        // Handle request
    }
}
```

#### 5. Create DTOs
```java
public class OrderRequest { /* ... */ }
public class OrderResponse { /* ... */ }
```

## 📝 Best Practices

### DO ✅
- Keep layers independent
- Use dependency injection
- Write tests for each layer
- Use DTOs for API boundaries
- Handle exceptions properly
- Add logging
- Document complex logic

### DON'T ❌
- Skip layers (controller → repository)
- Put business logic in controllers
- Expose entities in API responses
- Catch exceptions without handling
- Hard-code configuration values
- Ignore validation

## 🧪 Testing Strategy

### Unit Tests
```java
@ExtendWith(MockitoExtension.class)
class CourierAuthServiceTest {
    @Mock
    private CourierRepository courierRepository;
    
    @InjectMocks
    private CourierAuthService courierAuthService;
    
    @Test
    void register_withValidData_shouldCreateCourier() {
        // Test business logic
    }
}
```

### Integration Tests
```java
@SpringBootTest
@AutoConfigureMockMvc
class CourierAuthControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void register_shouldReturn200() throws Exception {
        // Test full flow
    }
}
```

## 🎓 Learning Path

1. **Start Here**: Understand the 4 main layers
2. **Read Code**: Study existing features (CourierAuth)
3. **Add Feature**: Create a simple CRUD feature
4. **Write Tests**: Add unit and integration tests
5. **Refactor**: Improve code based on feedback

## 📚 Additional Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Spring Data JPA](https://spring.io/projects/spring-data-jpa)
- [Clean Architecture by Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)

## 💡 Need Help?

- Check existing code examples in the project
- Read this guide thoroughly
- Ask questions in code reviews
- Consult with team members
  package com.api.pako.service;

import com.api.pako.dto.CourierLoginRequest;
import com.api.pako.dto.CourierLoginResponse;
import com.api.pako.dto.CourierRegistrationRequest;
import com.api.pako.dto.CourierRegistrationResponse;
import com.api.pako.exception.CourierAlreadyExistsException;
import com.api.pako.exception.InvalidCredentialsException;
import com.api.pako.model.Courier;
import com.api.pako.repository.CourierRepository;
import com.api.pako.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for CourierAuthService.
 */
@ExtendWith(MockitoExtension.class)
class CourierAuthServiceTest {

    @Mock
    private CourierRepository courierRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private CourierAuthService courierAuthService;

    private CourierRegistrationRequest registrationRequest;
    private CourierLoginRequest loginRequest;
    private Courier courier;

    @BeforeEach
    void setUp() {
        registrationRequest = new CourierRegistrationRequest(
            "John Doe",
            "john@example.com",
            "+905551234567",
            "password123"
        );

        loginRequest = new CourierLoginRequest(
            "john@example.com",
            "password123"
        );

        courier = new Courier();
        courier.setId(1L);
        courier.setName("John Doe");
        courier.setEmail("john@example.com");
        courier.setPhone("+905551234567");
        courier.setPasswordHash("hashedPassword");
        courier.setStatus(Courier.CourierStatus.INACTIVE);
    }

    @Test
    void register_withValidData_shouldCreateCourier() {
        // Arrange
        when(courierRepository.existsByEmail(anyString())).thenReturn(false);
        when(courierRepository.existsByPhone(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPassword");
        when(courierRepository.save(any(Courier.class))).thenReturn(courier);

        // Act
        CourierRegistrationResponse response = courierAuthService.register(registrationRequest);

        // Assert
        assertNotNull(response);
        assertEquals(1L, response.getCourierId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("Registration successful", response.getMessage());

        verify(courierRepository).existsByEmail("john@example.com");
        verify(courierRepository).existsByPhone("+905551234567");
        verify(passwordEncoder).encode("password123");
        verify(courierRepository).save(any(Courier.class));
    }

    @Test
    void register_withExistingEmail_shouldThrowException() {
        // Arrange
        when(courierRepository.existsByEmail(anyString())).thenReturn(true);

        // Act & Assert
        CourierAlreadyExistsException exception = assertThrows(
            CourierAlreadyExistsException.class,
            () -> courierAuthService.register(registrationRequest)
        );

        assertTrue(exception.getMessage().contains("email"));
        verify(courierRepository).existsByEmail("john@example.com");
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void register_withExistingPhone_shouldThrowException() {
        // Arrange
        when(courierRepository.existsByEmail(anyString())).thenReturn(false);
        when(courierRepository.existsByPhone(anyString())).thenReturn(true);

        // Act & Assert
        CourierAlreadyExistsException exception = assertThrows(
            CourierAlreadyExistsException.class,
            () -> courierAuthService.register(registrationRequest)
        );

        assertTrue(exception.getMessage().contains("Phone"));
        verify(courierRepository).existsByPhone("+905551234567");
        verify(courierRepository, never()).save(any(Courier.class));
    }

    @Test
    void login_withValidCredentials_shouldReturnToken() {
        // Arrange
        when(courierRepository.findByEmail(anyString())).thenReturn(Optional.of(courier));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtTokenProvider.generateToken(anyLong(), anyString())).thenReturn("jwt-token");

        // Act
        CourierLoginResponse response = courierAuthService.login(loginRequest);

        // Assert
        assertNotNull(response);
        assertEquals("jwt-token", response.getToken());
        assertEquals(1L, response.getCourierId());
        assertEquals("John Doe", response.getName());
        assertEquals("john@example.com", response.getEmail());
        assertEquals("Login successful", response.getStatus());

        verify(courierRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtTokenProvider).generateToken(1L, "john@example.com");
    }

    @Test
    void login_withInvalidEmail_shouldThrowException() {
        // Arrange
        when(courierRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> courierAuthService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid"));
        verify(courierRepository).findByEmail("john@example.com");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }

    @Test
    void login_withInvalidPassword_shouldThrowException() {
        // Arrange
        when(courierRepository.findByEmail(anyString())).thenReturn(Optional.of(courier));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        // Act & Assert
        InvalidCredentialsException exception = assertThrows(
            InvalidCredentialsException.class,
            () -> courierAuthService.login(loginRequest)
        );

        assertTrue(exception.getMessage().contains("Invalid"));
        verify(courierRepository).findByEmail("john@example.com");
        verify(passwordEncoder).matches("password123", "hashedPassword");
        verify(jwtTokenProvider, never()).generateToken(anyLong(), anyString());
    }
}

