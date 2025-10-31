# 🚀 Quick Start Guide - I Need Courier

## ✅ Migration Complete!

The project has been successfully migrated to **Clean Layered Architecture**. Everything is simpler and easier to understand now!

---

## 🏃 Quick Start (3 Steps)

### 1️⃣ Start Database
```bash
docker compose up -d postgres
```

### 2️⃣ Build Project
```bash
./mvnw clean package -DskipTests
```

### 3️⃣ Run Application
```bash
./start.sh
```

**That's it! 🎉** The application will be running on http://localhost:8080

---

## 🧪 Test the API

### ✅ Register a New Courier
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John Doe",
    "email": "john@example.com",
    "phone": "+905551234567",
    "password": "password123"
  }'
```

**Expected Success Response (200)**:
```json
{
  "code": 200,
  "data": {
    "courierId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "message": "Registration successful"
  },
  "message": "Registration successful"
}
```

**Expected Error Response - Validation Failed (400)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/auth/register",
  "timestamp": "2025-10-31T05:30:00",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid"
    },
    {
      "field": "password",
      "message": "Password must be between 6 and 20 characters"
    }
  ]
}
```

**Expected Error Response - User Already Exists (409)**:
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Courier already exists with email: john@example.com",
  "path": "/api/v1/auth/register",
  "timestamp": "2025-10-31T05:30:00",
  "validationErrors": []
}
```

### ✅ Login with Test Courier
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "courier1@test.com",
    "password": "password123"
  }'
```

**Expected Success Response (200)**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "courierId": 1,
    "name": "Test Courier 1",
    "email": "courier1@test.com",
    "status": "Login successful"
  },
  "message": "Login successful"
}
```

**Expected Error Response - Invalid Credentials (401)**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login",
  "timestamp": "2025-10-31T05:30:00",
  "validationErrors": []
}
```

### 🔍 Test Error Scenarios

**Test Invalid Email Format**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "John",
    "email": "invalid-email",
    "phone": "+905551234567",
    "password": "pass"
  }'
```

**Test Duplicate Registration**:
```bash
# First registration
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John","email":"test@test.com","phone":"+905551234567","password":"password123"}'

# Try same email again (should fail with 409)
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"Jane","email":"test@test.com","phone":"+905559999999","password":"password123"}'
```

**Test Wrong Password**:
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "courier1@test.com",
    "password": "wrongpassword"
  }'
```

---

## 📚 Documentation

| Document | Description |
|----------|-------------|
| [Architecture Guide](documents/CLEAN_LAYERED_ARCHITECTURE.md) | Learn about Clean Layered Architecture |
| [Git Workflow](GIT_WORKFLOW.md) | Branch strategy and commit conventions |
| [Contributing](CONTRIBUTING.md) | How to contribute to the project |
| [Migration Guide](MIGRATION_TO_CLEAN_ARCHITECTURE.md) | Details about the architecture migration |

---

## 🏗️ Project Structure

```
src/main/java/com/api/demo/
├── controller/     # REST API endpoints
├── service/       # Business logic
├── repository/    # Database access
├── model/         # Entities
├── dto/           # Data Transfer Objects
├── exception/     # Custom exceptions
├── config/        # Configuration
└── security/      # Security & JWT
```

**Simple & Clear! 😊**

---

## 🛠️ Useful Commands

### Start Application
```bash
./start.sh
```

### Stop Application
```bash
./stop.sh
```

### View Logs
```bash
tail -f logs/app.log
```

### Rebuild
```bash
./mvnw clean install
```

### Run Tests
```bash
./mvnw test
```

---

## 🐛 Troubleshooting

### Port Already in Use
```bash
lsof -ti:8080 | xargs kill -9
```

### Database Connection Failed
```bash
docker compose restart postgres
docker compose logs postgres
```

### Clean Build
```bash
./mvnw clean install
```

---

## 📞 Need Help?

1. **Check Logs**: `tail -f logs/app.log`
2. **Read Docs**: See [Architecture Guide](documents/CLEAN_LAYERED_ARCHITECTURE.md)
3. **Create Issue**: Use GitHub issue templates
4. **Ask Team**: Team chat or code review

---

## 🎯 Next Steps

- ✅ Clean Layered Architecture implemented
- ✅ JWT Authentication working
- ✅ Documentation complete
- ✅ GitHub workflow setup
- 🔜 Add integration tests
- 🔜 Implement order management
- 🔜 Add real-time tracking

---

**Happy Coding! 🚀**

