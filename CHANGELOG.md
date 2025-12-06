# Changelog

All notable changes to the I Need Courier project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.9.0] - 2025-12-06

### Added
- **Project Organization & Documentation**
  - `PROJECT_STRUCTURE.md` - Complete project organization guide
  - `COURIER_ORDER_FLOW.md` - Detailed delivery flow documentation
  - `WHY_START_DELIVERY_EXISTS.md` - Design decision explanations
  - `DEBUG_PICKUP_ISSUE.md` - Comprehensive troubleshooting guide
  - Organized documentation into `docs/reports/`, `docs/fixes/`, `docs/flows/`
  - Test scripts moved to `scripts/tests/`
  - 15+ documentation files created/reorganized

### Changed
- Reorganized 26 files from root directory into proper subdirectories
- Updated all documentation with current system state
- Enhanced README with clear project structure

### Removed
- 8 obsolete files (fix-flyway.sh, flyway-repair-guide.sh, migration.sh, github-push.sh, etc.)
- Old/unused test scripts from root directory
- Redundant documentation files

---

## [1.8.0] - 2025-12-06

### Added
- **WebSocket & Real-time Notifications**
  - WebSocket configuration for real-time communication
  - `WebSocketNotificationService` for assignment notifications
  - Real-time order assignment notifications to couriers
  - STOMP messaging protocol support
  - `/topic/assignments/{courierId}` subscription endpoint

### Changed
- Enhanced assignment flow with instant courier notifications
- Improved user experience with real-time updates

---

## [1.7.0] - 2025-12-06

### Added
- **Custom Exception System**
  - `NoCourierAvailableException` (503) - No courier available for assignment
  - `AssignmentNotFoundException` (404) - Assignment not found
  - `AssignmentNotOwnedException` (403) - Unauthorized assignment access
  - `AssignmentExpiredException` (410) - Assignment timeout
  - `InvalidAssignmentStatusException` (409) - Invalid status transition
  
- **Enhanced Logging System**
  - DEBUG level logs for development
  - INFO level logs for operations
  - ERROR level logs with detailed context
  - Request/Response logging for all endpoints
  - Assignment flow tracking

### Changed
- Replaced generic `RuntimeException` with specific custom exceptions
- Improved HTTP status code accuracy (403, 404, 410, 503 instead of 500)
- Enhanced error messages with detailed context
- Better exception handling in GlobalExceptionHandler

### Fixed
- Proper HTTP status codes for different error scenarios
- Improved debugging with detailed error context

---

## [1.6.0] - 2025-12-06

### Added
- **Delivery Flow System**
  - POST `/api/v1/courier/orders/{id}/pickup` – Mark order as picked up
  - POST `/api/v1/courier/orders/{id}/start-delivery` – Start delivery (in transit)
  - POST `/api/v1/courier/orders/{id}/complete` – Complete delivery with notes and collection amount
  - Complete order status workflow (ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED)
  - Optional notes and collection amount tracking
  - Flexible parameter handling (query params, form data, JSON)

### Changed
- Added `consumes = {"*/*"}` for better content-type support
- Enhanced CourierOrderController with detailed logging
- Improved parameter flexibility (@RequestParam instead of @RequestBody)

### Fixed
- "Bu sipariş size atanmamış" error in pickup endpoint
- Order.courier NULL issue in acceptAssignment
- 415 Unsupported Media Type error
- Proper courier ownership verification

---

## [1.5.0] - 2025-12-06

### Added
- **Order Assignment System**
  - FIFO queue-based automatic assignment algorithm
  - GET `/api/v1/courier/assignments/pending` – Get pending assignments
  - POST `/api/v1/courier/assignments/{id}/accept` – Accept assignment
  - POST `/api/v1/courier/assignments/{id}/reject` – Reject assignment with reason
  - 4-minute timeout mechanism with automatic reassignment
  - Assignment history tracking
  - `CourierAssignmentController` with 3 endpoints
  
- **Database Schema**
  - `order_assignments` table for assignment tracking
  - Assignment status enum (PENDING, ACCEPTED, REJECTED, TIMEOUT)
  - Assignment type enum (AUTO, REASSIGNMENT, MANUAL)
  - Timestamp tracking (assigned_at, response_at, timeout_at)
  
- **Assignment Service**
  - `OrderAssignmentService` with auto-assignment logic
  - Timeout checking scheduled task
  - Automatic reassignment on timeout/reject
  - Queue management integration

### Changed
- Business order creation now triggers automatic assignment
- Enhanced order lifecycle with assignment tracking

### Fixed
- Duplicate assignment creation prevention
- Timeout filter in pending assignments query
- Assignment status transition validation

---

## [1.4.0] - 2025-12-06

### Added
- **On-Duty Courier Management System**
  - POST `/api/v1/courier/shifts/{id}/check-in` – Check-in to shift (become on-duty)
  - POST `/api/v1/courier/shifts/check-out` – Check-out from shift (go off-duty)
  - FIFO queue management for on-duty couriers
  - Real-time on-duty status tracking
  - `OnDutyService` with queue operations
  
- **Database Schema**
  - `on_duty_couriers` table with FIFO queue support
  - Created timestamp and on_duty_since tracking
  - Source tracking (shift vs manual)
  - Automatic cleanup on check-out

### Changed
- Enhanced shift system with on-duty integration
- ShiftService updated with check-in/check-out logic

### Security
- Enhanced authorization checks in all endpoints
- Proper ownership verification
- Status-based access control

---

## [1.3.0] - 2025-11-14

### Added
- Courier Shift Management system
  - GET `/api/v1/courier/shifts/templates` – List shift templates
  - POST `/api/v1/courier/shifts/reserve` – Reserve a shift
  - GET `/api/v1/courier/shifts/upcoming` – Upcoming shifts
  - GET `/api/v1/courier/shifts/my-shifts` – List my shifts (with status filter)
  - GET `/api/v1/courier/shifts/active` – Active shift
  - POST `/api/v1/courier/shifts/{id}/check-in` – Check-in
  - POST `/api/v1/courier/shifts/{id}/check-out` – Check-out
  - DELETE `/api/v1/courier/shifts/{id}/cancel` – Cancel reservation
- Bash test script `test-shift-yasin.sh` with auto-cancel and parameters (SHIFT_DATE, TEMPLATE_INDEX)
- Postman collections and shift testing guide under docs/guides

### Changed
- Global exception handler improvements for method/media type errors
- Security enhancements: added JwtAuthenticationEntryPoint and JwtAccessDeniedHandler

### Fixed
- PostgreSQL enum mismatch on `shifts.shift_role` and `shifts.status` by converting to VARCHAR (manual SQL + V13 migration file)

---

## [1.2.0] - 2025-11-07

### Added
- **Business Order Management System**: Complete CRUD operations for orders
- **8 New API Endpoints**: Full order management functionality
  - POST `/api/v1/business/orders` - Create order
  - GET `/api/v1/business/orders` - List all orders
  - GET `/api/v1/business/orders?status=X` - Filter by status
  - GET `/api/v1/business/orders/{id}` - Get order details
  - PUT `/api/v1/business/orders/{id}` - Update order
  - DELETE `/api/v1/business/orders/{id}` - Delete order
  - POST `/api/v1/business/orders/{id}/cancel` - Cancel order
  - GET `/api/v1/business/orders/statistics` - Get statistics
- **Order Entity**: New Order model with full order lifecycle support
- **Order Enums**: OrderStatus, OrderPriority, PaymentType
- **BusinessOrderService**: Service layer for order operations
- **OrderRepository**: Custom queries for order management
- **Order DTOs**: OrderCreateRequest, OrderUpdateRequest, OrderResponse
- **Auto-generated Order Numbers**: Format ORD-YYYYMMDD-XXX
- **Order Statistics Endpoint**: Real-time order statistics
- **Status-based Filtering**: Filter orders by status
- **Business Ownership Verification**: Users can only access their own orders
- **PostgreSQL Enum Support**: Fixed enum type mapping with @JdbcTypeCode
- **Comprehensive Test Suite**: 
  - `BUSINESS_ORDER_CURL_TESTS.md` - Complete curl examples
  - `BUSINESS_ORDER_IMPLEMENTATION.md` - Implementation guide
  - `POSTGRES_ENUM_FIX.md` - Enum fix documentation
  - `TEST_README.md` - Quick start guide
  - Postman collection with 17 requests
  - Python automated test script
  - Bash test script for quick validation

### Changed
- **Architecture**: Implemented Clean Layered Architecture
- **Package Structure**: Added separate `business` package for business features
- **Security Config**: Updated to allow business endpoints (temporary)
- **Hibernate Config**: Added PostgreSQL dialect configuration
- **Application Properties**: Enhanced JPA/Hibernate settings

### Fixed
- **PostgreSQL Enum Type Mismatch**: Fixed payment_type, order_status, order_priority casting
- **Hibernate Enum Mapping**: Added @JdbcTypeCode for proper enum handling
- **Security Configuration**: Fixed 403 Forbidden on business endpoints
- **JPA Configuration**: Resolved LOB handling issues

### Security
- **JWT Authorization**: All order endpoints require valid JWT token
- **Ownership Validation**: Business can only access their own orders
- **Status-based Operations**: PENDING orders can be updated/deleted, ASSIGNED can be cancelled
- **Authorization Checks**: Every operation validates ownership

### Technical
- **Clean Layered Architecture**: Controller → Service → Repository → Model
- **SOLID Principles**: Interface segregation, dependency injection
- **Repository Pattern**: Custom queries and data access abstraction
- **DTO Pattern**: Separate request/response models
- **Exception Handling**: Custom exceptions with global handler
- **Validation**: Input validation with Jakarta validation

### Documentation
- Added detailed curl test guide with all endpoints
- Added implementation documentation
- Added PostgreSQL enum fix documentation
- Added Postman collection for easy testing
- Added Python and Bash test scripts
- Updated README with new features
- Updated VERSION.md with v1.2.0

---

## [1.1.0] - 2025-11-07

### Added
- **Unified Authentication System**: Single login endpoint for all user types
- **Automatic User Type Detection**: System automatically identifies Courier vs Business users
- **Courier Registration API**: New endpoint for courier self-registration
- **Business Registration API**: New endpoint for business self-registration
- **JWT Role-based Authorization**: Tokens now include user type (COURIER/BUSINESS)
- **Status-based Access Control**: Only active users can login
- **Test Documentation**: Added comprehensive test guides and results
- Database migration V10: Convert business status to VARCHAR
- Database migration V11: Convert courier status to VARCHAR

### Changed
- **Courier Model**: Updated status column from enum to VARCHAR(20)
- **Business Model**: Updated status column from enum to VARCHAR(20)
- **Login Response**: Now includes userType and status fields
- **JWT Token Structure**: Enhanced with role information

### Fixed
- Courier status enum type mismatch causing registration failures
- Business status enum type mismatch
- Database constraint issues with enum types
- Column definition conflicts in entity models

### Security
- Implemented BCrypt password hashing for all user types
- Added JWT token validation with role checking
- Implemented status-based login restrictions

### Documentation
- Added `docs/guides/TEST_LOGIN_GUIDE.md` - Complete testing guide with examples
- Added `docs/guides/TEST_RESULTS.md` - Detailed test results and API responses
- Added `docs/INDEX.md` - Documentation index and navigation
- Added `VERSION.md` - Version history and roadmap
- Updated API documentation with new endpoints

---

## [1.0.0] - 2025-10-31

### Added
- Initial project setup with Spring Boot 3.5.4
- PostgreSQL database integration
- Redis caching support
- Apache Kafka event streaming
- Docker containerization with docker-compose
- Nginx reverse proxy configuration
- Flyway database migrations (V1-V9)
- Basic entity models (User, Business, Courier, Order, Delivery)
- Health check endpoints with Spring Boot Actuator
- Swagger/OpenAPI documentation
- JWT authentication infrastructure
- Security configuration with Spring Security
- CORS configuration
- Database connection pooling with HikariCP

### Infrastructure
- Docker Compose orchestration
- PostgreSQL 15.4 container
- Redis 7.2 container
- Apache Kafka with Zookeeper
- Development and production profiles

### Documentation
- Project README
- Database design documentation
- API documentation structure
- Deployment guide
- Contributing guidelines
- Git workflow documentation

---

## Version Comparison

### v1.1.0 vs v1.0.0

**New Features:**
- 🎉 Unified login system (1 endpoint vs 2 separate)
- 🎉 Auto user type detection
- 🎉 Enhanced JWT tokens with roles
- 🎉 Courier self-registration
- 🎉 Business self-registration

**Improvements:**
- 🔧 Better database schema (VARCHAR vs ENUM)
- 🔧 More flexible user status management
- 🔧 Improved error handling

**Bug Fixes:**
- 🐛 Enum type mismatches resolved
- 🐛 Registration failures fixed
- 🐛 Database constraints corrected

---

## Migration Guide

### From v1.0.0 to v1.1.0

#### Database Changes
Run the following migrations automatically with Flyway:
- V10__Convert_business_status_to_varchar.sql
- V11__Convert_courier_status_to_varchar.sql

#### API Changes

**Old Login (v1.0.0):**
```
POST /api/v1/courier/login
POST /api/v1/business/login
```

**New Login (v1.1.0):**
```
POST /api/v1/auth/login
```

**Response Format Change:**
```json
// v1.0.0
{
  "token": "...",
  "userId": 1,
  "email": "..."
}

// v1.1.0
{
  "token": "...",
  "userId": 1,
  "email": "...",
  "userType": "COURIER",  // NEW
  "status": "ONLINE"      // NEW
}
```

#### Breaking Changes
- None - Old endpoints are deprecated but still functional

---

## Deprecation Notice

### Deprecated in v1.1.0
The following endpoints are deprecated and will be removed in v2.0.0:
- `POST /api/v1/courier/login` (Use `/api/v1/auth/login` instead)
- `POST /api/v1/business/login` (Use `/api/v1/auth/login` instead)

---

## Links

- [Version History](VERSION.md)
- [API Documentation](docs/api/API.md)
- [Contributing Guide](docs/guides/CONTRIBUTING.md)
- [GitHub Repository](https://github.com/YOUR_USERNAME/i-need-courier)

---

[1.1.0]: https://github.com/YOUR_USERNAME/i-need-courier/compare/v1.0.0...v1.1.0
[1.0.0]: https://github.com/YOUR_USERNAME/i-need-courier/releases/tag/v1.0.0
