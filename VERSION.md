# Version History - I Need Courier

## Current Version: v1.3.0

**Release Date:** November 14, 2025

---

## Version 1.3.0 (November 14, 2025)

### 🚚 Shift Management System
- ✅ Courier shift templates listing
- ✅ Shift reservation (date + template)
- ✅ Check-in / Check-out operations
- ✅ Cancel reservation
- ✅ Active and upcoming shifts endpoints
- ✅ Filter by shift status

### 🔧 Technical Fixes
- ✅ PostgreSQL enum mismatch fixed for shifts (converted to VARCHAR)
- ✅ Flyway migration added (V13)
- ✅ Global exception handler improvements
- ✅ Security: JwtAuthenticationEntryPoint + JwtAccessDeniedHandler

### 🧪 Testing & Tooling
- ✅ New bash script: `test-shift-yasin.sh` (idempotent, auto-cancel)
- ✅ Postman collections for Shift API
- ✅ Shift guide docs under `docs/guides/`

---

## Version 1.2.0 (November 7, 2025)

### 🎉 Major Features

#### Business Order Management System
- ✅ **Full CRUD Operations** for business orders
- ✅ **8 RESTful API Endpoints** for order management
- ✅ **Auto-generated Order Numbers** (ORD-YYYYMMDD-XXX format)
- ✅ **Order Status Workflow** (PENDING → ASSIGNED → PICKED_UP → IN_TRANSIT → DELIVERED)
- ✅ **Business Ownership Verification** (users can only access their own orders)
- ✅ **Status-based Operation Control** (PENDING orders can be updated/deleted)
- ✅ **Order Filtering** by status
- ✅ **Order Statistics** endpoint

#### API Endpoints
- ✅ `POST /api/v1/business/orders` - Create order
- ✅ `GET /api/v1/business/orders` - List all orders
- ✅ `GET /api/v1/business/orders?status=X` - Filter by status
- ✅ `GET /api/v1/business/orders/{id}` - Get order details
- ✅ `PUT /api/v1/business/orders/{id}` - Update order
- ✅ `DELETE /api/v1/business/orders/{id}` - Delete order
- ✅ `POST /api/v1/business/orders/{id}/cancel` - Cancel order
- ✅ `GET /api/v1/business/orders/statistics` - Get statistics

### 🏗️ Architecture Improvements

#### Clean Layered Architecture
- ✅ **Separate business package** for business-specific features
- ✅ **Service layer** with interface and implementation
- ✅ **Repository pattern** for data access
- ✅ **DTO pattern** for request/response handling
- ✅ **SOLID principles** implementation

#### New Components
- ✅ **Order Entity** with PostgreSQL enum support
- ✅ **OrderStatus, OrderPriority, PaymentType** enums
- ✅ **BusinessOrderService** & **BusinessOrderServiceImpl**
- ✅ **BusinessOrderController** (8 endpoints)
- ✅ **OrderRepository** with custom queries
- ✅ **OrderCreateRequest, OrderUpdateRequest, OrderResponse** DTOs

### 🔧 Technical Improvements

#### PostgreSQL Enum Type Support
- ✅ Fixed enum type mapping with `@JdbcTypeCode(SqlTypes.NAMED_ENUM)`
- ✅ Added Hibernate PostgreSQL dialect configuration
- ✅ Resolved varchar to enum casting issues

#### Security & Authorization
- ✅ JWT-based authorization for business endpoints
- ✅ Ownership verification on all operations
- ✅ Status-based access control

### 📚 Documentation

#### Test Documentation
- ✅ `docs/guides/BUSINESS_ORDER_CURL_TESTS.md` - Complete curl test guide with examples
- ✅ `docs/guides/BUSINESS_ORDER_IMPLEMENTATION.md` - Implementation details
- ✅ `docs/guides/BUSINESS_ORDER_PLAN.md` - Planning document
- ✅ `docs/guides/POSTGRES_ENUM_FIX.md` - Enum type fix documentation
- ✅ `TEST_README.md` - Quick test instructions
- ✅ **Postman Collection** with 17 ready-to-use requests
- ✅ **Python test script** for automated testing
- ✅ **Bash test script** for quick validation

### 🐛 Bug Fixes
- ✅ Fixed PostgreSQL enum type mismatch (payment_type, order_status, order_priority)
- ✅ Resolved Hibernate varchar to enum casting
- ✅ Fixed security configuration for business endpoints
- ✅ Corrected JPA/Hibernate dialect settings

### 🧪 Testing
- ✅ Integration test scripts (Python & Bash)
- ✅ Postman collection for manual testing
- ✅ Enum type fix verification
- ✅ Full CRUD operation tests
- ✅ Authorization tests

### 📊 Statistics
- **43+ files** created/modified
- **~3500 lines** of code added
- **8 API endpoints** implemented
- **5 documentation files** created
- **3 test scripts** provided

### 🔄 Breaking Changes
None

### 📦 Migration Required
No - Uses existing database schema

---

## Version 1.1.0 (November 7, 2025)

### 🎉 Major Features

#### Unified Authentication System
- ✅ **Single Login Endpoint** for all user types
- ✅ **Automatic User Type Detection** (Courier/Business)
- ✅ **JWT Token with Role-based Authorization**
- ✅ **Secure Password Management** with BCrypt

#### User Management
- ✅ **Courier Registration & Login**
- ✅ **Business Registration & Login**
- ✅ **Status-based Access Control**

### 🔧 Technical Improvements

#### Database Migrations
- ✅ `V10__Convert_business_status_to_varchar.sql` - Business status column optimization
- ✅ `V11__Convert_courier_status_to_varchar.sql` - Courier status column optimization

#### Model Updates
- ✅ Fixed Courier model enum type issues
- ✅ Fixed Business model enum type issues
- ✅ Improved column definitions for better compatibility

#### API Enhancements
- ✅ Unified login endpoint: `POST /api/v1/auth/login`
- ✅ Courier registration: `POST /api/v1/auth/register/courier`
- ✅ Business registration: `POST /api/v1/auth/register/business`

### 📚 Documentation
- ✅ Added `docs/guides/TEST_LOGIN_GUIDE.md` - Comprehensive login testing guide
- ✅ Added `docs/guides/TEST_RESULTS.md` - Detailed test results and examples
- ✅ Added `docs/INDEX.md` - Documentation index and navigation guide
- ✅ Updated API documentation

### 🐛 Bug Fixes
- ✅ Fixed courier status enum type mismatch
- ✅ Fixed business status enum type mismatch
- ✅ Resolved database constraint issues
- ✅ Corrected column definitions in entity models

### 🔒 Security
- ✅ BCrypt password hashing
- ✅ JWT token validation
- ✅ Role-based access control
- ✅ Status-based login restrictions

---

## Version 1.0.0 (October 2025)

### Initial Release

#### Core Features
- ✅ RESTful API with Spring Boot 3.5.4
- ✅ PostgreSQL database integration
- ✅ Redis caching support
- ✅ Apache Kafka for event streaming
- ✅ Flyway database migrations
- ✅ Docker containerization
- ✅ Swagger/OpenAPI documentation

#### Basic Entities
- Users
- Businesses
- Couriers
- Orders
- Deliveries

#### Infrastructure
- Docker Compose setup
- Nginx reverse proxy
- Health check endpoints
- Actuator monitoring

---

## Upcoming Features (v1.2.0 - Planned)

### 🚀 Features in Development
- [ ] Admin user type and management panel
- [ ] Real-time order tracking with WebSocket
- [ ] Push notifications for order updates
- [ ] Courier location tracking
- [ ] Advanced search and filtering
- [ ] Analytics dashboard
- [ ] Multi-language support
- [ ] Email verification system
- [ ] SMS notifications
- [ ] Rate limiting and API throttling

### 📱 Mobile Support
- [ ] Mobile-optimized API endpoints
- [ ] Mobile app documentation
- [ ] Push notification integration

### 🔧 Technical Debt
- [ ] Add comprehensive unit tests
- [ ] Add integration tests
- [ ] Improve error handling
- [ ] Add request validation
- [ ] Performance optimization
- [ ] Code coverage improvement

---

## Version Numbering

We follow [Semantic Versioning](https://semver.org/):

- **MAJOR** version: Incompatible API changes
- **MINOR** version: Backward-compatible new features
- **PATCH** version: Backward-compatible bug fixes

Example: `v1.1.0`
- Major: 1 (Initial stable release)
- Minor: 1 (New unified auth system)
- Patch: 0 (No patches yet)

---

## Support

For issues and questions:
- GitHub Issues: [Create an issue](https://github.com/YOUR_USERNAME/i-need-courier/issues)
- Email: support@ineedcourier.com
- Documentation: [docs/](./docs/)

---

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
