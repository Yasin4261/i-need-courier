# Courier JWT Authentication API

## Overview
This implementation provides JWT-based authentication for couriers in the I-Need-Courier application following hexagonal architecture principles.

## API Endpoints

### Authentication Endpoints

#### Register Courier
```
POST /api/v1/courier/auth/register
Content-Type: application/json

{
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+905551234567",
    "password": "password123"
}
```

Response:
```json
{
    "courierId": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "message": "Courier registered successfully"
}
```

#### Login Courier
```
POST /api/v1/courier/auth/login
Content-Type: application/json

{
    "email": "john.doe@example.com",
    "password": "password123"
}
```

Response:
```json
{
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "courierId": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "status": "INACTIVE"
}
```

### Protected Endpoints (Requires Bearer Token)

#### Get Courier Profile
```
GET /api/v1/courier/profile
Authorization: Bearer {token}
```

Response:
```json
{
    "id": 1,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "phone": "+905551234567",
    "status": "ONLINE",
    "createdAt": "2024-01-15T10:30:00",
    "lastLoginAt": "2024-01-15T14:25:30"
}
```

#### Update Courier Status
```
PUT /api/v1/courier/status?status=ONLINE
Authorization: Bearer {token}
```

Response:
```json
{
    "message": "Status updated successfully",
    "newStatus": "ONLINE"
}
```

## Courier Status Types
- `INACTIVE`: Registered but not active
- `ONLINE`: Active and can receive orders
- `OFFLINE`: Offline
- `BUSY`: Currently delivering an order
- `SUSPENDED`: Account suspended

## Error Responses

### Validation Error
```json
{
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed",
    "fieldErrors": {
        "email": "Invalid email format",
        "password": "Password must be between 6 and 20 characters"
    },
    "timestamp": "2024-01-15T10:30:00"
}
```

### Authentication Error
```json
{
    "code": "INVALID_CREDENTIALS",
    "message": "Invalid password",
    "timestamp": "2024-01-15T10:30:00"
}
```

## How to Use

1. **Register a new courier** using the registration endpoint
2. **Login** with credentials to get JWT token
3. **Include the token** in Authorization header for protected endpoints:
   ```
   Authorization: Bearer your-jwt-token-here
   ```

## Security Features

- JWT tokens expire after 24 hours (configurable)
- Passwords are hashed using BCrypt
- CORS enabled for cross-origin requests
- Stateless authentication
- Input validation on all endpoints

## Database Schema

The `couriers` table is automatically created with the following structure:
- `id`: Primary key
- `name`: Courier name
- `email`: Unique email address
- `phone`: Phone number
- `password_hash`: BCrypt hashed password
- `status`: Current courier status
- `created_at`: Registration timestamp
- `last_login_at`: Last login timestamp
