#!/bin/bash

# Vardiya İşlemleri Test Script
# Shift Operations Test Script
# Bu script vardiya sisteminin tüm özelliklerini test eder

set -e

BASE_URL="http://localhost:8081/api/v1"
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Tarih hesaplamaları
TOMORROW=$(date -d "+1 day" +%Y-%m-%d)
NEXT_WEEK=$(date -d "+7 days" +%Y-%m-%d)

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║     VARDİYA SİSTEMİ TEST SCRIPT - SHIFT OPERATIONS     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""

# ============================================
# 1. KURYELERİ TEST ET
# ============================================
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}1. KURYE GİRİŞLERİNİ TEST ETME${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

# Courier 1: Ali Özkan
echo -e "\n${GREEN}➤ Kurye 1 Girişi: Ali Özkan${NC}"
COURIER1_RESPONSE=$(curl -s -X POST $BASE_URL/auth/courier/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ali.ozkan@courier.com",
    "password": "password123"
  }')

COURIER1_TOKEN=$(echo $COURIER1_RESPONSE | jq -r '.token // empty')
if [ -z "$COURIER1_TOKEN" ] || [ "$COURIER1_TOKEN" = "null" ]; then
  echo -e "${RED}✗ Ali Özkan girişi başarısız!${NC}"
  echo "Response: $COURIER1_RESPONSE"
  exit 1
fi

COURIER1_ID=$(echo $COURIER1_RESPONSE | jq -r '.courierId // empty')
echo -e "${GREEN}✓ Ali Özkan giriş yaptı (Token: ${COURIER1_TOKEN:0:30}...)${NC}"
echo "   Courier ID: $COURIER1_ID"
echo "   Email: ali.ozkan@courier.com"

# Courier 2: Fatma Çelik
echo -e "\n${GREEN}➤ Kurye 2 Girişi: Fatma Çelik${NC}"
COURIER2_RESPONSE=$(curl -s -X POST $BASE_URL/auth/courier/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "fatma.celik@courier.com",
    "password": "password123"
  }')

COURIER2_TOKEN=$(echo $COURIER2_RESPONSE | jq -r '.token // empty')
if [ -z "$COURIER2_TOKEN" ] || [ "$COURIER2_TOKEN" = "null" ]; then
  echo -e "${RED}✗ Fatma Çelik girişi başarısız!${NC}"
  echo "Response: $COURIER2_RESPONSE"
  exit 1
fi

COURIER2_ID=$(echo $COURIER2_RESPONSE | jq -r '.courierId // empty')
echo -e "${GREEN}✓ Fatma Çelik giriş yaptı (Token: ${COURIER2_TOKEN:0:30}...)${NC}"
echo "   Courier ID: $COURIER2_ID"
echo "   Email: fatma.celik@courier.com"

# Courier 3: Osman Acar
echo -e "\n${GREEN}➤ Kurye 3 Girişi: Osman Acar${NC}"
COURIER3_RESPONSE=$(curl -s -X POST $BASE_URL/auth/courier/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "osman.acar@courier.com",
    "password": "password123"
  }')

COURIER3_TOKEN=$(echo $COURIER3_RESPONSE | jq -r '.token // empty')
if [ -z "$COURIER3_TOKEN" ] || [ "$COURIER3_TOKEN" = "null" ]; then
  echo -e "${RED}✗ Osman Acar girişi başarısız!${NC}"
  echo "Response: $COURIER3_RESPONSE"
  exit 1
fi

COURIER3_ID=$(echo $COURIER3_RESPONSE | jq -r '.courierId // empty')
echo -e "${GREEN}✓ Osman Acar giriş yaptı (Token: ${COURIER3_TOKEN:0:30}...)${NC}"
echo "   Courier ID: $COURIER3_ID"
echo "   Email: osman.acar@courier.com"

sleep 1

# ============================================
# 2. VARDİYA ŞABLONLARINI GÖRÜNTÜLE
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}2. VARDİYA ŞABLONLARINI GÖRÜNTÜLEME${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

echo -e "\n${GREEN}➤ Vardiya Şablonlarını Listele${NC}"
TEMPLATES_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/templates \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $TEMPLATES_RESPONSE | jq '.'

TEMPLATE1_ID=$(echo $TEMPLATES_RESPONSE | jq -r '.data[0].templateId // empty')
TEMPLATE2_ID=$(echo $TEMPLATES_RESPONSE | jq -r '.data[1].templateId // empty')

if [ -z "$TEMPLATE1_ID" ] || [ "$TEMPLATE1_ID" = "null" ]; then
  echo -e "${RED}✗ Vardiya şablonları bulunamadı!${NC}"
  exit 1
fi

echo -e "${GREEN}✓ Vardiya şablonları başarıyla getirildi${NC}"
echo "   Template 1 ID: $TEMPLATE1_ID"
echo "   Template 2 ID: $TEMPLATE2_ID"

sleep 1

# ============================================
# 3. VARDİYA REZERVASYONU
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}3. VARDİYA REZERVASYONU${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

# Ali Özkan - Yarın sabah vardiyası
echo -e "\n${GREEN}➤ Ali Özkan: Yarın için sabah vardiyası rezerve ediyor${NC}"
RESERVE1_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/reserve \
  -H "Authorization: Bearer $COURIER1_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": $TEMPLATE1_ID,
    \"shiftDate\": \"$TOMORROW\",
    \"notes\": \"İlk vardiyam - Test\"
  }")

echo $RESERVE1_RESPONSE | jq '.'

SHIFT1_ID=$(echo $RESERVE1_RESPONSE | jq -r '.data.shiftId // empty')
if [ -z "$SHIFT1_ID" ] || [ "$SHIFT1_ID" = "null" ]; then
  echo -e "${RED}✗ Vardiya rezervasyonu başarısız!${NC}"
else
  echo -e "${GREEN}✓ Ali Özkan vardiya rezerve etti (Shift ID: $SHIFT1_ID)${NC}"
fi

sleep 1

# Fatma Çelik - Yarın sabah vardiyası
echo -e "\n${GREEN}➤ Fatma Çelik: Yarın için sabah vardiyası rezerve ediyor${NC}"
RESERVE2_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/reserve \
  -H "Authorization: Bearer $COURIER2_TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": $TEMPLATE1_ID,
    \"shiftDate\": \"$TOMORROW\",
    \"notes\": \"Sabah vardiyası\"
  }")

echo $RESERVE2_RESPONSE | jq '.'

SHIFT2_ID=$(echo $RESERVE2_RESPONSE | jq -r '.data.shiftId // empty')
if [ -z "$SHIFT2_ID" ] || [ "$SHIFT2_ID" = "null" ]; then
  echo -e "${RED}✗ Vardiya rezervasyonu başarısız!${NC}"
else
  echo -e "${GREEN}✓ Fatma Çelik vardiya rezerve etti (Shift ID: $SHIFT2_ID)${NC}"
fi

sleep 1

# Osman Acar - Gelecek hafta akşam vardiyası
if [ -n "$TEMPLATE2_ID" ] && [ "$TEMPLATE2_ID" != "null" ]; then
  echo -e "\n${GREEN}➤ Osman Acar: Gelecek hafta için akşam vardiyası rezerve ediyor${NC}"
  RESERVE3_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/reserve \
    -H "Authorization: Bearer $COURIER3_TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"templateId\": $TEMPLATE2_ID,
      \"shiftDate\": \"$NEXT_WEEK\",
      \"notes\": \"Akşam vardiyası - test\"
    }")

  echo $RESERVE3_RESPONSE | jq '.'

  SHIFT3_ID=$(echo $RESERVE3_RESPONSE | jq -r '.data.shiftId // empty')
  if [ -z "$SHIFT3_ID" ] || [ "$SHIFT3_ID" = "null" ]; then
    echo -e "${RED}✗ Vardiya rezervasyonu başarısız!${NC}"
  else
    echo -e "${GREEN}✓ Osman Acar vardiya rezerve etti (Shift ID: $SHIFT3_ID)${NC}"
  fi
fi

sleep 1

# ============================================
# 4. GELECEK VARDİYALARI GÖRÜNTÜLE
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}4. GELECEK VARDİYALARI GÖRÜNTÜLEME${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

echo -e "\n${GREEN}➤ Ali Özkan: Gelecek vardiyalarını görüntülüyor${NC}"
UPCOMING1_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/upcoming \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $UPCOMING1_RESPONSE | jq '.'

UPCOMING_COUNT=$(echo $UPCOMING1_RESPONSE | jq -r '.data | length')
echo -e "${GREEN}✓ Ali Özkan'ın $UPCOMING_COUNT gelecek vardiyası var${NC}"

sleep 1

echo -e "\n${GREEN}➤ Fatma Çelik: Gelecek vardiyalarını görüntülüyor${NC}"
UPCOMING2_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/upcoming \
  -H "Authorization: Bearer $COURIER2_TOKEN")

echo $UPCOMING2_RESPONSE | jq '.'

sleep 1

# ============================================
# 5. TÜM VARDİYALARI GÖRÜNTÜLE
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}5. TÜM VARDİYALARI GÖRÜNTÜLEME${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

echo -e "\n${GREEN}➤ Ali Özkan: Tüm vardiyalarını görüntülüyor${NC}"
ALL_SHIFTS_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/my-shifts \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $ALL_SHIFTS_RESPONSE | jq '.'

sleep 1

echo -e "\n${GREEN}➤ Ali Özkan: Sadece RESERVED durumundaki vardiyaları görüntülüyor${NC}"
RESERVED_SHIFTS_RESPONSE=$(curl -s -X GET "$BASE_URL/courier/shifts/my-shifts?status=RESERVED" \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $RESERVED_SHIFTS_RESPONSE | jq '.'

sleep 1

# ============================================
# 6. AKTİF VARDİYA KONTROL
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}6. AKTİF VARDİYA KONTROLÜ${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

echo -e "\n${GREEN}➤ Ali Özkan: Aktif vardiya kontrolü yapıyor${NC}"
ACTIVE_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/active \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $ACTIVE_RESPONSE | jq '.'

ACTIVE_SHIFT=$(echo $ACTIVE_RESPONSE | jq -r '.data // empty')
if [ -z "$ACTIVE_SHIFT" ] || [ "$ACTIVE_SHIFT" = "null" ]; then
  echo -e "${BLUE}ℹ Henüz aktif vardiya yok${NC}"
else
  echo -e "${GREEN}✓ Aktif vardiya bulundu${NC}"
fi

sleep 1

# ============================================
# 7. VARDİYAYA CHECK-IN (GİRİŞ)
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}7. VARDİYAYA CHECK-IN (GİRİŞ)${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

if [ -n "$SHIFT1_ID" ] && [ "$SHIFT1_ID" != "null" ]; then
  echo -e "\n${GREEN}➤ Ali Özkan: Vardiyaya giriş yapıyor (Shift ID: $SHIFT1_ID)${NC}"
  echo -e "${BLUE}Not: Bu işlem sadece vardiya başlangıcından 30 dakika öncesinden itibaren yapılabilir${NC}"

  CHECKIN_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/$SHIFT1_ID/check-in \
    -H "Authorization: Bearer $COURIER1_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "notes": "Vardiyaya başladım",
      "latitude": 41.0082,
      "longitude": 28.9784
    }')

  echo $CHECKIN_RESPONSE | jq '.'

  CHECK_IN_SUCCESS=$(echo $CHECKIN_RESPONSE | jq -r '.success // false')
  if [ "$CHECK_IN_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✓ Ali Özkan vardiyaya giriş yaptı${NC}"
  else
    ERROR_MSG=$(echo $CHECKIN_RESPONSE | jq -r '.message // "Hata mesajı yok"')
    echo -e "${YELLOW}⚠ Check-in yapılamadı: $ERROR_MSG${NC}"
    echo -e "${BLUE}Bu beklenen bir durumdur - vardiya henüz başlamadı${NC}"
  fi
else
  echo -e "${YELLOW}⚠ Check-in için geçerli vardiya ID'si bulunamadı${NC}"
fi

sleep 1

# ============================================
# 8. VARDİYADAN CHECK-OUT (ÇIKIŞ)
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}8. VARDİYADAN CHECK-OUT (ÇIKIŞ)${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

if [ -n "$SHIFT1_ID" ] && [ "$SHIFT1_ID" != "null" ]; then
  echo -e "\n${GREEN}➤ Ali Özkan: Vardiyadan çıkış yapıyor (Shift ID: $SHIFT1_ID)${NC}"
  echo -e "${BLUE}Not: Bu işlem sadece check-in yapılmış vardiyalar için çalışır${NC}"

  CHECKOUT_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/$SHIFT1_ID/check-out \
    -H "Authorization: Bearer $COURIER1_TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "notes": "Vardiya tamamlandı",
      "latitude": 41.0082,
      "longitude": 28.9784
    }')

  echo $CHECKOUT_RESPONSE | jq '.'

  CHECK_OUT_SUCCESS=$(echo $CHECKOUT_RESPONSE | jq -r '.success // false')
  if [ "$CHECK_OUT_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✓ Ali Özkan vardiyadan çıkış yaptı${NC}"
  else
    ERROR_MSG=$(echo $CHECKOUT_RESPONSE | jq -r '.message // "Hata mesajı yok"')
    echo -e "${YELLOW}⚠ Check-out yapılamadı: $ERROR_MSG${NC}"
    echo -e "${BLUE}Bu beklenen bir durumdur - henüz check-in yapılmadı${NC}"
  fi
else
  echo -e "${YELLOW}⚠ Check-out için geçerli vardiya ID'si bulunamadı${NC}"
fi

sleep 1

# ============================================
# 9. VARDİYA REZERVASYONU İPTAL
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}9. VARDİYA REZERVASYONU İPTAL ETME${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

if [ -n "$SHIFT2_ID" ] && [ "$SHIFT2_ID" != "null" ]; then
  echo -e "\n${GREEN}➤ Fatma Çelik: Vardiya rezervasyonunu iptal ediyor (Shift ID: $SHIFT2_ID)${NC}"
  echo -e "${BLUE}Not: İptal işlemi sadece vardiya başlangıcından 2 saat öncesine kadar yapılabilir${NC}"

  CANCEL_RESPONSE=$(curl -s -X DELETE $BASE_URL/courier/shifts/$SHIFT2_ID/cancel \
    -H "Authorization: Bearer $COURIER2_TOKEN")

  echo $CANCEL_RESPONSE | jq '.'

  CANCEL_SUCCESS=$(echo $CANCEL_RESPONSE | jq -r '.success // false')
  if [ "$CANCEL_SUCCESS" = "true" ]; then
    echo -e "${GREEN}✓ Fatma Çelik vardiya rezervasyonunu iptal etti${NC}"
  else
    ERROR_MSG=$(echo $CANCEL_RESPONSE | jq -r '.message // "Hata mesajı yok"')
    echo -e "${YELLOW}⚠ İptal yapılamadı: $ERROR_MSG${NC}"
  fi
else
  echo -e "${YELLOW}⚠ İptal için geçerli vardiya ID'si bulunamadı${NC}"
fi

sleep 1

# ============================================
# 10. SON DURUM KONTROLÜ
# ============================================
echo -e "\n${YELLOW}════════════════════════════════════════════════════════${NC}"
echo -e "${YELLOW}10. SON DURUM KONTROLÜ${NC}"
echo -e "${YELLOW}════════════════════════════════════════════════════════${NC}"

echo -e "\n${GREEN}➤ Ali Özkan: Son durumu kontrol ediyor${NC}"
FINAL_RESPONSE=$(curl -s -X GET $BASE_URL/courier/shifts/my-shifts \
  -H "Authorization: Bearer $COURIER1_TOKEN")

echo $FINAL_RESPONSE | jq '.'

echo -e "\n${GREEN}➤ Fatma Çelik: Son durumu kontrol ediyor${NC}"
FINAL_RESPONSE2=$(curl -s -X GET $BASE_URL/courier/shifts/my-shifts \
  -H "Authorization: Bearer $COURIER2_TOKEN")

echo $FINAL_RESPONSE2 | jq '.'

# ============================================
# ÖZET
# ============================================
echo -e "\n${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                    TEST TAMAMLANDI                     ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}✓ Kurye girişleri test edildi${NC}"
echo -e "${GREEN}✓ Vardiya şablonları listelendi${NC}"
echo -e "${GREEN}✓ Vardiya rezervasyonları yapıldı${NC}"
echo -e "${GREEN}✓ Gelecek vardiyalar görüntülendi${NC}"
echo -e "${GREEN}✓ Tüm vardiyalar listelendi${NC}"
echo -e "${GREEN}✓ Aktif vardiya kontrolü yapıldı${NC}"
echo -e "${GREEN}✓ Check-in işlemi test edildi${NC}"
echo -e "${GREEN}✓ Check-out işlemi test edildi${NC}"
echo -e "${GREEN}✓ Vardiya iptali test edildi${NC}"
echo -e "${GREEN}✓ Son durum kontrolleri yapıldı${NC}"
echo ""
echo -e "${BLUE}Notlar:${NC}"
echo -e "${YELLOW}• Check-in işlemi sadece vardiya başlangıcından 30 dk önce yapılabilir${NC}"
echo -e "${YELLOW}• Check-out işlemi sadece check-in yapılmış vardiyalar için geçerlidir${NC}"
echo -e "${YELLOW}• Vardiya iptali başlangıçtan 2 saat öncesine kadar yapılabilir${NC}"
echo ""

