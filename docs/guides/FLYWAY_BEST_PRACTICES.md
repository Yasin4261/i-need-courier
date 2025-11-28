# Flyway Migration Best Practices Guide

## 🚫 Yasaklar - Bu Sorunu Önlemek İçin

### 1. Migration Dosyalarını ASLA Değiştirme
```bash
# ✅ DOĞRU: Yeni migration dosyası oluştur
V14__Fix_previous_migration.sql

# ❌ YANLIŞ: Existing migration dosyasını değiştir
V13__Convert_shift_enums_to_varchar.sql  # Bu dosyayı değiştirme!
```

### 2. Production'da Validate-on-Migrate Kullan
```properties
# ✅ DOĞRU: Production'da validation açık
spring.flyway.validate-on-migrate=true

# ❌ YANLIŞ: Production'da validation kapalı
spring.flyway.validate-on-migrate=false  
```

### 3. Clean-Disabled Her Zaman True
```properties
# ✅ DOĞRU: Database'i yanlışlıkla temizleyemez
spring.flyway.clean-disabled=true
```

## ✅ Önerilen Workflow

### Development Ortamında:
```bash
# 1. Yeni migration oluştur
./mvnw flyway:migrate

# 2. Sorun varsa repair kullan
./mvnw flyway:repair

# 3. Development'da database'i sıfırla
./mvnw flyway:clean && ./mvnw flyway:migrate
```

### Production'da:
```bash
# 1. Sadece migrate kullan (clean yasak)
./mvnw flyway:migrate

# 2. Sorun varsa manuel repair
./mvnw flyway:repair

# 3. Backup al
pg_dump -h host -U user dbname > backup.sql
```

## 🔧 Troubleshooting Commands

### Checksum Mismatch Durumunda:
```bash
# Opsiyon 1: Repair (veriler korunur)
docker compose exec postgres psql -U courier_user -d courier_db
DELETE FROM flyway_schema_history WHERE version = 'X';
\q

# Opsiyon 2: Development'da clean start
docker compose down
docker volume rm i-need-courier_postgres_data  
docker compose up --build
```

### Migration Status Kontrol:
```bash
# Mevcut migration durumu
./mvnw flyway:info

# Validation check
./mvnw flyway:validate

# Repair if needed
./mvnw flyway:repair
```

## 📋 Git Hooks (Otomatik Kontrol)

`.git/hooks/pre-commit` dosyası oluştur:
```bash
#!/bin/bash
# Migration dosyaları değişti mi kontrol et
if git diff --cached --name-only | grep -q "src/main/resources/db/migration/V[0-9]"; then
    echo "❌ HATA: Existing migration dosyaları değiştirilemez!"
    echo "Yeni migration dosyası oluşturun: V$(date +%Y%m%d%H%M)__Description.sql"
    exit 1
fi
```

## 📊 Migration Naming Convention

```
V{YYYYMMDDHHMM}__{Description}.sql

Örnekler:
✅ V202511281030__Add_user_table.sql
✅ V202511281130__Fix_user_email_constraint.sql
❌ V13__Convert_shift_enums_to_varchar.sql (çok kısa)
```

## 🎯 Özet Kurallar

1. **ASLA** existing migration dosyasını değiştirme
2. **HER ZAMAN** validate-on-migrate=true kullan (production)
3. **HER ZAMAN** clean-disabled=true kullan
4. **Migration'dan önce** backup al
5. **Git hook** kullanarak otomatik kontrol ekle
6. **Development'da** temiz database ile test et
7. **Production'da** önce staging'de test et

Bu kuralları takip edersen bir daha checksum mismatch sorunu yaşamazsın! 🛡️
