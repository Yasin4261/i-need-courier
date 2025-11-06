# Changelog

All notable changes to the I Need Courier project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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

