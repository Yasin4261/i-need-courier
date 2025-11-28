#!/bin/bash

# Flyway Migration Management Scripts
# Güvenli migration işlemleri için komutlar

set -e

DB_URL="jdbc:postgresql://localhost:5433/courier_db"
DB_USER="courier_user"
DB_PASSWORD="courier_password"

function show_help() {
    echo "🗄️  Flyway Migration Management"
    echo ""
    echo "Usage: ./migration.sh [command]"
    echo ""
    echo "Commands:"
    echo "  info      - Show migration status"
    echo "  validate  - Validate migrations"
    echo "  migrate   - Run migrations"
    echo "  repair    - Repair migration history"
    echo "  baseline  - Create baseline for existing DB"
    echo "  clean     - Clean database (DANGER - only for development!)"
    echo "  backup    - Create database backup"
    echo "  restore   - Restore from backup"
    echo ""
    echo "Examples:"
    echo "  ./migration.sh info"
    echo "  ./migration.sh validate"
    echo "  ./migration.sh migrate"
}

function check_postgres() {
    echo "🔍 Checking PostgreSQL connection..."
    if ! pg_isready -h localhost -p 5433 -U $DB_USER >/dev/null 2>&1; then
        echo "❌ PostgreSQL not available. Starting with Docker..."
        docker compose up -d postgres
        echo "⏳ Waiting for PostgreSQL..."
        sleep 10
    fi
    echo "✅ PostgreSQL is ready"
}

function migration_info() {
    echo "📊 Migration Status:"
    ./mvnw flyway:info -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
}

function migration_validate() {
    echo "✅ Validating migrations..."
    ./mvnw flyway:validate -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
    echo "✅ Validation completed successfully!"
}

function migration_migrate() {
    echo "🚀 Running migrations..."
    ./mvnw flyway:migrate -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
    echo "✅ Migrations completed successfully!"
}

function migration_repair() {
    echo "🔧 Repairing migration history..."
    read -p "Are you sure you want to repair the migration history? (y/N): " confirm
    if [[ $confirm =~ ^[Yy]$ ]]; then
        ./mvnw flyway:repair -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
        echo "✅ Repair completed!"
    else
        echo "❌ Repair cancelled"
    fi
}

function migration_baseline() {
    echo "📍 Creating baseline..."
    ./mvnw flyway:baseline -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
    echo "✅ Baseline created!"
}

function migration_clean() {
    echo "⚠️  WARNING: This will DELETE ALL DATA in the database!"
    echo "This should ONLY be used in development environment!"
    read -p "Type 'DELETE ALL DATA' to confirm: " confirm
    if [[ $confirm == "DELETE ALL DATA" ]]; then
        ./mvnw flyway:clean -Dflyway.url=$DB_URL -Dflyway.user=$DB_USER -Dflyway.password=$DB_PASSWORD
        echo "🗑️  Database cleaned!"
    else
        echo "❌ Clean cancelled"
    fi
}

function backup_database() {
    echo "💾 Creating database backup..."
    BACKUP_FILE="backup_$(date +%Y%m%d_%H%M%S).sql"
    pg_dump -h localhost -p 5433 -U $DB_USER -d courier_db > "backups/$BACKUP_FILE"
    echo "✅ Backup created: backups/$BACKUP_FILE"
}

function restore_database() {
    echo "📂 Available backups:"
    ls -la backups/*.sql 2>/dev/null || echo "No backups found in backups/ directory"
    echo ""
    read -p "Enter backup filename to restore: " backup_file
    if [[ -f "backups/$backup_file" ]]; then
        echo "⚠️  WARNING: This will OVERWRITE current database!"
        read -p "Are you sure? (y/N): " confirm
        if [[ $confirm =~ ^[Yy]$ ]]; then
            psql -h localhost -p 5433 -U $DB_USER -d courier_db < "backups/$backup_file"
            echo "✅ Database restored from $backup_file"
        fi
    else
        echo "❌ Backup file not found"
    fi
}

# Create backups directory
mkdir -p backups

# Main script logic
case "$1" in
    "info")
        check_postgres
        migration_info
        ;;
    "validate")
        check_postgres
        migration_validate
        ;;
    "migrate")
        check_postgres
        migration_migrate
        ;;
    "repair")
        check_postgres
        migration_repair
        ;;
    "baseline")
        check_postgres
        migration_baseline
        ;;
    "clean")
        check_postgres
        migration_clean
        ;;
    "backup")
        check_postgres
        backup_database
        ;;
    "restore")
        check_postgres
        restore_database
        ;;
    *)
        show_help
        ;;
esac
