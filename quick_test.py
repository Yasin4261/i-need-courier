#!/usr/bin/env python3
"""Quick enum fix test"""
import requests
import sys
import time

BASE_URL = "http://localhost:8081"

print("🔍 Enum Fix Test\n")

# Wait for backend
print("⏳ Waiting for backend...")
for i in range(15):
    try:
        r = requests.get(f"{BASE_URL}/actuator/health", timeout=2)
        if r.status_code == 200:
            print("✅ Backend is UP!\n")
            break
    except:
        pass
    time.sleep(2)
    print(f"   Attempt {i+1}/15...")
else:
    print("❌ Backend not responding!")
    sys.exit(1)

# Login
print("🔐 Login...")
try:
    r = requests.post(
        f"{BASE_URL}/api/v1/auth/login",
        json={"email": "yeni@pizza.com", "password": "password123"},
        timeout=5
    )
    token = r.json()['data']['token']
    print(f"✅ Token: {token[:30]}...\n")
except Exception as e:
    print(f"❌ Login failed: {e}")
    sys.exit(1)

# Test Order Create (ENUM TEST!)
print("📦 Creating Order (Testing ENUM types)...")
print("   payment_type=CASH, priority=NORMAL, status=PENDING\n")

try:
    r = requests.post(
        f"{BASE_URL}/api/v1/business/orders",
        headers={"Authorization": f"Bearer {token}"},
        json={
            "pickupAddress": "Kadıköy Test, Istanbul",
            "deliveryAddress": "Beşiktaş Test, Istanbul",
            "endCustomerName": "Enum Test User",
            "endCustomerPhone": "+905551234567",
            "priority": "NORMAL",
            "paymentType": "CASH",
            "deliveryFee": 30.00
        },
        timeout=10
    )

    response = r.json()

    if r.status_code == 201 and response.get('success'):
        print("✅✅✅ ENUM FIX ÇALIŞIYOR! ✅✅✅\n")
        print(f"Order ID: {response['data']['orderId']}")
        print(f"Order Number: {response['data']['orderNumber']}")
        print(f"Status: {response['data']['status']}")
        print(f"Priority: {response['data']['priority']}")
        print(f"Payment Type: {response['data']['paymentType']}")
        print("\n🎉 TEST BAŞARILI!")
        sys.exit(0)
    else:
        print(f"❌ Request failed: {r.status_code}")
        print(f"Response: {response}")

        if "payment_type" in str(response).lower():
            print("\n❌ ENUM HATASI DEVAM EDİYOR!")

        sys.exit(1)

except Exception as e:
    print(f"❌ Error: {e}")
    sys.exit(1)
build -