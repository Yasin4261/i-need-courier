# 📚 I Need Courier - Documentation Index

Welcome to the **I Need Courier** documentation! This guide will help you navigate through all available documentation.

---

## 🚀 Quick Start

**New to the project?** Start here:

1. 📖 [README.md](../README.md) - Project overview and quick start
2. 🎯 [QUICKSTART.md](guides/QUICKSTART.md) - Detailed setup guide
3. 🧪 [TEST_LOGIN_GUIDE.md](guides/TEST_LOGIN_GUIDE.md) - Testing the authentication

---

## 📋 Documentation Structure

### 🎯 Getting Started
- [README](../README.md) - Main project overview
- [Quick Start Guide](guides/QUICKSTART.md) - Step-by-step setup
- [Installation Guide](setup/PROJECT_ORGANIZATION.md) - Detailed installation

### 🔐 Authentication & API
- [Unified Auth API](api/UNIFIED_AUTH_API.md) - New unified login system
- [Courier Auth API](api/COURIER_AUTH_API.md) - Courier-specific endpoints
- [Auth Quick Reference](api/AUTH_QUICK_REFERENCE.md) - Quick API reference
- [API Documentation](api/API.md) - Complete API docs
- [Test Guide](guides/TEST_LOGIN_GUIDE.md) - Testing authentication
- [Test Results](guides/TEST_RESULTS.md) - Test examples and results

### 🗄️ Database
- [Database Design](DATABASE_DESIGN.md) - Overall database architecture
- [Database Schema](DATABASE.md) - Detailed table schemas
- [Database for Backend](DATABASE_FOR_BACKEND.md) - Backend-specific DB info
- [Database Graph](db_graph.png) - Visual schema diagram

### 📅 Shift Management
- [Shift Management Guide](guides/SHIFT_MANAGEMENT_GUIDE.md) - Complete shift system guide
- [On-Duty System](guides/ON_DUTY_SYSTEM.md) - **NEW** On-duty couriers tracking system
- [On-Duty Test Guide](guides/ON_DUTY_TEST_GUIDE.md) - **NEW** Testing on-duty functionality
- [Shift System Changelog](SHIFT_SYSTEM_CHANGELOG.md) - What's new in shift system
- [Database Design](DATABASE_DESIGN.md#3-vardiya-yönetim-sistemi) - Shift tables and logic

### 🏗️ Architecture
- [Clean Layered Architecture](guides/CLEAN_LAYERED_ARCHITECTURE.md) - Current architecture
- [Old Hexagonal Architecture](setup/OLD_HEXAGONAL_ARCHITECTURE.md) - Previous design (deprecated)
- [Migration to Clean Architecture](setup/MIGRATION_TO_CLEAN_ARCHITECTURE.md) - Migration guide

### 🔧 Development
- [Contributing Guide](guides/CONTRIBUTING.md) - How to contribute
- [Git Workflow](guides/GIT_WORKFLOW.md) - Git branching strategy
- [GitHub Setup](setup/GITHUB_SETUP_COMPLETE.md) - GitHub configuration

### 🚀 Deployment
- [Deployment Guide](DEPLOYMENT.md) - Production deployment
- [Docker Setup](../compose.yaml) - Docker configuration

### 📦 Version Management
- [Version History](../VERSION.md) - All versions and features
- [Changelog](../CHANGELOG.md) - Detailed changes per version
- [Migration Summary](setup/MIGRATION_SUMMARY.md) - Migration notes

---

## 🎯 By Role

### For New Developers
1. [README](../README.md)
2. [Quick Start Guide](guides/QUICKSTART.md)
3. [Clean Layered Architecture](guides/CLEAN_LAYERED_ARCHITECTURE.md)
4. [Contributing Guide](guides/CONTRIBUTING.md)
5. [Git Workflow](guides/GIT_WORKFLOW.md)

### For Frontend Developers
1. [API Documentation](api/API.md)
2. [Unified Auth API](api/UNIFIED_AUTH_API.md)
3. [Auth Quick Reference](api/AUTH_QUICK_REFERENCE.md)
4. [Shift Management Guide](guides/SHIFT_MANAGEMENT_GUIDE.md) - Shift API endpoints
5. [Test Examples](guides/TEST_RESULTS.md)

### For Courier Mobile App Developers
1. [Shift Management Guide](guides/SHIFT_MANAGEMENT_GUIDE.md) - Complete shift workflow
2. [Unified Auth API](api/UNIFIED_AUTH_API.md) - Login/Register
3. [Auth Quick Reference](api/AUTH_QUICK_REFERENCE.md) - Quick reference

### For DevOps Engineers
1. [Deployment Guide](DEPLOYMENT.md)
2. [Docker Setup](../compose.yaml)
3. [Database Design](DATABASE.md)

### For Project Managers
1. [Version History](../VERSION.md)
2. [Changelog](../CHANGELOG.md)
3. [README](../README.md)

---

## 📊 Current Version: v1.2.0

### What's New in v1.2.0 (Latest)
- ✅ **Shift Management System** - Full shift reservation and check-in/out
- ✅ **FIFO Order Assignment** - Queue-based fair distribution
- ✅ **Shift Templates** - Reusable shift schedules
- ✅ **on_duty_since Tracking** - Automatic queue management
- ✅ **REST API for Shifts** - Complete courier shift endpoints

### Previous Version (v1.1.0)
- ✅ Unified Authentication System
- ✅ Auto User Type Detection
- ✅ Courier Self-Registration
- ✅ Business Self-Registration
- ✅ Enhanced JWT with Roles

**See:** [Version History](../VERSION.md) | [Changelog](../CHANGELOG.md)

---

## 🔗 Quick Links

### API Endpoints
```
POST /api/v1/auth/login                   - Unified login
POST /api/v1/auth/register/courier        - Register courier
POST /api/v1/auth/register/business       - Register business
GET  /actuator/health                     - Health check
GET  /swagger-ui.html                     - API documentation
```

### Development URLs
- **Backend API:** http://localhost:8081
- **Swagger UI:** http://localhost:8081/swagger-ui.html
- **Actuator:** http://localhost:8081/actuator
- **Health Check:** http://localhost:8081/actuator/health

---

## 🆘 Need Help?

### Documentation Issues
- Missing documentation? [Create an issue](https://github.com/YOUR_USERNAME/i-need-courier/issues)
- Found an error? [Submit a PR](https://github.com/YOUR_USERNAME/i-need-courier/pulls)

### Technical Support
- Email: support@ineedcourier.com
- GitHub Issues: [Report a bug](https://github.com/YOUR_USERNAME/i-need-courier/issues)

---

## 📝 Documentation Standards

When contributing documentation:
- Use Markdown format
- Include code examples
- Add clear section headers
- Keep it up-to-date with code changes
- Follow the existing structure

See: [Contributing Guide](guides/CONTRIBUTING.md)

---

## 🗺️ Documentation Map

```
docs/
├── 📄 INDEX.md (you are here)
├── 📊 DATABASE.md
├── 📊 DATABASE_FOR_BACKEND.md
├── 📊 DATABASE_DESIGN.md
├── 🚀 DEPLOYMENT.md
├── ℹ️ HELP.md
├── api/
│   ├── API.md
│   ├── AUTH_QUICK_REFERENCE.md
│   ├── COURIER_AUTH_API.md
│   └── UNIFIED_AUTH_API.md
├── guides/
│   ├── CLEAN_LAYERED_ARCHITECTURE.md
│   ├── CONTRIBUTING.md
│   ├── GIT_WORKFLOW.md
│   ├── QUICKSTART.md
│   ├── TEST_LOGIN_GUIDE.md
│   └── TEST_RESULTS.md
└── setup/
    ├── GITHUB_SETUP_COMPLETE.md
    ├── MIGRATION_SUMMARY.md
    ├── MIGRATION_TO_CLEAN_ARCHITECTURE.md
    ├── OLD_HEXAGONAL_ARCHITECTURE.md
    ├── PROJECT_ORGANIZATION.md
    └── UNIFIED_AUTH_COMPLETE.md

Root Directory:
├── 📄 README.md
├── 📋 VERSION.md
└── 📋 CHANGELOG.md
```

---

**Last Updated:** November 7, 2025  
**Version:** 1.1.0

