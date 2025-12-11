#!/bin/bash

echo "🛠️  Manual Flyway Repair for Migration V13"

cd /home/yasin/Desktop/repos/i-need-courier

echo "Option 1: Quick repair with Docker restart"
echo "=========================================="
echo "docker-compose down"
echo "docker volume rm i-need-courier_postgres_data"
echo "docker-compose up --build"
echo ""

echo "Option 2: Connect to PostgreSQL and manual repair"
echo "================================================"
echo "docker-compose exec postgres psql -U courier_user -d courier_db"
echo "DELETE FROM flyway_schema_history WHERE version = '13';"
echo "\\q"
echo "docker-compose restart courier-backend"
echo ""

echo "Option 3: Run repair via Maven (if PostgreSQL is running)"
echo "======================================================="
echo "./mvnw flyway:repair -Dspring.profiles.active=docker"
echo "./mvnw flyway:migrate -Dspring.profiles.active=docker"
echo ""

echo "💡 Choose the appropriate method based on your needs!"
echo "   - Option 1: Clean database restart (recommended for development)"
echo "   - Option 2: Preserve data, only fix migration history"
echo "   - Option 3: Maven-based repair"
