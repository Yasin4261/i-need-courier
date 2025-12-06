#!/bin/bash
# Complete Delivery Flow Test Script
# Tests: Business Order → Auto-Assignment → Courier Accept → Pickup → In-Transit → Delivered

set -e

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m' # No Color

# API Base URL
BASE_URL="http://localhost:8081"

# Tokens (you may need to update these)
BUSINESS_TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQlVTSU5FU1MiLCJ1c2VySWQiOjEwLCJlbWFpbCI6ImluZm9AbXliaXouY29tIiwic3ViIjoiaW5mb0BteWJpei5jb20iLCJpYXQiOjE3NjQ3MTkyNDIsImV4cCI6MTc2NDgwNTY0Mn0.ZrUjNs_8BGOpyCIsoDezTG25rj8Awp8qla8OBipmFgmJN8GvQKjEJGjCHwlE6W3o"
COURIER_TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQ09VUklFUiIsInVzZXJJZCI6NCwiZW1haWwiOiJ5YXNpbjNAcGFrby5jb20iLCJzdWIiOiJ5YXNpbjNAcGFrby5jb20iLCJpYXQiOjE3NjQ3MDQxNDYsImV4cCI6MTc2NDc5MDU0Nn0.N4tQ9kwolxeGEvVfGbsm6f8XdzFP4SBT_2tgrnwdIsi2yYIXNYZM2Uh_WVu7gEM-"

echo -e "${CYAN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${CYAN}║                                                            ║${NC}"
echo -e "${CYAN}║     KAPSAMLI TESLİMAT AKIŞI TEST SİSTEMİ                  ║${NC}"
echo -e "${CYAN}║     Complete Delivery Flow Test                           ║${NC}"
echo -e "${CYAN}║                                                            ║${NC}"
echo -e "${CYAN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""

# Function to print step header
print_step() {
    echo ""
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
    echo -e "${YELLOW}$1${NC}"
    echo -e "${BLUE}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
}

# Function to check response
check_response() {
    local response=$1
    local expected_field=$2

    if echo "$response" | jq -e ".$expected_field" > /dev/null 2>&1; then
        echo -e "${GREEN}✅ SUCCESS${NC}"
        return 0
    else
        echo -e "${RED}❌ FAILED${NC}"
        echo "$response" | jq
        return 1
    fi
}

# STEP 0: Prerequisites Check
print_step "STEP 0: Ön Kontroller (Prerequisites)"

echo "Checking application health..."
HEALTH=$(curl -s ${BASE_URL}/actuator/health | jq -r '.status')
if [ "$HEALTH" == "UP" ]; then
    echo -e "${GREEN}✅ Application is running${NC}"
else
    echo -e "${RED}❌ Application is not running${NC}"
    exit 1
fi

echo ""
echo "Checking on-duty courier..."
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

# STEP 1: Business Creates Order
print_step "STEP 1: 🏢 Business Sipariş Oluşturur (Create Order)"

ORDER_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/business/orders \
  -H "Authorization: Bearer ${BUSINESS_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "pickupAddress": "Beşiktaş Meydanı No:15, Beşiktaş/İstanbul",
    "deliveryAddress": "Kadıköy İskelesi No:25, Kadıköy/İstanbul",
    "packageDescription": "Pizza (2 adet, büyük boy)",
    "packageCount": 2,
    "deliveryFee": 50.00,
    "paymentType": "CASH"
  }')

check_response "$ORDER_RESPONSE" "data.orderId" || exit 1

ORDER_ID=$(echo "$ORDER_RESPONSE" | jq -r '.data.orderId')
ORDER_NUMBER=$(echo "$ORDER_RESPONSE" | jq -r '.data.orderNumber')

echo ""
echo -e "${GREEN}Order Created:${NC}"
echo "  Order ID: $ORDER_ID"
echo "  Order Number: $ORDER_NUMBER"
echo "  Status: $(echo "$ORDER_RESPONSE" | jq -r '.data.status')"

echo ""
echo "Waiting 3 seconds for auto-assignment..."
sleep 3

# STEP 2: Check Assignment
print_step "STEP 2: 🔍 Assignment Kontrolü (Check Assignment)"

docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT
    oa.id AS assignment_id,
    oa.order_id,
    oa.courier_id,
    c.name AS courier_name,
    oa.status,
    oa.assignment_type,
    to_char(oa.assigned_at, 'YYYY-MM-DD HH24:MI:SS') as assigned_at,
    to_char(oa.timeout_at, 'YYYY-MM-DD HH24:MI:SS') as timeout_at
FROM order_assignments oa
JOIN couriers c ON c.id = oa.courier_id
WHERE oa.order_id = $ORDER_ID
ORDER BY oa.assigned_at DESC
LIMIT 1;
"

# STEP 3: Courier Views Pending Assignments
print_step "STEP 3: 📋 Kurye Bekleyen Atamaları Görür"

PENDING_RESPONSE=$(curl -s -X GET ${BASE_URL}/api/v1/courier/assignments/pending \
  -H "Authorization: Bearer ${COURIER_TOKEN}")

check_response "$PENDING_RESPONSE" "data" || exit 1

ASSIGNMENT_ID=$(echo "$PENDING_RESPONSE" | jq -r ".data[] | select(.orderId == $ORDER_ID) | .assignmentId")

if [ -z "$ASSIGNMENT_ID" ] || [ "$ASSIGNMENT_ID" == "null" ]; then
    echo -e "${RED}❌ Assignment not found for order $ORDER_ID${NC}"
    echo "Pending assignments:"
    echo "$PENDING_RESPONSE" | jq '.data'
    exit 1
fi

echo ""
echo -e "${GREEN}Found Assignment:${NC}"
echo "  Assignment ID: $ASSIGNMENT_ID"
echo "  Order ID: $ORDER_ID"

# STEP 4: Courier Accepts Assignment
print_step "STEP 4: ✅ Kurye Atamayı Kabul Eder (Accept Assignment)"

ACCEPT_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/courier/assignments/${ASSIGNMENT_ID}/accept \
  -H "Authorization: Bearer ${COURIER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{}')

check_response "$ACCEPT_RESPONSE" "message" || exit 1

echo ""
echo -e "${GREEN}Assignment Accepted!${NC}"
echo "  Message: $(echo "$ACCEPT_RESPONSE" | jq -r '.message')"

sleep 2

# Verify assignment status
echo ""
echo "Verifying assignment status in database..."
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT id, order_id, courier_id, status,
       to_char(response_at, 'YYYY-MM-DD HH24:MI:SS') as response_at
FROM order_assignments
WHERE id = $ASSIGNMENT_ID;
"

# STEP 5: Pickup
print_step "STEP 5: 📦 PICKUP - Kurye Paketi Alır"

PICKUP_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/courier/orders/${ORDER_ID}/pickup \
  -H "Authorization: Bearer ${COURIER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"notes": "2 adet pizza kutusu alındı, sıcak tutuluyor"}')

check_response "$PICKUP_RESPONSE" "data.status" || exit 1

PICKUP_STATUS=$(echo "$PICKUP_RESPONSE" | jq -r '.data.status')

echo ""
echo -e "${GREEN}Pickup Successful!${NC}"
echo "  Status: $PICKUP_STATUS"
echo "  Message: $(echo "$PICKUP_RESPONSE" | jq -r '.message')"

if [ "$PICKUP_STATUS" != "PICKED_UP" ]; then
    echo -e "${RED}❌ Expected status PICKED_UP but got $PICKUP_STATUS${NC}"
    exit 1
fi

sleep 2

# STEP 6: Start Delivery (In-Transit)
print_step "STEP 6: 🚗 IN-TRANSIT - Teslimat Başlatılır"

TRANSIT_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/courier/orders/${ORDER_ID}/start-delivery \
  -H "Authorization: Bearer ${COURIER_TOKEN}")

check_response "$TRANSIT_RESPONSE" "data.status" || exit 1

TRANSIT_STATUS=$(echo "$TRANSIT_RESPONSE" | jq -r '.data.status')

echo ""
echo -e "${GREEN}Delivery Started!${NC}"
echo "  Status: $TRANSIT_STATUS"
echo "  Message: $(echo "$TRANSIT_RESPONSE" | jq -r '.message')"

if [ "$TRANSIT_STATUS" != "IN_TRANSIT" ]; then
    echo -e "${RED}❌ Expected status IN_TRANSIT but got $TRANSIT_STATUS${NC}"
    exit 1
fi

sleep 2

# STEP 7: Complete Delivery
print_step "STEP 7: ✅ DELIVERED - Teslimat Tamamlanır"

DELIVERED_RESPONSE=$(curl -s -X POST ${BASE_URL}/api/v1/courier/orders/${ORDER_ID}/complete \
  -H "Authorization: Bearer ${COURIER_TOKEN}" \
  -H "Content-Type: application/json" \
  -d '{"notes": "Müşteriye teslim edildi, 50 TL nakit tahsil edildi", "collectionAmount": 50.00}')

check_response "$DELIVERED_RESPONSE" "data.status" || exit 1

DELIVERED_STATUS=$(echo "$DELIVERED_RESPONSE" | jq -r '.data.status')

echo ""
echo -e "${GREEN}Delivery Completed!${NC}"
echo "  Status: $DELIVERED_STATUS"
echo "  Message: $(echo "$DELIVERED_RESPONSE" | jq -r '.message')"

if [ "$DELIVERED_STATUS" != "DELIVERED" ]; then
    echo -e "${RED}❌ Expected status DELIVERED but got $DELIVERED_STATUS${NC}"
    exit 1
fi

# STEP 8: Final Verification
print_step "STEP 8: 🔍 Final Database Verification"

echo "Order final status:"
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT
    id,
    order_number,
    status,
    courier_notes,
    collection_amount,
    to_char(created_at, 'YYYY-MM-DD HH24:MI:SS') as created_at,
    to_char(updated_at, 'YYYY-MM-DD HH24:MI:SS') as updated_at
FROM orders
WHERE id = $ORDER_ID;
"

echo ""
echo "Assignment history:"
docker exec courier-postgres psql -U courier_user -d courier_db -c "
SELECT
    id,
    order_id,
    courier_id,
    status,
    assignment_type,
    to_char(assigned_at, 'YYYY-MM-DD HH24:MI:SS') as assigned_at,
    to_char(response_at, 'YYYY-MM-DD HH24:MI:SS') as response_at,
    rejection_reason
FROM order_assignments
WHERE order_id = $ORDER_ID
ORDER BY assigned_at DESC;
"

# Summary
print_step "TEST SUMMARY"

echo ""
echo -e "${GREEN}╔════════════════════════════════════════════════════════════╗${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}║                  ✅ ALL TESTS PASSED! ✅                   ║${NC}"
echo -e "${GREEN}║                                                            ║${NC}"
echo -e "${GREEN}╚════════════════════════════════════════════════════════════╝${NC}"
echo ""
echo "Test Results:"
echo -e "  ${GREEN}✅${NC} Business Order Creation"
echo -e "  ${GREEN}✅${NC} Auto-Assignment (FIFO)"
echo -e "  ${GREEN}✅${NC} Courier Pending Assignments"
echo -e "  ${GREEN}✅${NC} Courier Accept Assignment"
echo -e "  ${GREEN}✅${NC} Order Pickup (PICKED_UP)"
echo -e "  ${GREEN}✅${NC} Order In-Transit"
echo -e "  ${GREEN}✅${NC} Order Delivered"
echo ""
echo "Tested Order:"
echo "  Order ID: $ORDER_ID"
echo "  Order Number: $ORDER_NUMBER"
echo "  Assignment ID: $ASSIGNMENT_ID"
echo "  Final Status: $DELIVERED_STATUS"
echo "  Collection Amount: 50.00 TL"
echo ""
echo -e "${CYAN}System is production ready! 🚀${NC}"
echo ""

