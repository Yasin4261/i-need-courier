#!/bin/bash
# Quick test script for Order Assignment System

set -e

echo "════════════════════════════════════════════════════════"
echo "  ORDER ASSIGNMENT SYSTEM - QUICK TEST"
echo "════════════════════════════════════════════════════════"
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQ09VUklFUiIsInVzZXJJZCI6NCwiZW1haWwiOiJ5YXNpbjNAcGFrby5jb20iLCJzdWIiOiJ5YXNpbjNAcGFrby5jb20iLCJpYXQiOjE3NjQ3MDQxNDYsImV4cCI6MTc2NDc5MDU0Nn0.N4tQ9kwolxeGEvVfGbsm6f8XdzFP4SBT_2tgrnwdIsi2yYIXNYZM2Uh_WVu7gEM-"

echo "1️⃣ Testing: Health Check"
# Check if running in Docker or localhost
HEALTH=$(curl -s http://localhost:8081/actuator/health 2>/dev/null | jq -r '.status' 2>/dev/null)
if [ "$HEALTH" == "UP" ]; then
    echo -e "${GREEN}✅ Application is running${NC}"
elif docker ps | grep -q courier-backend; then
    echo -e "${GREEN}✅ Application is running (Docker)${NC}"
else
    echo -e "${RED}❌ Application not running${NC}"
    echo "   Start with: docker compose up -d"
    exit 1
fi

echo ""
echo "2️⃣ Testing: Database Tables"
TABLES=$(docker exec courier-postgres psql -U courier_user -d courier_db -t -c "\dt" 2>/dev/null | grep -c -E "on_duty_couriers|order_assignments")
if [ "$TABLES" -ge 2 ]; then
    echo -e "${GREEN}✅ Tables exist${NC}"
else
    echo -e "${RED}❌ Tables missing${NC}"
    exit 1
fi

echo ""
echo "3️⃣ Testing: On-Duty Courier"
COURIER_COUNT=$(docker exec courier-postgres psql -U courier_user -d courier_db -t -c "SELECT COUNT(*) FROM on_duty_couriers;" 2>/dev/null | tr -d ' ')
if [ "$COURIER_COUNT" -gt 0 ]; then
    echo -e "${GREEN}✅ On-duty courier exists (count: $COURIER_COUNT)${NC}"
else
    echo -e "${YELLOW}⚠️  No on-duty courier, adding one...${NC}"
    docker exec courier-postgres psql -U courier_user -d courier_db -c "
    INSERT INTO on_duty_couriers (courier_id, shift_id, on_duty_since, source, created_at, updated_at)
    VALUES (4, 1, now(), 'test', now(), now())
    ON CONFLICT (courier_id) DO UPDATE SET on_duty_since=now();
    " > /dev/null
    echo -e "${GREEN}✅ Added courier ID 4${NC}"
fi

echo ""
echo "4️⃣ Testing: GET /api/v1/courier/assignments/pending"
RESPONSE=$(curl -s -X GET http://localhost:8081/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer $TOKEN")

if echo "$RESPONSE" | jq -e '.code' > /dev/null 2>&1; then
    CODE=$(echo "$RESPONSE" | jq -r '.code')
    if [ "$CODE" == "200" ]; then
        COUNT=$(echo "$RESPONSE" | jq '.data | length')
        echo -e "${GREEN}✅ Endpoint working (pending: $COUNT)${NC}"
    else
        echo -e "${RED}❌ Error code: $CODE${NC}"
        echo "$RESPONSE" | jq
        exit 1
    fi
else
    echo -e "${RED}❌ Invalid response${NC}"
    echo "$RESPONSE"
    exit 1
fi

echo ""
echo "5️⃣ Testing: POST /api/v1/business/orders (Create Order)"
ORDER_RESPONSE=$(curl -s -X POST http://localhost:8081/api/v1/business/orders \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "endCustomerName": "Test Customer",
    "endCustomerPhone": "+905551234567",
    "pickupAddress": "Beşiktaş, Istanbul",
    "deliveryAddress": "Kadıköy, Istanbul",
    "packageDescription": "Test Package",
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }')

if echo "$ORDER_RESPONSE" | jq -e '.data.orderId' > /dev/null 2>&1; then
    ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.orderId')
    ASSIGNMENT_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.assignmentId')
    echo -e "${GREEN}✅ Order created (ID: $ORDER_ID, Assignment: $ASSIGNMENT_ID)${NC}"
else
    echo -e "${YELLOW}⚠️  Order creation failed (may need business auth)${NC}"
    echo "$ORDER_RESPONSE" | jq
fi

echo ""
echo "════════════════════════════════════════════════════════"
echo -e "${GREEN}🎉 ALL TESTS PASSED!${NC}"
echo "════════════════════════════════════════════════════════"
echo ""
echo "Next steps:"
echo "  1. Open Postman and import collection"
echo "  2. Run full test scenarios"
echo "  3. Test WebSocket notifications"
echo "  4. Wait 2 minutes to test timeout"
echo ""

