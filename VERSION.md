# Version History - I Need Courier

## Current Version: v1.1.0

**Release Date:** November 7, 2025

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

