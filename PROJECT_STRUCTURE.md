# 📁 Proje Organizasyonu

Bu dokümantasyon, proje yapısını ve dosyaların konumlarını açıklar.

---

## 📂 Kök Dizin Yapısı

```
i-need-courier/
├── 📄 README.md                    # Ana proje dokümantasyonu
├── 📄 CHANGELOG.md                 # Versiyon değişiklikleri
├── 📄 VERSION.md                   # Mevcut versiyon
├── 📄 LICENSE                      # Lisans bilgisi
├── 📄 pom.xml                      # Maven yapılandırması
├── 📄 Dockerfile                   # Docker image tanımı
├── 📄 compose.yaml                 # Docker Compose yapılandırması
├── 📄 nginx.conf                   # Nginx yapılandırması
├── 📁 src/                         # Kaynak kodlar
├── 📁 docs/                        # Dokümantasyon
├── 📁 scripts/                     # Yardımcı script'ler
├── 📁 postman/                     # Postman koleksiyonları
├── 📁 migrations/                  # Veritabanı migration'ları
├── 📁 backups/                     # Yedekler
└── 📁 logs/                        # Log dosyaları
```

---

## 📚 Dokümantasyon Yapısı (`docs/`)

### 📊 Reports (`docs/reports/`)
Test ve sistem raporları:
- `DELIVERY_SYSTEM_REPORT.md` - Teslimat sistemi test raporu
- `FINAL_TEST_REPORT.md` - Final test raporu
- `TEST_STATUS_REPORT.md` - Test durum raporu

### 🔧 Fixes (`docs/fixes/`)
Yapılan düzeltmeler ve iyileştirmeler:
- `COURIER_PICKUP_FIX.md` - Pickup hatası çözümü
- `CUSTOM_EXCEPTIONS_IMPLEMENTATION.md` - Custom exception implementasyonu
- `DEBUG_PICKUP_ISSUE.md` - Pickup debug rehberi
- `DUPLICATE_ASSIGNMENT_FIX.md` - Duplicate assignment fix
- `LOGGING_EXCEPTION_HANDLING_IMPROVEMENT.md` - Logging iyileştirmesi
- `PICKUP_COMPLETE_FIX.md` - Pickup/Complete endpoint fix

### 🔄 Flows (`docs/flows/`)
İş akışları ve kullanım rehberleri:
- `COURIER_ORDER_FLOW.md` - **Kurye sipariş akışı (adım adım)** ⭐
- `WHY_START_DELIVERY_EXISTS.md` - Start-delivery endpoint açıklaması
- `KURYE_TESLIMAT_CURL.txt` - Curl komutları

### 📖 Guides (`docs/guides/`)
Detaylı kullanım kılavuzları:
- `QUICKSTART.md` - Hızlı başlangıç
- `ORDER_ASSIGNMENT_SYSTEM.md` - Atama sistemi
- `SHIFT_MANAGEMENT_GUIDE.md` - Vardiya yönetimi
- `ON_DUTY_SYSTEM.md` - On-duty sistemi
- `DEVELOPED_ENDPOINTS.md` - Geliştirilen endpoint'ler
- Ve daha fazlası...

### 🗄️ Database
- `DATABASE_DESIGN.md` - Veritabanı tasarımı
- `DATABASE.md` - Veritabanı dokümantasyonu
- `db_graph.png` - Veritabanı şeması

### 🔐 API
API dokümantasyonları (`docs/api/`):
- `API.md` - Genel API dokümantasyonu
- `UNIFIED_AUTH_API.md` - Authentication API
- `COURIER_AUTH_API.md` - Kurye auth endpoints

### ⚙️ Setup
Kurulum ve migration bilgileri (`docs/setup/`):
- `MIGRATION_TO_CLEAN_ARCHITECTURE.md`
- `PROJECT_ORGANIZATION.md`

---

## 🔨 Script'ler (`scripts/`)

### Ana Script'ler
- `start.sh` - Sistemi başlat
- `stop.sh` - Sistemi durdur
- `setup-git.sh` - Git kurulumu

### Test Script'leri (`scripts/tests/`)
- `test-business-orders.sh` - Business order testleri
- `test-shift-operations.sh` - Vardiya testleri
- `test-shift-yasin.sh` - Özel vardiya testleri
- `test_business_orders.py` - Python test script
- `quick_test.py` - Hızlı test

### Delivery Test Script'leri
- `test-delivery-flow.sh` - Teslimat akışı testi
- `test-delivery-interactive.sh` - İnteraktif test
- `test-order-assignment.sh` - Assignment testi

---

## 📮 Postman Koleksiyonları (`postman/`)

- `Business_Orders_API.postman_collection.json` - Business API
- `Shift_Management_API.postman_collection.json` - Vardiya API
- `Shift_Test_Scenarios.postman_collection.json` - Test senaryoları

**Environment'lar:**
- `Development.postman_environment.json`
- `Production.postman_environment.json`

---

## 🚀 Hızlı Başlangıç

### 1. Sistemi Başlat
```bash
# Docker ile
docker compose up -d

# Veya script ile
./scripts/start.sh
```

### 2. Dokümantasyon Oku
```bash
# Hızlı başlangıç
cat docs/guides/QUICKSTART.md

# Kurye sipariş akışı
cat docs/flows/COURIER_ORDER_FLOW.md

# API dokümantasyonu
cat docs/api/API.md
```

### 3. Test Et
```bash
# Business order testi
./scripts/tests/test-business-orders.sh

# Teslimat akışı testi
./scripts/test-delivery-flow.sh
```

---

## 📝 Önemli Dosyalar

### Geliştirme İçin:
- 📄 `docs/guides/QUICKSTART.md` - Hemen başla
- 📄 `docs/flows/COURIER_ORDER_FLOW.md` - Sipariş akışı ⭐
- 📄 `docs/guides/DEVELOPED_ENDPOINTS.md` - Tüm endpoint'ler
- 📄 `docs/DATABASE_DESIGN.md` - Veritabanı şeması

### Test İçin:
- 📄 `docs/flows/KURYE_TESLIMAT_CURL.txt` - Curl komutları
- 📄 `docs/guides/TEST_LOGIN_GUIDE.md` - Login testi
- 📁 `postman/` - Postman koleksiyonları

### Sorun Çözme İçin:
- 📄 `docs/fixes/DEBUG_PICKUP_ISSUE.md` - Pickup debug
- 📄 `docs/fixes/LOGGING_EXCEPTION_HANDLING_IMPROVEMENT.md` - Logging
- 📄 `docs/guides/FLYWAY_BEST_PRACTICES.md` - Migration sorunları

---

## 🎯 Dizin Temizliği Yapıldı

**Kaldırılan Dosyalar:**
- ❌ `fix-flyway.sh` (eskimiş)
- ❌ `flyway-repair-guide.sh` (eskimiş)
- ❌ `migration.sh` (eskimiş)
- ❌ `github-push.sh` (kullanılmıyor)
- ❌ `GITHUB_PUSH_READY.md` (gereksiz)
- ❌ `GIT_FLOW_COMPLETE.md` (gereksiz)
- ❌ `PROJECT_COMPLETE.md` (gereksiz)
- ❌ `TEST_README.md` (gereksiz)

**Taşınan Dosyalar:**
- ✅ Report'lar → `docs/reports/`
- ✅ Fix'ler → `docs/fixes/`
- ✅ Flow'lar → `docs/flows/`
- ✅ Test script'leri → `scripts/tests/`

---

## 📌 Notlar

- Tüm dokümantasyon markdown formatında
- Script'ler bash ile yazılmış
- Postman koleksiyonları JSON formatında
- Database migration'ları `src/main/resources/db/migration/` altında

---

**Son Güncelleme:** December 6, 2025  
**Proje:** i-need-courier  
**Status:** ✅ Organize edildi ve temizlendi

