# I Need Courier - B2B Courier Management System

A comprehensive B2B courier management system built with Spring Boot, designed for businesses to manage courier operations efficiently.

## 🚀 Features

- **Courier Authentication**: JWT-based authentication system for couriers
- **Simple Registration & Login**: Streamlined auth endpoints with standardized responses
- **Email & Phone Validation**: Unique constraints and validation for user data
- **Secure Password Handling**: BCrypt password encryption
- **Database Schema Management**: Flyway migrations for consistent database state
- **RESTful API**: Clean, documented API endpoints
- **Containerized Deployment**: Docker and Docker Compose support

## 🏗️ Architecture

### Tech Stack
- **Backend**: Spring Boot 3.5.4 with Java 21
- **Database**: PostgreSQL with PostGIS extension
- **Cache**: Redis (configured but disabled for simplicity)
- **Message Queue**: Apache Kafka (configured but disabled for simplicity)
- **Security**: Spring Security with JWT tokens
- **Containerization**: Docker & Docker Compose
- **Database Migrations**: Flyway

### Current Implementation
The system currently focuses on courier management with:
1. **Courier Registration**: Simple registration with name, email, phone, and password
2. **JWT Authentication**: Secure login with JWT token generation
3. **Standardized API Responses**: Consistent response format with code, data, and message
4. **Robust Error Handling**: Detailed error messages for debugging and user feedback

## 🗄️ Database Schema

### Current Tables

#### Couriers Table
- **id**: Primary key (bigserial)
- **name**: Courier full name (varchar 100)
- **email**: Unique email address (varchar 100)
- **phone**: Unique phone number (varchar 20)
- **password_hash**: Encrypted password (varchar 255)
- **status**: User status enum (INACTIVE, ONLINE, OFFLINE, BUSY, SUSPENDED)
- **created_at**: Registration timestamp
- **updated_at**: Last update timestamp

*Note: The database contains additional tables for businesses, orders, coordinators, etc., but the current API implementation focuses on courier authentication.*

## 🚢 Deployment

### Using Docker Compose

1. **Clone the repository**
```bash
git clone https://github.com/Yasin4261/i-need-courier.git
cd i-need-courier
```

2. **Start all services**
```bash
docker compose up --build -d
```

3. **Access the application**
- Health Check: http://localhost:8081/actuator/health
- API Documentation: http://localhost:8081/swagger-ui.html

### Services Overview

| Service | Port | Description |
|---------|------|-------------|
| Spring Boot App | 8081 | Main application server |
| PostgreSQL | 5433 | Primary database |
| Redis | 6380 | Cache (disabled) |
| Kafka | 29092 | Message broker (disabled) |

## 🔐 Authentication

### API Endpoints

#### Registration
```bash
POST /api/v1/simple-auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com", 
  "phone": "+905551234567",
  "password": "securepass123"
}
```

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
  "message": "Registration successful"
}
```

**Error Response (409 - Email/Phone exists):**
```json
{
  "code": 409,
  "data": null,
  "message": "Courier already exists with email: john@example.com"
}
```

#### Login
```bash
POST /api/v1/simple-auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "securepass123"
}
```

**Success Response (200):**
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "courierId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "status": "Login successful"
  },
  "message": "Login successful"
}
```

### Using JWT Token
Include the token in Authorization header for protected endpoints:
```bash
Authorization: Bearer eyJhbGciOiJIUzM4NCJ9...
```

## 🛠️ Development

### Prerequisites
- Java 21+
- Maven 3.6+
- Docker & Docker Compose

### Local Development Setup

1. **Start Database and Services**
```bash
docker compose up -d postgres redis kafka
```

2. **Run Application Locally**
```bash
./mvnw spring-boot:run
```

### Database Migrations

The project uses Flyway for database migrations:
- `V1__Create_initial_tables.sql`: Creates all database tables
- `V2__Insert_initial_data.sql`: Loads sample data
- `V3__Add_location_columns_to_businesses.sql`: Location updates
- `V4__Create_couriers_table.sql`: Courier table (skipped if exists)
- `V5__Add_password_hash_to_couriers.sql`: Adds password_hash column
- `V6__Update_user_status_enum.sql`: Updates user status enum values

Migrations run automatically on application startup.

## 📡 API Endpoints

### Authentication Endpoints
- `POST /api/v1/simple-auth/register` - Register new courier
- `POST /api/v1/simple-auth/login` - Login and get JWT token

### Legacy Endpoints (Available but Complex)
- `POST /api/v1/courier/auth/register` - Legacy registration endpoint
- `POST /api/v1/courier/auth/login` - Legacy login endpoint

### Health & Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information  
- `GET /actuator/metrics` - Application metrics
- `GET /swagger-ui.html` - API Documentation

## 📋 Validation Rules

### Registration Validation
- **Name**: 2-100 characters, required
- **Email**: Valid email format, unique, required
- **Phone**: Valid phone format, unique, required  
- **Password**: 6-20 characters, required

### Response Format
All API responses follow this standard format:
```json
{
  "code": 200,
  "data": { ... },
  "message": "Success message"
}
```

## 🚨 Error Handling

The system includes comprehensive error handling:
- **Validation Errors**: Field-level validation with detailed messages
- **Duplicate Data**: Specific errors for email/phone conflicts
- **Authentication Errors**: Invalid credentials handling
- **Database Errors**: Graceful handling of database issues
- **Generic Errors**: Fallback error handling with detailed logging

## 🔧 Configuration

### Application Properties
- **JWT Secret**: Configurable via `jwt.secret`
- **JWT Expiration**: Configurable via `jwt.expiration-hours` (default: 24h)
- **Database**: PostgreSQL connection settings
- **Logging**: Debug logging enabled for development

## 🔮 Future Enhancements

- [ ] Order management endpoints
- [ ] Coordinator management
- [ ] Business management  
- [ ] Real-time order tracking
- [ ] Mobile application for couriers
- [ ] Advanced analytics and reporting
- [ ] Integration with mapping services
- [ ] Automated dispatch algorithms

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 🙋‍♂️ Support

For support and questions:
- Create an issue in the GitHub repository
- Check the API documentation at `/swagger-ui.html`
- Review the standardized API responses
