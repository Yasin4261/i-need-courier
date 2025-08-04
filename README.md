# I Need Courier - B2B Courier Management System

A comprehensive B2B courier management system built with Spring Boot, designed for businesses to manage courier operations efficiently.

## 🚀 Features

- **Business Management**: Register and manage businesses that place orders
- **Courier Management**: Manage courier staff with shift scheduling and vehicle assignment
- **Coordinator System**: Field coordinators supervise couriers and assign orders
- **Order Tracking**: Real-time order tracking with GPS coordinates
- **Vehicle Management**: Assign and track courier vehicles
- **Flexible Work Types**: Support for both shift-based and flexible (joker) work schedules
- **Payment System**: Multiple payment types including business accounts and cash collection

## 🏗️ Architecture

### Tech Stack
- **Backend**: Spring Boot 3.5.4 with Java 21
- **Database**: MySQL 8.0 with Flyway migrations
- **Cache**: Redis for session management
- **Message Queue**: Apache Kafka
- **Security**: Spring Security
- **Containerization**: Docker & Docker Compose
- **Reverse Proxy**: Nginx

### System Design
The system follows a B2B model where:
1. **Businesses** place orders in the system
2. **Coordinators** assign orders to available couriers
3. **Couriers** pick up packages from businesses and deliver to end customers
4. **Real-time tracking** throughout the delivery process

## 🗄️ Database Schema

### Core Entities

#### System Users
- Admins, Coordinators, and Couriers with role-based access
- Secure authentication and authorization

#### Businesses
- Companies that place delivery orders
- Contact information and address details
- Payment terms and credit limits

#### Orders
- Complete order lifecycle management
- Pickup and delivery addresses with detailed descriptions
- Package information including weight and value
- Payment and collection details

#### Couriers
- Personal and professional information
- Shift scheduling (fixed shifts or flexible hours)
- Vehicle assignments and current location
- Performance tracking

#### Coordinators
- Field supervisors managing courier teams
- Coverage area assignments
- Capacity management

## 🚢 Deployment

### Using Docker Compose

1. **Clone the repository**
```bash
git clone https://github.com/Yasin4261/i-need-courier.git
cd i-need-courier
```

2. **Start all services**
```bash
docker-compose up --build -d
```

3. **Access the application**
- Main Application: http://localhost
- Direct API: http://localhost:8080
- Health Check: http://localhost/health

### Services Overview

| Service | Port | Description |
|---------|------|-------------|
| Nginx | 80 | Reverse proxy and load balancer |
| Spring Boot App | 8080 | Main application server |
| MySQL | 3306 | Primary database |
| Redis | 6379 | Session store and cache |
| Kafka | 9092 | Message broker |

## 🔐 Authentication

### Default Login
- **Username**: admin
- **Password**: admin123

### User Roles
- **ADMIN**: Full system access
- **COORDINATOR**: Manage couriers and assign orders
- **COURIER**: View assigned orders and update status

## 📊 Sample Data

The system comes with pre-loaded sample data:
- 1 System Admin
- 2 Coordinators (different shifts and coverage areas)
- 3 Couriers with various vehicle types
- 5 Businesses (restaurants, pharmacies, markets)
- Sample orders in different states

## 🛠️ Development

### Prerequisites
- Java 21+
- Maven 3.6+
- Docker & Docker Compose

### Local Development Setup

1. **Database Setup**
```bash
# Using Docker
docker run -d --name courier-mysql \
  -e MYSQL_ROOT_PASSWORD=root_password \
  -e MYSQL_DATABASE=courier_db \
  -e MYSQL_USER=courier_user \
  -e MYSQL_PASSWORD=courier_password \
  -p 3306:3306 mysql:8.0
```

2. **Redis Setup**
```bash
docker run -d --name courier-redis -p 6379:6379 redis:7-alpine
```

3. **Run Application**
```bash
./mvnw spring-boot:run
```

### Database Migrations

The project uses Flyway for database migrations:
- `V1__Create_initial_tables.sql`: Creates all database tables
- `V2__Insert_initial_data.sql`: Loads sample data

Migrations run automatically on application startup.

## 📡 API Endpoints

### Courier Management
- `GET /api/couriers` - List all couriers
- `GET /api/couriers/{id}` - Get courier details
- `POST /api/couriers` - Create new courier
- `PUT /api/couriers/{id}` - Update courier
- `DELETE /api/couriers/{id}` - Delete courier
- `GET /api/couriers/available` - Get available couriers
- `GET /api/couriers/search?name={name}` - Search couriers

### Health Monitoring
- `GET /actuator/health` - Application health status
- `GET /actuator/info` - Application information
- `GET /actuator/metrics` - Application metrics

## 🔄 Business Flow

### Order Lifecycle
1. **Order Creation**: Business places an order in the system
2. **Coordinator Assignment**: Order assigned to area coordinator
3. **Courier Assignment**: Coordinator assigns order to available courier
4. **Pickup**: Courier picks up package from business
5. **In Transit**: Package being delivered to end customer
6. **Delivery**: Package delivered and confirmed
7. **Tracking**: Complete audit trail maintained

### Work Schedule Types
- **Shift Workers**: Fixed working hours with specific start/end times
- **Joker Workers**: Flexible schedule, available as needed

## 🚨 Error Handling

The system includes comprehensive error handling:
- Global exception handler for API errors
- Validation errors with detailed field-level messages
- Database constraint violations
- Authentication and authorization errors

## 📈 Monitoring & Observability

- **Health Checks**: Built-in health monitoring
- **Metrics**: Application performance metrics
- **Logging**: Comprehensive logging throughout the application
- **Database Monitoring**: Connection pool and query performance

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
- Check the existing documentation
- Review the API endpoints and sample data

## 🔮 Future Enhancements

- [ ] Mobile application for couriers
- [ ] Advanced analytics and reporting
- [ ] Integration with mapping services
- [ ] Automated dispatch algorithms
- [ ] Customer notification system
- [ ] Performance analytics dashboard
