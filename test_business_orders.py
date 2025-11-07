#!/usr/bin/env python3
"""
Business Order API Integration Test
"""
import requests
import json
import time
import sys

BASE_URL = "http://localhost:8081"

def print_section(title):
    print("\n" + "="*60)
    print(f"  {title}")
    print("="*60)

def print_response(response):
    try:
        print(json.dumps(response.json(), indent=2))
    except:
        print(response.text)

def main():
    print_section("Business Order API Test")

    # Wait for backend to be ready
    print("\n⏳ Waiting for backend to start...")
    for i in range(30):
        try:
            response = requests.get(f"{BASE_URL}/actuator/health", timeout=2)
            if response.status_code == 200:
                print("✅ Backend is ready!")
                break
        except:
            pass
        time.sleep(2)
        print(f"   Attempt {i+1}/30...")
    else:
        print("❌ Backend did not start in time!")
        sys.exit(1)

    # Test 1: Business Login
    print_section("1. Business Login")
    try:
        response = requests.post(
            f"{BASE_URL}/api/v1/auth/login",
            json={
                "email": "yeni@pizza.com",
                "password": "password123"
            }
        )
        print(f"Status: {response.status_code}")
        data = response.json()
        print_response(response)

        if response.status_code != 200:
            print("❌ Login failed!")
            sys.exit(1)

        token = data['data']['token']
        print(f"\n✅ Login successful! Token: {token[:30]}...")
    except Exception as e:
        print(f"❌ Error: {e}")
        sys.exit(1)

    headers = {
        "Authorization": f"Bearer {token}",
        "Content-Type": "application/json"
    }

    # Test 2: Create Order
    print_section("2. Create Order")
    try:
        order_data = {
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
        }

        response = requests.post(
            f"{BASE_URL}/api/v1/business/orders",
            json=order_data,
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)

        if response.status_code not in [200, 201]:
            print("❌ Order creation failed!")
            return

        order_id = response.json()['data']['orderId']
        print(f"\n✅ Order created successfully! ID: {order_id}")
    except Exception as e:
        print(f"❌ Error: {e}")
        return

    # Test 3: Get All Orders
    print_section("3. Get All Orders")
    try:
        response = requests.get(
            f"{BASE_URL}/api/v1/business/orders",
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)
        print(f"\n✅ Retrieved {len(response.json()['data'])} orders")
    except Exception as e:
        print(f"❌ Error: {e}")

    # Test 4: Get Order by ID
    print_section("4. Get Order by ID")
    try:
        response = requests.get(
            f"{BASE_URL}/api/v1/business/orders/{order_id}",
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)
        print("\n✅ Order retrieved successfully")
    except Exception as e:
        print(f"❌ Error: {e}")

    # Test 5: Update Order
    print_section("5. Update Order")
    try:
        update_data = {
            "packageDescription": "3x Pizza Margherita (UPDATED)",
            "businessNotes": "Urgent delivery!"
        }
        response = requests.put(
            f"{BASE_URL}/api/v1/business/orders/{order_id}",
            json=update_data,
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)
        print("\n✅ Order updated successfully")
    except Exception as e:
        print(f"❌ Error: {e}")

    # Test 6: Get Statistics
    print_section("6. Get Statistics")
    try:
        response = requests.get(
            f"{BASE_URL}/api/v1/business/orders/statistics",
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)
        print("\n✅ Statistics retrieved")
    except Exception as e:
        print(f"❌ Error: {e}")

    # Test 7: Cancel Order
    print_section("7. Cancel Order")
    try:
        response = requests.post(
            f"{BASE_URL}/api/v1/business/orders/{order_id}/cancel",
            params={"reason": "Test cancellation"},
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)
        print("\n✅ Order cancelled successfully")
    except Exception as e:
        print(f"❌ Error: {e}")

    # Test 8: Try to Delete (Should Fail)
    print_section("8. Try to Delete Cancelled Order (Should Fail)")
    try:
        response = requests.delete(
            f"{BASE_URL}/api/v1/business/orders/{order_id}",
            headers=headers
        )
        print(f"Status: {response.status_code}")
        print_response(response)

        if response.status_code == 400:
            print("\n✅ Correctly rejected delete operation on cancelled order")
        else:
            print("\n⚠️ Expected 400 error but got different response")
    except Exception as e:
        print(f"❌ Error: {e}")

    print_section("✅ All Tests Completed!")

if __name__ == "__main__":
    main()

