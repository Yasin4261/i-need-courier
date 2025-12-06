#!/bin/bash

# Business Order API Test Script

BASE_URL="http://localhost:8081"

echo "======================================"
echo "Business Order API Test"
echo "======================================"
echo ""

# 1. Login and get token
echo "1. Business Login..."
LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{"email":"yeni@pizza.com","password":"password123"}')

TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')

if [ "$TOKEN" == "null" ] || [ -z "$TOKEN" ]; then
    echo "❌ Login failed!"
    echo "Response: $LOGIN_RESPONSE"
    exit 1
fi

echo "✅ Login successful"
echo "Token: ${TOKEN:0:30}..."
echo ""

# 2. Create Order
echo "2. Creating Order..."
CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/api/v1/business/orders" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "pickupAddress": "Kadıköy Moda Caddesi No:123, Istanbul",
    "pickupContactPerson": "Ali Veli",
    "deliveryAddress": "Beşiktaş Barbaros Bulvarı No:45, Istanbul",
    "endCustomerName": "Ahmet Yılmaz",
    "endCustomerPhone": "+905551234567",
    "packageDescription": "2x Pizza Margherita",
    "packageWeight": 1.5,
    "packageCount": 2,
    "priority": "NORMAL",
    "paymentType": "CASH",
    "deliveryFee": 35.50,
    "collectionAmount": 0,
    "businessNotes": "Sıcak tutulmalı"
  }')

echo "$CREATE_RESPONSE" | jq .
ORDER_ID=$(echo $CREATE_RESPONSE | jq -r '.data.orderId')
echo ""

if [ "$ORDER_ID" == "null" ] || [ -z "$ORDER_ID" ]; then
    echo "❌ Order creation failed!"
    exit 1
fi

echo "✅ Order created successfully with ID: $ORDER_ID"
echo ""

# 3. Get All Orders
echo "3. Getting All Orders..."
curl -s -X GET "$BASE_URL/api/v1/business/orders" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 4. Get Order by ID
echo "4. Getting Order #$ORDER_ID..."
curl -s -X GET "$BASE_URL/api/v1/business/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 5. Update Order
echo "5. Updating Order #$ORDER_ID..."
curl -s -X PUT "$BASE_URL/api/v1/business/orders/$ORDER_ID" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{
    "packageDescription": "3x Pizza Margherita (UPDATED)",
    "businessNotes": "Urgent delivery!"
  }' | jq .
echo ""

# 6. Get Statistics
echo "6. Getting Order Statistics..."
curl -s -X GET "$BASE_URL/api/v1/business/orders/statistics" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 7. Cancel Order
echo "7. Cancelling Order #$ORDER_ID..."
curl -s -X POST "$BASE_URL/api/v1/business/orders/$ORDER_ID/cancel?reason=Test%20cancellation" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

# 8. Try to delete cancelled order (should fail)
echo "8. Trying to delete cancelled order (should fail)..."
curl -s -X DELETE "$BASE_URL/api/v1/business/orders/$ORDER_ID" \
  -H "Authorization: Bearer $TOKEN" | jq .
echo ""

echo "======================================"
echo "✅ All tests completed!"
echo "======================================"

