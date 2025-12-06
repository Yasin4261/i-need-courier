#!/bin/bash

# Enum Fix Test Script
# Bu script fix'in çalışıp çalışmadığını test eder

echo "🔍 PostgreSQL Enum Type Fix Test"
echo "=================================="
echo ""

# Colors
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m'

BASE_URL="http://localhost:8081"

# 1. Health Check
echo -e "${YELLOW}1️⃣ Backend Health Check...${NC}"
HEALTH=$(curl -s "${BASE_URL}/actuator/health" | grep -o '"status":"[^"]*"' | cut -d'"' -f4)

if [ "$HEALTH" == "UP" ]; then
    echo -e "${GREEN}✅ Backend çalışıyor${NC}\n"
else
    echo -e "${RED}❌ Backend çalışmıyor!${NC}"
    echo "Loglara bak: docker logs courier-backend"
    exit 1
fi

# 2. Login
echo -e "${YELLOW}2️⃣ Business Login...${NC}"
LOGIN_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/auth/login" \
    -H "Content-Type: application/json" \
    -d '{"email":"yeni@pizza.com","password":"password123"}')

TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*"' | cut -d'"' -f4)

if [ -n "$TOKEN" ]; then
    echo -e "${GREEN}✅ Login başarılı${NC}"
    echo "Token: ${TOKEN:0:30}..."
    echo ""
else
    echo -e "${RED}❌ Login başarısız!${NC}"
    echo "$LOGIN_RESPONSE"
    exit 1
fi

# 3. Order Create Test (ENUM TEST!)
echo -e "${YELLOW}3️⃣ Order Create (Enum Type Test)...${NC}"
echo "Testing: payment_type=CASH, priority=NORMAL, status=PENDING"
echo ""

ORDER_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/business/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{
        "pickupAddress": "Test Pickup Address, Istanbul",
        "deliveryAddress": "Test Delivery Address, Istanbul",
        "endCustomerName": "Enum Test User",
        "endCustomerPhone": "+905551234567",
        "priority": "NORMAL",
        "paymentType": "CASH",
        "deliveryFee": 30.00
    }')

# Check for success
if echo "$ORDER_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅✅✅ ENUM FIX ÇALIŞIYOR! ✅✅✅${NC}"
    echo ""
    echo "Order Details:"
    echo "$ORDER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_RESPONSE"
    echo ""

    ORDER_ID=$(echo "$ORDER_RESPONSE" | grep -o '"orderId":[0-9]*' | grep -o '[0-9]*')
    echo -e "${GREEN}Order ID: $ORDER_ID${NC}"

elif echo "$ORDER_RESPONSE" | grep -q "payment_type"; then
    echo -e "${RED}❌ ENUM HATASI DEVAM EDİYOR!${NC}"
    echo ""
    echo "Hata Detayı:"
    echo "$ORDER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_RESPONSE"
    echo ""
    exit 1

else
    echo -e "${RED}❌ Farklı bir hata oluştu!${NC}"
    echo ""
    echo "Response:"
    echo "$ORDER_RESPONSE" | python3 -m json.tool 2>/dev/null || echo "$ORDER_RESPONSE"
    echo ""
    exit 1
fi

# 4. Different Enum Values Test
echo -e "${YELLOW}4️⃣ Testing Different Enum Values...${NC}"
echo ""

# Test URGENT priority
echo "Testing: priority=URGENT, paymentType=CREDIT_CARD"
URGENT_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/business/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{
        "pickupAddress": "Test Address",
        "deliveryAddress": "Test Address",
        "endCustomerName": "Urgent Test",
        "endCustomerPhone": "+905559999999",
        "priority": "URGENT",
        "paymentType": "CREDIT_CARD",
        "deliveryFee": 50.00
    }')

if echo "$URGENT_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ URGENT priority çalışıyor${NC}"
else
    echo -e "${RED}❌ URGENT priority hatası${NC}"
fi

# Test CASH_ON_DELIVERY
echo "Testing: paymentType=CASH_ON_DELIVERY"
COD_RESPONSE=$(curl -s -X POST "${BASE_URL}/api/v1/business/orders" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer ${TOKEN}" \
    -d '{
        "pickupAddress": "Test Address",
        "deliveryAddress": "Test Address",
        "endCustomerName": "COD Test",
        "endCustomerPhone": "+905558888888",
        "priority": "NORMAL",
        "paymentType": "CASH_ON_DELIVERY",
        "deliveryFee": 25.00,
        "collectionAmount": 100.00
    }')

if echo "$COD_RESPONSE" | grep -q '"success":true'; then
    echo -e "${GREEN}✅ CASH_ON_DELIVERY çalışıyor${NC}"
else
    echo -e "${RED}❌ CASH_ON_DELIVERY hatası${NC}"
fi

echo ""

# 5. Database Check
echo -e "${YELLOW}5️⃣ Database Record Check...${NC}"
docker exec courier-postgres psql -U courier_user -d courier_db -c \
    "SELECT order_number, status, priority, payment_type FROM orders ORDER BY id DESC LIMIT 3;" \
    2>/dev/null || echo "Database check skipped"

echo ""
echo -e "${GREEN}=================================="
echo "✅ ENUM FIX TEST TAMAMLANDI!"
echo -e "==================================${NC}"

