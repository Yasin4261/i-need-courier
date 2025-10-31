# API Documentation

## Authentication

All API endpoints require authentication. Use the following credentials:

- **Username**: admin
- **Password**: admin123

## Base URL

```
http://localhost:8080/api
```

## Courier Endpoints

### Get All Couriers
```http
GET /api/couriers
```

**Response:**
```json
[
  {
    "id": 1,
    "name": "Ali Özkan",
    "email": "ali.ozkan@courier.com",
    "phone": "+905551111111",
    "vehicleType": "MOTORCYCLE",
    "workType": "SHIFT",
    "isAvailable": true,
    "status": "ONLINE"
  }
]
```

### Get Courier by ID
```http
GET /api/couriers/{id}
```

### Create New Courier
```http
POST /api/couriers
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john.doe@courier.com",
  "phone": "+905551234567",
  "vehicleType": "MOTORCYCLE",
  "workType": "SHIFT",
  "isAvailable": true
}
```

### Update Courier
```http
PUT /api/couriers/{id}
Content-Type: application/json

{
  "name": "John Doe Updated",
  "email": "john.doe@courier.com",
  "phone": "+905551234567",
  "isAvailable": false
}
```

### Delete Courier
```http
DELETE /api/couriers/{id}
```

### Get Available Couriers
```http
GET /api/couriers/available
```

### Search Couriers
```http
GET /api/couriers/search?name=Ali
```

## Error Responses

### Validation Error (400)
```json
{
  "timestamp": "2025-08-04T12:00:00",
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "name": "Name is required",
    "email": "Email should be valid"
  }
}
```

### Not Found (404)
```json
{
  "timestamp": "2025-08-04T12:00:00",
  "status": 404,
  "error": "Courier not found",
  "message": "Courier with id 999 not found"
}
```

### Internal Server Error (500)
```json
{
  "timestamp": "2025-08-04T12:00:00",
  "status": 500,
  "error": "Internal Server Error",
  "message": "An unexpected error occurred"
}
```

## Health Check Endpoints

### Application Health
```http
GET /actuator/health
```

**Response:**
```json
{
  "status": "UP",
  "components": {
    "db": {
      "status": "UP",
      "details": {
        "database": "MySQL",
        "validationQuery": "isValid()"
      }
    },
    "redis": {
      "status": "UP"
    }
  }
}
```

### Application Info
```http
GET /actuator/info
```

### Application Metrics
```http
GET /actuator/metrics
```
