#!/bin/bash
# Backfill script for on_duty_couriers table
# This script populates on_duty_couriers from existing couriers.on_duty_since and shifts data

set -e

echo "🔄 Starting on_duty_couriers backfill..."

# Database connection details
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5433}"
DB_NAME="${DB_NAME:-courier_db}"
DB_USER="${DB_USER:-courier_user}"

# Check if postgres is accessible
echo "📡 Checking database connection..."
if ! pg_isready -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" > /dev/null 2>&1; then
    echo "❌ Database not accessible at $DB_HOST:$DB_PORT"
    echo "💡 If using Docker: docker compose up -d postgres"
    exit 1
fi

echo "✅ Database connection OK"

# Dry-run: show what will be backfilled
echo ""
echo "📊 Dry-run: Checking current on-duty couriers..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
SELECT c.id, c.name, c.on_duty_since, s.shift_id, s.status, s.check_in_time
FROM couriers c
LEFT JOIN shifts s ON s.courier_id = c.id AND s.status = 'CHECKED_IN'
WHERE c.on_duty_since IS NOT NULL OR s.status = 'CHECKED_IN'
ORDER BY c.on_duty_since ASC NULLS LAST;"

echo ""
read -p "⚠️  Proceed with backfill? (y/N): " confirm
if [[ ! "$confirm" =~ ^[Yy]$ ]]; then
    echo "❌ Backfill cancelled"
    exit 0
fi

# Execute backfill
echo ""
echo "🚀 Executing backfill..."
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" <<'SQL'
INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source, created_at, updated_at)
SELECT
    c.id,
    s.shift_id,
    COALESCE(
        c.on_duty_since AT TIME ZONE 'UTC',
        s.check_in_time AT TIME ZONE 'UTC',
        now()
    ) AS on_duty_since,
    'backfill' AS source,
    now() AS created_at,
    now() AS updated_at
FROM couriers c
LEFT JOIN shifts s ON s.courier_id = c.id AND s.status = 'CHECKED_IN'
WHERE c.on_duty_since IS NOT NULL OR s.status = 'CHECKED_IN'
ON CONFLICT (courier_id) DO UPDATE
  SET on_duty_since = EXCLUDED.on_duty_since,
      shift_id = EXCLUDED.shift_id,
      updated_at = now();
SQL

echo "✅ Backfill completed successfully!"

# Show results
echo ""
echo "📊 Current on_duty_couriers table:"
psql -h "$DB_HOST" -p "$DB_PORT" -U "$DB_USER" -d "$DB_NAME" -c "
SELECT od.id, od.courier_id, c.name, od.on_duty_since, od.shift_id, od.source
FROM on_duty_couriers od
JOIN couriers c ON c.id = od.courier_id
ORDER BY od.on_duty_since ASC;"

echo ""
echo "✅ Done!"

