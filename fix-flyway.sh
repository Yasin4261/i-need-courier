#!/bin/bash

echo "🔧 Fixing Flyway Migration Checksum Issue..."

# Stop containers if running
echo "1. Stopping containers..."
docker-compose down

# Remove only the app container to force rebuild
echo "2. Removing app container..."
docker-compose rm -f courier-backend

# Clean PostgreSQL data to start fresh
echo "3. Cleaning PostgreSQL data..."
docker volume rm i-need-courier_postgres_data 2>/dev/null || true

# Start only PostgreSQL first
echo "4. Starting PostgreSQL..."
docker-compose up -d postgres

echo "5. Waiting for PostgreSQL to be ready..."
sleep 10

# Run Flyway repair through Maven
echo "6. Running Flyway repair..."
./mvnw flyway:repair -Dspring.profiles.active=docker

# Or alternatively, start the app which will auto-migrate
echo "7. Starting the application..."
docker-compose up --build -d

echo "✅ Migration issue should be resolved!"
echo "Check logs with: docker-compose logs courier-backend"
