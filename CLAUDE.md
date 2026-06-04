# CLAUDE.md — i-need-courier

## Project Overview

**i-need-courier** (internal artifact ID: `pako`) is a courier management system backend built with Spring Boot 4.0 / Java 21. It connects businesses that need deliveries with couriers who fulfil them. The system handles registration, authentication, shift management, real-time order assignment, and the full delivery lifecycle.

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 (virtual threads enabled) |
| Framework | Spring Boot 4.0.0 |
| Database | PostgreSQL 17 + PostGIS (via Docker, port 5433) |
| Migrations | Flyway (V1–V18, classpath:db/migration) |
| Cache / Sessions | Redis 8 (port 6380) |
| Messaging | Apache Kafka 4 (KRaft mode, ports 9092/9094) |
| Real-time | WebSocket / STOMP |
| Auth | JWT (jjwt 0.13, stateless, 24h expiry) |
| API Docs | SpringDoc OpenAPI / Swagger UI (`/swagger-ui.html`) |
| Build | Maven Wrapper (`./mvnw`) |
| Containerisation | Docker Compose (`compose.yaml`) |

## Running the Project

```bash
# Start infrastructure (DB + Redis + Kafka)
docker compose up -d postgres redis kafka

# Run the app (port 8081)
./mvnw spring-boot:run

# Or run everything in Docker
docker compose up
```

Kafka UI is available at `http://localhost:8082`.

## Running Tests

```bash
# Unit tests only (Surefire, *Test.java)
./mvnw test

# Integration tests only (Failsafe, *IT.java)
./mvnw verify -P integration

# All tests + coverage report (JaCoCo → target/site/jacoco)
./mvnw verify
```

Integration tests use Testcontainers — Docker must be running.

## Package Structure

```
com.api.pako
├── business/          # Business-facing order management (separate sub-package)
│   ├── controller/
│   ├── dto/
│   └── service/
├── config/            # SecurityConfig, WebSocketConfig, ApiResponseAdvice
├── controller/        # CourierAssignmentController, CourierOrderController,
│                      # RegistrationController, UnifiedAuthController
├── courier/           # Courier-specific sub-package (CourierShiftController)
├── dto/               # Shared DTOs (ApiResponse, login/registration requests)
├── exception/         # Custom exceptions + GlobalExceptionHandler
├── model/             # JPA entities (Business, Courier, Order, OrderAssignment,
│                      # OnDutyCourier, Shift, ShiftTemplate)
│   └── enums/         # OrderStatus, AssignmentStatus, AssignmentType, ...
├── repository/        # Spring Data JPA repositories
├── security/          # JWT filter, entry points, token provider
└── service/           # Domain services (OrderAssignmentService, OnDutyService,
                       # ShiftService, UnifiedAuthService, WebSocketNotificationService, ...)
```

## Key Domain Concepts

- **Business** — a company that creates delivery orders.
- **Courier** — a driver who picks up and delivers orders.
- **Order** — a delivery request with pickup/delivery addresses, status lifecycle: `PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED` (or `CANCELLED` / `RETURNED`).
- **OrderAssignment** — tracks which courier was offered an order. Status: `PENDING → ACCEPTED / REJECTED / TIMEOUT`.
- **OnDutyCourier** — couriers currently on shift; maintained as a FIFO queue for assignment.
- **Shift / ShiftTemplate** — shift reservation and check-in/check-out system.

## Order Assignment Flow

1. Business creates an order → `BusinessOrderService`.
2. `OrderAssignmentService.assignToNextAvailableCourier()` picks the first courier in the FIFO queue.
3. Assignment is created (`PENDING`); courier notified via WebSocket.
4. Courier has `order.assignment.timeout.minutes` (default: 4 min) to accept or reject.
5. On accept → order becomes `ASSIGNED`; courier moves to end of queue.
6. On reject / timeout → order is reassigned (`AssignmentType.REASSIGNMENT`) to the next courier.
7. Scheduled job (`@Scheduled(fixedDelay=30000)`) auto-expires timed-out pending assignments.

## Architecture Conventions (enforced by `ConventionsTest` via ArchUnit)

- **No `@Autowired` field injection** — constructor injection only.
- **No static `Logger` fields** — use `@Slf4j` (Lombok).
- **All public controller methods must return `ApiResponse<T>`** — enforced by ArchUnit.

## Testing Conventions

- Unit test files: `*Test.java` (Surefire).
- Integration test files: `*IT.java` (Failsafe + Testcontainers).
- The instance of the class under test is **always named `underTest`** (not `service`, not `sut`).
- No `@Autowired` in test classes either — use constructor or `@InjectMocks`.
- Verify mock arguments with Mockito `assertArg`, not `ArgumentCaptor` (reserve captors for when the value is needed after verification).

## Code Style

- **Always use `var` for local variables** (Java local-variable type inference) in both production and test code.
- Lombok is used heavily (`@Slf4j`, `@RequiredArgsConstructor`, `@Getter`, `@Setter`, `@Builder`, etc.).
- Entities use `@EqualsAndHashCode(onlyExplicitlyIncluded = true)` and `@ToString.Exclude` on lazy relations to prevent N+1 and infinite recursion.
- Database enums are stored as `VARCHAR` (not native PG enum types) after migrations V10–V13.
- All timestamps use `OffsetDateTime` with `ZoneOffset.UTC` in service layer; entities use `LocalDateTime`.

## Git / Commit Conventions

- **Never add `Co-Authored-By` trailers** (or any AI attribution) to commit messages or PR bodies.

## Notes for Future Sessions

- Some comments inside service/controller methods are currently in Turkish. These will be translated to English incrementally across future sessions — do **not** batch-translate them all at once.
- The `compose.yaml` uses port `5433` (PostgreSQL) and `6380` (Redis) to avoid conflicts with local installations.
- The JWT secret in `application.properties` is a placeholder — must be replaced with a proper secret before any production use.
