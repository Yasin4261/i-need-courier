# 🔐 Unified Authentication API Documentation

## Overview

I Need Courier uses a **unified authentication system** with **role-based JWT tokens**. 

### Key Features
- ✅ **Single Login Endpoint** for all user types
- ✅ **Automatic User Type Detection** based on email
- ✅ **Role-Based JWT Tokens** (COURIER, BUSINESS, ADMIN)
- ✅ **Separate Registration Endpoints** for different user types

---

## 🚀 Authentication Endpoints

### 1. Unified Login (All User Types)

**POST** `/api/v1/auth/login`

Single endpoint for Couriers, Businesses, and Admins. System automatically detects user type from database.

**Request:**
```json
{
  "email": "user@example.com",
  "password": "password123"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9.eyJ1c2VySWQiOjEsImVtYWlsIjoidXNlckBleGFtcGxlLmNvbSIsInJvbGUiOiJDT1VSSUVSIn0...",
    "userId": 1,
    "email": "user@example.com",
    "name": "John Doe",
    "userType": "COURIER",  // or "BUSINESS", "ADMIN"
    "status": "ONLINE",
    "message": "Login successful"
  },
  "message": "Login successful"
}
```

**Error Response (401):**
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Invalid email or password",
  "path": "/api/v1/auth/login",
  "timestamp": "2025-10-31T12:00:00",
  "validationErrors": []
}
```

**JWT Token Structure:**
```json
{
  "userId": 1,
  "email": "user@example.com",
  "role": "COURIER",  // COURIER | BUSINESS | ADMIN
  "iat": 1730000000,
  "exp": 1730086400,
  "sub": "user@example.com"
}
```

---

## 📝 Registration Endpoints

### 2. Register Courier

**POST** `/api/v1/auth/register/courier`

Register a new courier. After registration, use `/api/v1/auth/login` to get JWT token.

**Request:**
```json
{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+905551234567",
  "password": "password123"
}
```

**Validation Rules:**
- `name`: 2-100 characters, required
- `email`: Valid email format, unique, required
- `phone`: Valid phone format (+[country][number]), unique, required
- `password`: 6-20 characters, required

**Success Response (200):**
```json
{
  "code": 200,
  "data": {
    "courierId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "message": "Registration successful"
  },
  "message": "Courier registration successful"
}
```

**Error Response - Duplicate Email (409):**
```json
{
  "status": 409,
  "error": "Conflict",
  "message": "Courier already exists with email: john@example.com",
  "path": "/api/v1/auth/register/courier",
  "timestamp": "2025-10-31T12:00:00",
  "validationErrors": []
}
```

### 3. Register Business

**POST** `/api/v1/auth/register/business`

Register a new business. Status will be **PENDING** until approved by admin.

**Request:**
```json
{
  "name": "Pizza Palace",
  "email": "contact@pizzapalace.com",
  "phone": "+905551234567",
  "password": "password123",
  "address": "Kadıköy, Istanbul",
  "contactPerson": "Mehmet Yılmaz",
  "businessType": "Restaurant"
}
```

**Validation Rules:**
- `name`: 2-200 characters, required
- `email`: Valid email format, unique, required
- `phone`: Valid phone format, unique, required
- `password`: 6-20 characters, required
- `address`: Max 500 characters, required
- `contactPerson`: Max 100 characters, optional
- `businessType`: Max 100 characters, optional

**Success Response (200):**
```json
{
  "code": 200,
  "data": {
    "businessId": 1,
    "name": "Pizza Palace",
    "email": "contact@pizzapalace.com",
    "status": "PENDING",
    "message": "Registration successful. Pending approval."
  },
  "message": "Business registration successful. Pending approval."
}
```

**Note:** Business must be **ACTIVE** to login. Admin approval required.

---

## 🧪 Test Credentials

### Couriers (All ACTIVE)
```
Email: courier1@test.com
Password: password123
Role: COURIER

Email: courier2@test.com
Password: password123
Role: COURIER

Email: courier3@test.com
Password: password123
Role: COURIER
```

### Businesses (ACTIVE)
```
Email: contact@pizzapalace.com
Password: password123
Role: BUSINESS
Status: ACTIVE ✅

Email: branch@burgerking.com
Password: password123
Role: BUSINESS
Status: ACTIVE ✅

Email: info@pharmacyplus.com
Password: password123
Role: BUSINESS
Status: ACTIVE ✅
```

### Business (PENDING - Cannot Login)
```
Email: pending@newbiz.com
Password: password123
Role: BUSINESS
Status: PENDING ❌
```

---

## 🔑 Using JWT Token

### Include in Headers

```bash
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

### Example Protected Request

```bash
curl -X GET http://localhost:8080/api/v1/courier/profile \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"
```

### Decode JWT to Get User Info

Frontend can decode JWT to get:
- `userId`: User ID
- `email`: User email
- `role`: User role for routing and authorization

**Role-Based Routing (Frontend):**
```javascript
const token = response.data.token;
const role = response.data.userType;

if (role === 'COURIER') {
  router.push('/courier/dashboard');
} else if (role === 'BUSINESS') {
  router.push('/business/dashboard');
} else if (role === 'ADMIN') {
  router.push('/admin/dashboard');
}
```

---

## 📊 Business Status Flow

```
Registration → PENDING (cannot login)
     ↓
Admin Approval
     ↓
ACTIVE (can login) → Login with JWT
     ↓
Access Business Endpoints
```

**Business Status Values:**
- `PENDING`: Awaiting admin approval (cannot login)
- `ACTIVE`: Approved, can login and use system
- `SUSPENDED`: Temporarily blocked
- `INACTIVE`: Deactivated

---

## 🔐 Security Notes

1. **Password Storage**: BCrypt hashed (never stored in plain text)
2. **JWT Expiration**: 24 hours (configurable)
3. **HTTPS Required**: Production should use HTTPS
4. **Token Refresh**: Implement refresh token mechanism (future)
5. **Rate Limiting**: Implement login rate limiting (future)

---

## 🚦 HTTP Status Codes

| Code | Meaning | When |
|------|---------|------|
| 200 | Success | Login/Registration successful |
| 400 | Bad Request | Validation errors |
| 401 | Unauthorized | Invalid credentials |
| 409 | Conflict | Email/Phone already exists |
| 500 | Server Error | Unexpected error |

---

## 📝 cURL Examples

### Courier Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "courier1@test.com",
    "password": "password123"
  }'
```

### Business Login
```bash
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "contact@pizzapalace.com",
    "password": "password123"
  }'
```

### Courier Registration
```bash
curl -X POST http://localhost:8080/api/v1/auth/register/courier \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jane Smith",
    "email": "jane@example.com",
    "phone": "+905559876543",
    "password": "password123"
  }'
```

### Business Registration
```bash
curl -X POST http://localhost:8080/api/v1/auth/register/business \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Burger House",
    "email": "info@burgerhouse.com",
    "phone": "+905559876543",
    "password": "password123",
    "address": "Beşiktaş, Istanbul",
    "contactPerson": "Ali Demir",
    "businessType": "Fast Food"
  }'
```

---

## 🎯 Best Practices

### Frontend Integration

1. **Store Token Securely**
   ```javascript
   localStorage.setItem('token', response.data.token);
   localStorage.setItem('userType', response.data.userType);
   localStorage.setItem('userId', response.data.userId);
   ```

2. **Add Token to All Requests**
   ```javascript
   axios.defaults.headers.common['Authorization'] = `Bearer ${token}`;
   ```

3. **Handle Token Expiration**
   ```javascript
   axios.interceptors.response.use(
     response => response,
     error => {
       if (error.response.status === 401) {
         // Token expired, redirect to login
         localStorage.clear();
         router.push('/login');
       }
       return Promise.reject(error);
     }
   );
   ```

4. **Role-Based UI**
   ```javascript
   const userType = localStorage.getItem('userType');
   
   if (userType === 'COURIER') {
     // Show courier menu
   } else if (userType === 'BUSINESS') {
     // Show business menu
   }
   ```

---

## 🔄 Workflow Diagram

```
┌─────────────┐
│   Client    │
└──────┬──────┘
       │
       │ POST /api/v1/auth/login
       │ {email, password}
       ↓
┌──────────────────┐
│ UnifiedAuthService│
└──────┬───────────┘
       │
       ├─→ Check Courier DB
       │   ├─ Found? → Validate → Generate JWT (role=COURIER)
       │   └─ Not found? ↓
       │
       └─→ Check Business DB
           ├─ Found? → Validate → Check Status → Generate JWT (role=BUSINESS)
           └─ Not found? → 401 Error
```

---

## ✅ Summary

**Single Login Endpoint** = `/api/v1/auth/login`
- ✅ Works for Couriers
- ✅ Works for Businesses
- ✅ Works for future user types (Admins, Customers, etc.)

**Registration Endpoints**:
- `/api/v1/auth/register/courier` - Courier registration
- `/api/v1/auth/register/business` - Business registration

**JWT Contains**:
- `userId` - For API calls
- `role` - For authorization and routing
- `email` - For display

**Result**: Clean, scalable, role-based authentication! 🎉

