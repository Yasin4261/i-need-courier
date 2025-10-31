# 🔐 Unified Authentication System - Quick Reference

## 🎯 Single Login Endpoint

```bash
POST /api/v1/auth/login
```

**Works for:**
- ✅ Couriers
- ✅ Businesses  
- ✅ Future user types (Admin, Customer, etc.)

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response includes JWT with role:**
```json
{
  "token": "eyJhbGc...",
  "userId": 1,
  "userType": "COURIER",  // or "BUSINESS", "ADMIN"
  "email": "user@example.com",
  "name": "User Name"
}
```

---

## 📝 Registration Endpoints

### Courier Registration
```bash
POST /api/v1/auth/register/courier

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+905551234567",
  "password": "password123"
}
```

### Business Registration
```bash
POST /api/v1/auth/register/business

{
  "name": "Pizza Palace",
  "email": "contact@pizzapalace.com",
  "phone": "+905551234567",
  "password": "password123",
  "address": "Istanbul"
}
```

**Note:** Business needs admin approval before login!

---

## 🧪 Test Credentials

### Couriers
```
courier1@test.com / password123
courier2@test.com / password123
courier3@test.com / password123
```

### Businesses (Active)
```
contact@pizzapalace.com / password123
branch@burgerking.com / password123
info@pharmacyplus.com / password123
```

---

## 📚 Full Documentation

See [UNIFIED_AUTH_API.md](UNIFIED_AUTH_API.md) for complete API documentation.

---

**Unified. Simple. Role-Based. 🚀**

