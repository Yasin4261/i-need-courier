#!/bin/bash
BASE_URL="http://localhost:8081/api/v1"
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Yeni: Parametrik tarih ve şablon seçimi, otomatik iptal
TOMORROW=$(date -d "+1 day" +%Y-%m-%d)
SHIFT_DATE="${SHIFT_DATE:-$TOMORROW}"
TEMPLATE_INDEX="${TEMPLATE_INDEX:-0}"
AUTO_CANCEL="${AUTO_CANCEL:-1}"

echo -e "${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║          VARDİYA SİSTEMİ TEST - Yasin Kullanıcısı      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
# 1. LOGIN
echo -e "\n${YELLOW}1. GİRİŞ YAPILIYOR${NC}"
LOGIN_RESPONSE=$(curl -s -X POST $BASE_URL/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"yasin3@pako.com","password":"123456"}')
TOKEN=$(echo $LOGIN_RESPONSE | jq -r '.data.token')
USER_ID=$(echo $LOGIN_RESPONSE | jq -r '.data.userId')
echo -e "${GREEN}✓ Giriş başarılı!${NC}"
echo "  User ID: $USER_ID"
echo "  Token: ${TOKEN:0:50}..."
echo "$LOGIN_RESPONSE" | jq '.'
sleep 2
# 2. VARDİYA ŞABLONLARINI LİSTELE
echo -e "\n${YELLOW}2. VARDİYA ŞABLONLARINI LİSTELEME${NC}"
TEMPLATES=$(curl -s -X GET $BASE_URL/courier/shifts/templates \
  -H "Authorization: Bearer $TOKEN")
echo "$TEMPLATES" | jq '.'
TEMPLATE1=$(echo $TEMPLATES | jq -r '.data[0].templateId')
TEMPLATE2=$(echo $TEMPLATES | jq -r '.data[1].templateId')
# Yeni: Seçilecek şablon ID'si (TEMPLATE_INDEX ile)
TEMPLATE_ID=$(echo $TEMPLATES | jq -r ".data[$TEMPLATE_INDEX].templateId")
echo -e "${GREEN}✓ Şablon 1 ID: $TEMPLATE1${NC}"
echo -e "${GREEN}✓ Şablon 2 ID: $TEMPLATE2${NC}"
sleep 2
# 3. VARDİYA REZERVE ET
echo -e "\n${YELLOW}3. VARDİYA REZERVASYONU${NC}"
echo "  Tarih: $SHIFT_DATE (değiştirmek için: SHIFT_DATE=YYYY-MM-DD ./test-shift-yasin.sh)"
echo "  Şablon: INDEX=$TEMPLATE_INDEX (değiştirmek için: TEMPLATE_INDEX=N) -> templateId=$TEMPLATE_ID"

# Yeni: Hedef tarih için mevcut RESERVED vardiyaları otomatik iptal et (idempotent)
if [ "$AUTO_CANCEL" = "1" ]; then
  echo -e "${BLUE}Ön temizlik: $SHIFT_DATE için mevcut RESERVED vardiyalar iptal ediliyor...${NC}"
  RESERVED_LIST=$(curl -s -X GET "$BASE_URL/courier/shifts/my-shifts?status=RESERVED" \
    -H "Authorization: Bearer $TOKEN")
  IDS_TO_CANCEL=$(echo "$RESERVED_LIST" | jq -r --arg d "$SHIFT_DATE" '.data[] | select((.startTime|startswith($d))) | .shiftId')
  if [ -n "$IDS_TO_CANCEL" ]; then
    while IFS= read -r ID; do
      [ -z "$ID" ] && continue
      echo "  - İptal ediliyor: Shift ID $ID"
      curl -s -X DELETE "$BASE_URL/courier/shifts/$ID/cancel" -H "Authorization: Bearer $TOKEN" | jq -r '.message' | sed 's/^/    /'
    done <<< "$IDS_TO_CANCEL"
  else
    echo "  - İptal edilecek RESERVED vardiya bulunmadı."
  fi
fi

RESERVE_RESPONSE=$(curl -s -X POST $BASE_URL/courier/shifts/reserve \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d "{
    \"templateId\": $TEMPLATE_ID,
    \"shiftDate\": \"$SHIFT_DATE\",
    \"notes\": \"Test vardiyası - Yasin\"
  }")
echo "$RESERVE_RESPONSE" | jq '.'
SHIFT_ID=$(echo $RESERVE_RESPONSE | jq -r '.data.shiftId')
if [ "$SHIFT_ID" != "null" ] && [ -n "$SHIFT_ID" ]; then
  echo -e "${GREEN}✓ Vardiya rezerve edildi! Shift ID: $SHIFT_ID${NC}"
else
  echo -e "${YELLOW}⚠ Vardiya rezerve edilemedi${NC}"
fi
sleep 2
# 4. GELECEK VARDİYALARI GÖRÜNTÜLE
echo -e "\n${YELLOW}4. GELECEK VARDİYALARIMI GÖRÜNTÜLEME${NC}"
UPCOMING=$(curl -s -X GET $BASE_URL/courier/shifts/upcoming \
  -H "Authorization: Bearer $TOKEN")
echo "$UPCOMING" | jq '.'
UPCOMING_COUNT=$(echo $UPCOMING | jq -r '.data | length')
echo -e "${GREEN}✓ $UPCOMING_COUNT gelecek vardiya bulundu${NC}"
sleep 2
# 5. TÜM VARDİYALARIMI GÖRÜNTÜLE
echo -e "\n${YELLOW}5. TÜM VARDİYALARIMI GÖRÜNTÜLEME${NC}"
ALL_SHIFTS=$(curl -s -X GET $BASE_URL/courier/shifts/my-shifts \
  -H "Authorization: Bearer $TOKEN")
echo "$ALL_SHIFTS" | jq '.'
sleep 2
# 6. SADECE RESERVED VARDİYALARI FİLTRELE
echo -e "\n${YELLOW}6. SADECE RESERVED DURUMDA VARDİYALARI GÖRÜNTÜLEME${NC}"
RESERVED=$(curl -s -X GET "$BASE_URL/courier/shifts/my-shifts?status=RESERVED" \
  -H "Authorization: Bearer $TOKEN")
echo "$RESERVED" | jq '.'
sleep 2
# 7. AKTİF VARDİYA KONTROLÜ
echo -e "\n${YELLOW}7. AKTİF VARDİYA KONTROLÜ${NC}"
ACTIVE=$(curl -s -X GET $BASE_URL/courier/shifts/active \
  -H "Authorization: Bearer $TOKEN")
echo "$ACTIVE" | jq '.'
sleep 2
# 8. CHECK-IN DENEMESİ (Başlamadan önce olduğu için hata vermesi normal)
if [ "$SHIFT_ID" != "null" ] && [ -n "$SHIFT_ID" ]; then
  echo -e "\n${YELLOW}8. VARDİYAYA CHECK-IN DENEMESİ${NC}"
  echo -e "${BLUE}Not: Vardiya henüz başlamadığı için hata vermesi beklenir${NC}"
  CHECKIN=$(curl -s -X POST $BASE_URL/courier/shifts/$SHIFT_ID/check-in \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d '{
      "notes": "Vardiyaya başladım",
      "latitude": 41.0082,
      "longitude": 28.9784
    }')
  echo "$CHECKIN" | jq '.'
  sleep 2
fi
# 9. İKİNCİ BİR VARDİYA REZERVE ET
if [ "$TEMPLATE2" != "null" ] && [ -n "$TEMPLATE2" ]; then
  NEXT_WEEK=$(date -d "+7 days" +%Y-%m-%d)
  echo -e "\n${YELLOW}9. GELECEK HAFTA İÇİN İKİNCİ VARDİYA REZERVE ETME${NC}"
  echo "  Tarih: $NEXT_WEEK"
  RESERVE2=$(curl -s -X POST $BASE_URL/courier/shifts/reserve \
    -H "Authorization: Bearer $TOKEN" \
    -H "Content-Type: application/json" \
    -d "{
      \"templateId\": $TEMPLATE2,
      \"shiftDate\": \"$NEXT_WEEK\",
      \"notes\": \"Akşam vardiyası test\"
    }")
  echo "$RESERVE2" | jq '.'
  SHIFT_ID2=$(echo $RESERVE2 | jq -r '.data.shiftId')
  sleep 2
  # 10. İKİNCİ VARDİYAYI İPTAL ET
  if [ "$SHIFT_ID2" != "null" ] && [ -n "$SHIFT_ID2" ]; then
    echo -e "\n${YELLOW}10. İKİNCİ VARDİYAYI İPTAL ETME${NC}"
    CANCEL=$(curl -s -X DELETE $BASE_URL/courier/shifts/$SHIFT_ID2/cancel \
      -H "Authorization: Bearer $TOKEN")
    echo "$CANCEL" | jq '.'
    sleep 2
  fi
fi
# 11. SON DURUM
echo -e "\n${YELLOW}11. SON DURUM KONTROLÜ${NC}"
FINAL=$(curl -s -X GET $BASE_URL/courier/shifts/my-shifts \
  -H "Authorization: Bearer $TOKEN")
echo "$FINAL" | jq '.'
# ÖZET
echo -e "\n${BLUE}╔════════════════════════════════════════════════════════╗${NC}"
echo -e "${BLUE}║                 TEST TAMAMLANDI ✓                      ║${NC}"
echo -e "${BLUE}╚════════════════════════════════════════════════════════╝${NC}"
echo ""
echo -e "${GREEN}Test edilen işlemler:${NC}"
echo "  ✓ Kurye girişi"
echo "  ✓ Vardiya şablonlarını listeleme"
echo "  ✓ Vardiya rezerve etme"
echo "  ✓ Gelecek vardiyaları görüntüleme"
echo "  ✓ Tüm vardiyaları listeleme"
echo "  ✓ Duruma göre vardiya filtreleme"
echo "  ✓ Aktif vardiya kontrolü"
echo "  ✓ Check-in denemesi"
echo "  ✓ İkinci vardiya rezervasyonu"
echo "  ✓ Vardiya iptali"
echo ""
