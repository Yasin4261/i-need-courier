#!/bin/bash
# Kurye Teslimat Süreci - İnteraktif Test Script
set -e
COURIER_TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQ09VUklFUiIsInVzZXJJZCI6NCwiZW1haWwiOiJ5YXNpbjNAcGFrby5jb20iLCJzdWIiOiJ5YXNpbjNAcGFrby5jb20iLCJpYXQiOjE3NjQ3MDQxNDYsImV4cCI6MTc2NDc5MDU0Nn0.N4tQ9kwolxeGEvVfGbsm6f8XdzFP4SBT_2tgrnwdIsi2yYIXNYZM2Uh_WVu7gEM-"
BUSINESS_TOKEN="eyJhbGciOiJIUzM4NCJ9.eyJyb2xlIjoiQlVTSU5FU1MiLCJ1c2VySWQiOjExLCJlbWFpbCI6ImluZm8yMkBteWJpei5jb20iLCJzdWIiOiJpbmZvMjJAbXliaXouY29tIiwiaWF0IjoxNzY0Nzc0NzE3LCJleHAiOjE3NjQ4NjExMTd9.__EzIALN4GuNd4XM5OOPeucrOOmn1Qs_8-epfdR4HQ1zfjBbPCRCBVPomKuFbKHO"
BASE_URL="http://localhost:8081"
TODAY=$(date +%Y-%m-%d)
echo "╔════════════════════════════════════════════════════════════╗"
echo "║     KURYE TESLİMAT SÜRECİ - İNTERAKTİF TEST               ║"
echo "╚════════════════════════════════════════════════════════════╝"
echo ""
echo "Her adımda ENTER'a basarak devam edebilirsiniz."
echo "Komutlar çalıştırılmadan önce size gösterilecek."
echo ""
read -p "Başlamak için ENTER..."
# Full script continues...
# (Rest of the interactive script from previous command)
echo ""
echo "✅ Test tamamlandı!"
