# Database Schema Documentation

## Overview

The I Need Courier system uses a MySQL database with the following core entities designed for B2B courier operations.

## Entity Relationship Diagram

```
SystemUser (1) ←→ (1) Coordinator (1) ←→ (N) Courier
                         ↓                    ↓
Business (1) ←→ (N) Order ←→ (1) Courier ←→ (1) Vehicle
                    ↓
              OrderTracking (N)
```

## Tables

### system_users
System users including admins, coordinators, and couriers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| username | VARCHAR(50) | UNIQUE, NOT NULL | Login username |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| password | VARCHAR(255) | NOT NULL | Encrypted password |
| first_name | VARCHAR(50) | | First name |
| last_name | VARCHAR(50) | | Last name |
| phone | VARCHAR(20) | | Phone number |
| role | ENUM | NOT NULL | ADMIN, COORDINATOR, COURIER |
| is_active | BOOLEAN | DEFAULT TRUE | Account status |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes:**
- `idx_email (email)`
- `idx_username (username)`
- `idx_role (role)`
- `idx_is_active (is_active)`

### businesses
Companies that place delivery orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| business_code | VARCHAR(50) | UNIQUE, NOT NULL | Business code |
| name | VARCHAR(200) | NOT NULL | Business name |
| contact_person | VARCHAR(100) | | Contact person name |
| phone | VARCHAR(20) | NOT NULL | Phone number |
| email | VARCHAR(100) | | Email address |
| address | VARCHAR(500) | NOT NULL | Business address |
| address_description | TEXT | | Detailed address description |
| latitude | DECIMAL(10,8) | | GPS latitude |
| longitude | DECIMAL(11,8) | | GPS longitude |
| location_name | VARCHAR(200) | | Location name |
| business_type | VARCHAR(100) | | Type of business |
| payment_terms | ENUM | DEFAULT 'POSTPAID' | PREPAID, POSTPAID, CASH_ON_DELIVERY |
| credit_limit | DECIMAL(10,2) | DEFAULT 0.00 | Credit limit |
| is_active | BOOLEAN | DEFAULT TRUE | Business status |
| notes | TEXT | | Additional notes |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Indexes:**
- `idx_business_code (business_code)`
- `idx_name (name)`
- `idx_phone (phone)`
- `idx_business_type (business_type)`
- `idx_is_active (is_active)`
- `idx_location (latitude, longitude)`

### coordinators
Field coordinators who manage courier teams.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| system_user_id | BIGINT | UNIQUE, FK → system_users.id | Associated system user |
| name | VARCHAR(100) | NOT NULL | Coordinator name |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| phone | VARCHAR(20) | NOT NULL | Phone number |
| employee_id | VARCHAR(50) | UNIQUE | Employee identifier |
| work_type | ENUM | DEFAULT 'SHIFT' | JOKER, SHIFT |
| shift_start | TIME | | Shift start time |
| shift_end | TIME | | Shift end time |
| is_active | BOOLEAN | DEFAULT TRUE | Active status |
| status | ENUM | DEFAULT 'OFFLINE' | ONLINE, OFFLINE, BUSY, ON_BREAK, IN_FIELD |
| current_latitude | DECIMAL(10,8) | | Current GPS latitude |
| current_longitude | DECIMAL(11,8) | | Current GPS longitude |
| coverage_area | VARCHAR(500) | | Responsible coverage area |
| max_courier_count | INT | DEFAULT 10 | Maximum couriers managed |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

### couriers
Delivery personnel who execute orders.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| system_user_id | BIGINT | UNIQUE, FK → system_users.id | Associated system user |
| name | VARCHAR(100) | NOT NULL | Courier name |
| email | VARCHAR(100) | UNIQUE, NOT NULL | Email address |
| phone | VARCHAR(20) | NOT NULL | Phone number |
| license_number | VARCHAR(50) | | Driving license number |
| vehicle_type | ENUM | | BICYCLE, MOTORCYCLE, CAR, VAN, TRUCK, WALKING |
| work_type | ENUM | DEFAULT 'SHIFT' | JOKER, SHIFT |
| shift_start | TIME | | Shift start time |
| shift_end | TIME | | Shift end time |
| is_available | BOOLEAN | DEFAULT TRUE | Availability status |
| status | ENUM | DEFAULT 'OFFLINE' | ONLINE, OFFLINE, BUSY, ON_BREAK, IN_DELIVERY |
| current_latitude | DECIMAL(10,8) | | Current GPS latitude |
| current_longitude | DECIMAL(11,8) | | Current GPS longitude |
| current_location_name | VARCHAR(200) | | Current location name |
| total_deliveries | INT | DEFAULT 0 | Total completed deliveries |
| on_duty_since | TIMESTAMP WITH TIME ZONE | | Queue-based assignment key (set on check-in) |
| vehicle_id | BIGINT | FK → vehicles.id | Assigned vehicle |
| coordinator_id | BIGINT | FK → coordinators.id | Assigned coordinator |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Note:** `on_duty_since` is used for FIFO (First-In, First-Out) order assignment. Set when courier checks in, cleared on check-out.

### shifts
Courier shift reservations with check-in/check-out management.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| shift_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| courier_id | BIGINT | NOT NULL, FK → couriers.id | Assigned courier |
| start_time | TIMESTAMP WITH TIME ZONE | NOT NULL | Planned shift start time |
| end_time | TIMESTAMP WITH TIME ZONE | NOT NULL | Planned shift end time |
| shift_role | ENUM | NOT NULL, DEFAULT 'COURIER' | COURIER, CAPTAIN |
| status | ENUM | NOT NULL, DEFAULT 'RESERVED' | RESERVED, CHECKED_IN, CHECKED_OUT, CANCELLED, NO_SHOW |
| check_in_time | TIMESTAMP WITH TIME ZONE | | Actual check-in timestamp |
| check_out_time | TIMESTAMP WITH TIME ZONE | | Actual check-out timestamp |
| notes | TEXT | | Shift notes and comments |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Shift Status Enum:**
- `RESERVED`: Shift is reserved but courier hasn't checked in yet
- `CHECKED_IN`: Courier has checked in and is actively working
- `CHECKED_OUT`: Courier has checked out, shift completed
- `CANCELLED`: Reservation cancelled by courier (min 2 hours before start)
- `NO_SHOW`: Courier didn't check in for reserved shift

**Constraints:**
- Check-in time must be within 30 minutes before start_time
- Check-out time must be after check-in time
- Maximum shift duration: 24 hours
- No overlapping shifts for the same courier

**Indexes:**
- `idx_shifts_courier (courier_id)`
- `idx_shifts_status (status)`
- `idx_shifts_date_range (start_time, end_time)`
- `idx_shifts_courier_status (courier_id, status)`

### shift_templates
Reusable shift time templates for easy reservation.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| template_id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| name | VARCHAR(100) | NOT NULL | Template name (e.g., "Morning Shift") |
| description | TEXT | | Template description |
| start_time | TIME | NOT NULL | Start time of day |
| end_time | TIME | NOT NULL | End time of day |
| default_role | ENUM | DEFAULT 'COURIER' | Default role for this shift |
| max_couriers | INT | DEFAULT 10 | Maximum couriers per shift |
| is_active | BOOLEAN | DEFAULT TRUE | Template active status |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

**Default Templates:**
- Morning Shift: 09:00 - 17:00 (15 couriers)
- Evening Shift: 14:00 - 22:00 (12 couriers)
- Night Shift: 18:00 - 02:00 (8 couriers, restaurant deliveries)
- Full Day Shift: 08:00 - 20:00 (5 couriers, joker mode)
- Captain Morning: 08:00 - 16:00 (3 team leaders)

**Indexes:**
- `idx_shift_templates_active (is_active)`
- `idx_shift_templates_times (start_time, end_time)`

### orders
Delivery orders from businesses to end customers.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_number | VARCHAR(50) | UNIQUE, NOT NULL | Order reference number |
| status | ENUM | DEFAULT 'PENDING' | PENDING, ASSIGNED, PICKED_UP, IN_TRANSIT, DELIVERED, CANCELLED |
| priority | ENUM | DEFAULT 'NORMAL' | LOW, NORMAL, HIGH, URGENT |
| business_id | BIGINT | NOT NULL, FK → businesses.id | Ordering business |
| courier_id | BIGINT | FK → couriers.id | Assigned courier |
| coordinator_id | BIGINT | FK → coordinators.id | Supervising coordinator |
| end_customer_name | VARCHAR(100) | NOT NULL | Final recipient name |
| end_customer_phone | VARCHAR(20) | | Final recipient phone |
| pickup_address | TEXT | NOT NULL | Pickup location |
| pickup_address_description | TEXT | | Detailed pickup instructions |
| delivery_address | TEXT | NOT NULL | Delivery location |
| delivery_address_description | TEXT | | Detailed delivery instructions |
| package_description | VARCHAR(500) | NOT NULL | Package contents |
| package_weight | DECIMAL(8,2) | | Package weight (kg) |
| package_count | INT | DEFAULT 1 | Number of packages |
| package_value | DECIMAL(10,2) | | Package value for insurance |
| payment_type | ENUM | DEFAULT 'BUSINESS_ACCOUNT' | CASH, CARD, ONLINE, BUSINESS_ACCOUNT |
| delivery_fee | DECIMAL(10,2) | NOT NULL | Delivery charge |
| collection_amount | DECIMAL(10,2) | DEFAULT 0.00 | Amount to collect |
| courier_notes | TEXT | | Instructions for courier |
| business_notes | TEXT | | Notes from business |
| receipt_image_url | VARCHAR(500) | | Receipt/invoice image |
| delivery_proof_url | VARCHAR(500) | | Delivery confirmation image |
| order_date | DATETIME | DEFAULT CURRENT_TIMESTAMP | Order creation date |
| scheduled_pickup_time | DATETIME | | Planned pickup time |
| actual_pickup_time | DATETIME | | Actual pickup time |
| estimated_delivery_time | DATETIME | | Estimated delivery time |
| actual_delivery_time | DATETIME | | Actual delivery time |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

### vehicles
Courier vehicles and equipment.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| plate_number | VARCHAR(20) | UNIQUE, NOT NULL | License plate |
| vehicle_type | ENUM | NOT NULL | BICYCLE, MOTORCYCLE, CAR, VAN, TRUCK, WALKING |
| brand | VARCHAR(50) | | Vehicle brand |
| model | VARCHAR(50) | | Vehicle model |
| year | INT | | Manufacturing year |
| color | VARCHAR(30) | | Vehicle color |
| max_weight_capacity | DECIMAL(8,2) | | Maximum load capacity (kg) |
| is_active | BOOLEAN | DEFAULT TRUE | Vehicle status |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Creation time |
| updated_at | DATETIME | DEFAULT CURRENT_TIMESTAMP ON UPDATE | Last update time |

### order_tracking
Order status change history and tracking.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PRIMARY KEY, AUTO_INCREMENT | Unique identifier |
| order_id | BIGINT | NOT NULL, FK → orders.id | Associated order |
| status | ENUM | NOT NULL | Order status at this point |
| description | VARCHAR(500) | | Status change description |
| location | VARCHAR(200) | | Location name |
| latitude | DECIMAL(10,8) | | GPS latitude |
| longitude | DECIMAL(11,8) | | GPS longitude |
| created_at | DATETIME | DEFAULT CURRENT_TIMESTAMP | Timestamp |
| created_by | VARCHAR(100) | | Person who made the change |
| created_by_type | ENUM | DEFAULT 'SYSTEM' | SYSTEM, COURIER, COORDINATOR, ADMIN |

## Relationships

- **system_users** → **coordinators** (1:1)
- **system_users** → **couriers** (1:1)
- **coordinators** → **couriers** (1:N)
- **couriers** → **shifts** (1:N) - A courier can have multiple shifts
- **shift_templates** → **shifts** (logical reference) - Templates used to create shifts
- **businesses** → **orders** (1:N)
- **couriers** → **orders** (1:N)
- **vehicles** → **couriers** (1:1)
- **orders** → **order_tracking** (1:N)

## Shift Management System

### Shift Workflow

```
1. RESERVATION (RESERVED)
   ↓ Courier reserves a shift using a template for a future date
   
2. CHECK-IN (CHECKED_IN)
   ↓ Courier checks in when shift time arrives (up to 30 min early)
   ↓ System sets couriers.on_duty_since = CURRENT_TIMESTAMP
   ↓ Courier enters FIFO queue for order assignment
   
3. ACTIVE WORK
   ↓ Courier receives and delivers orders based on FIFO
   
4. CHECK-OUT (CHECKED_OUT)
   ↓ Courier checks out at shift end (can be early or late)
   ↓ System clears couriers.on_duty_since = NULL
   ↓ Courier exits FIFO queue
```

### Business Rules

**Reservation Rules:**
- Cannot reserve shifts for past dates
- No overlapping shifts for the same courier
- Can reserve multiple non-overlapping shifts per day
- Cancellation allowed until 2 hours before start

**Check-In Rules:**
- Can check in 30 minutes before scheduled start
- Only RESERVED shifts can be checked in
- Location data (lat/lon) optionally captured
- Automatically updates courier status to ONLINE

**Check-Out Rules:**
- Only CHECKED_IN shifts can be checked out
- Can check out anytime (early or late allowed)
- Automatically updates courier status to OFFLINE
- Clears on_duty_since for queue management

### FIFO Order Assignment

The system implements **First-In, First-Out** logic for fair order distribution:

```sql
-- Find next courier in queue
SELECT id, name, 
       EXTRACT(EPOCH FROM (CURRENT_TIMESTAMP - on_duty_since))/3600 as hours_working
FROM couriers
WHERE status = 'ONLINE' 
  AND on_duty_since IS NOT NULL
ORDER BY on_duty_since ASC
LIMIT 1;
```

**Queue Priority:**
- Couriers sorted by `on_duty_since` ascending (earliest first)
- Longest-working courier gets the next order
- Fair distribution across active couriers
- Automatic queue management via check-in/check-out

**Example:**
```
Courier A: on_duty_since = 08:00 (4 hours) → Priority 1
Courier B: on_duty_since = 09:30 (2.5 hours) → Priority 2
Courier C: on_duty_since = 10:15 (1.75 hours) → Priority 3

Next order → Assigned to Courier A
```

## Indexes and Performance

### Query Optimization
- **Location-based queries**: Spatial indexes on latitude/longitude columns
- **Status filtering**: Indexes on status and priority columns
- **Time-based queries**: Indexes on timestamp columns
- **Search functionality**: Indexes on name and email columns
- **Shift queries**: Composite indexes on (courier_id, status, start_time)
- **FIFO queue**: Index on (status, on_duty_since) for fast queue lookups

### Performance Considerations
- **Partitioning**: Consider partitioning orders table by date for large datasets
- **Archiving**: Move old orders and completed shifts to archive tables
- **Caching**: Use Redis for:
  - Active courier queue (on_duty_since ranking)
  - Frequently accessed order status
  - Courier locations
  - Available shift templates
- **Queue Optimization**: Maintain in-memory queue for hot path order assignment
