# 🚚 I Need Courier - Courier Management System

> **Modern, Clean Layered Architecture ile geliştirilmiş kurye yönetim sistemi**

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

---

## 📋 İçindekiler

- [Hızlı Başlangıç](#-hızlı-başlangıç)
- [Özellikler](#-özellikler)
- [Mimari](#️-mimari)
- [Dokümantasyon](#-dokümantasyon)
- [Kurulum](#-kurulum)
- [API Kullanımı](#-api-kullanımı)
- [Geliştirme](#-geliştirme)
- [Katkıda Bulunma](#-katkıda-bulunma)

---

## 🚀 Hızlı Başlangıç

### 3 Adımda Çalıştır

```bash
# 1. Veritabanını başlat
docker compose up -d postgres

# 2. Projeyi derle
./mvnw clean package -DskipTests

# 3. Uygulamayı çalıştır
./scripts/start.sh
```

**Uygulama hazır!** → http://localhost:8080

### Test Et

```bash
# Sağlık kontrolü
curl http://localhost:8080/actuator/health

# Kurye kaydı
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"name":"John Doe","email":"john@test.com","phone":"+905551234567","password":"password123"}'

# Giriş yap
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"courier1@test.com","password":"password123"}'
```

---

## ✨ Özellikler

### Mevcut Özellikler
- ✅ **Kurye Kayıt Sistemi** - Yeni kuryelerin kayıt olması
- ✅ **JWT Authentication** - Güvenli giriş ve token yönetimi
- ✅ **Detaylı Hata Yönetimi** - Kullanıcı dostu hata mesajları
- ✅ **Validation** - Kapsamlı input validasyonu
- ✅ **API Dokümantasyonu** - Swagger/OpenAPI entegrasyonu
- ✅ **Health Checks** - Actuator ile sistem sağlığı kontrolü
- ✅ **Database Migrations** - Flyway ile versiyon kontrollü DB

### Planlanan Özellikler
- 🔜 **Sipariş Yönetimi** - Sipariş oluşturma ve takip
- 🔜 **Gerçek Zamanlı Takip** - WebSocket ile canlı konum
- 🔜 **İşletme Paneli** - İşletmeler için yönetim arayüzü
- 🔜 **Bildirimler** - Push notification sistemi
- 🔜 **Analytics Dashboard** - İstatistik ve raporlama

---

## 🏗️ Mimari

### Clean Layered Architecture

```
┌─────────────────────────────────────┐
│     Controller Layer                │  ← REST API Endpoints
├─────────────────────────────────────┤
│     Service Layer                   │  ← Business Logic
├─────────────────────────────────────┤
│     Repository Layer                │  ← Data Access
├─────────────────────────────────────┤
│     Model Layer                     │  ← Domain Entities
└─────────────────────────────────────┘
```

### Proje Yapısı

```
i-need-courier/
├── src/main/java/com/api/demo/
│   ├── controller/      # REST Controllers
│   ├── service/         # Business Logic
│   ├── repository/      # Data Access
│   ├── model/          # JPA Entities
│   ├── dto/            # Data Transfer Objects
│   ├── exception/      # Exception Handling
│   ├── config/         # Configuration
│   └── security/       # Security & JWT
├── docs/               # 📚 Tüm Dokümantasyon
│   ├── guides/         # Geliştirme rehberleri
│   ├── api/            # API dokümantasyonu
│   └── setup/          # Kurulum ve migration
├── scripts/            # 🔧 Otomasyon scriptleri
│   ├── start.sh
│   ├── stop.sh
│   └── setup-git.sh
├── .github/            # GitHub templates & CI/CD
└── migrations/         # Database migrations
```

**📖 Detaylı mimari açıklaması:** [Clean Layered Architecture Guide](docs/guides/CLEAN_LAYERED_ARCHITECTURE.md)

---

## 📚 Dokümantasyon

### 🎯 Hızlı Başlangıç
- **[Quick Start Guide](docs/guides/QUICKSTART.md)** - 5 dakikada başla
- **[Migration Summary](docs/setup/MIGRATION_SUMMARY.md)** - Mimari geçiş özeti

### 📖 Geliştirme Rehberleri
- **[Clean Layered Architecture](docs/guides/CLEAN_LAYERED_ARCHITECTURE.md)** - Mimari detayları
- **[Contributing Guide](docs/guides/CONTRIBUTING.md)** - Nasıl katkıda bulunulur
- **[Git Workflow](docs/guides/GIT_WORKFLOW.md)** - Branch stratejisi ve commit kuralları

### 🔧 Kurulum & Setup
- **[GitHub Setup](docs/setup/GITHUB_SETUP_COMPLETE.md)** - GitHub yapılandırması
- **[Migration Guide](docs/setup/MIGRATION_TO_CLEAN_ARCHITECTURE.md)** - Hexagonal'den Clean'e geçiş

### 🌐 API Dokümantasyonu
- **[API Overview](docs/api/API.md)** - Genel API bilgisi
- **[Courier Auth API](docs/api/COURIER_AUTH_API.md)** - Authentication endpoints
- **Swagger UI**: http://localhost:8080/swagger-ui/index.html (uygulama çalışırken)

### 💾 Database
- **[Database Design](docs/DATABASE_DESIGN.md)** - Veritabanı şeması
- **[Database Guide](docs/DATABASE.md)** - Veritabanı kullanımı

---

## 🛠️ Kurulum

### Gereksinimler

- **Java 21+**
- **Maven 3.9+**
- **Docker & Docker Compose**
- **PostgreSQL 16** (Docker ile gelir)

### Detaylı Kurulum

```bash
# 1. Repository'yi klonla
git clone https://github.com/YOUR_USERNAME/i-need-courier.git
cd i-need-courier

# 2. Veritabanını başlat
docker compose up -d postgres

# 3. Projeyi derle
./mvnw clean install

# 4. Uygulamayı çalıştır
./scripts/start.sh
```

### Environment Variables

```bash
# application.properties veya environment
SERVER_PORT=8080
DATABASE_URL=jdbc:postgresql://localhost:5432/courier_db
DATABASE_USERNAME=courier_user
DATABASE_PASSWORD=courier_pass
JWT_SECRET=your-secret-key-change-in-production
JWT_EXPIRATION_HOURS=24
```

---

## 🌐 API Kullanımı

### Authentication Endpoints

#### Kurye Kaydı
```bash
POST /api/v1/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "phone": "+905551234567",
  "password": "password123"
}
```

**Başarılı Response (200)**:
```json
{
  "code": 200,
  "data": {
    "courierId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "message": "Registration successful"
  },
  "message": "Registration successful"
}
```

**Hata Response (400 - Validation)**:
```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Validation failed for one or more fields",
  "path": "/api/v1/auth/register",
  "timestamp": "2025-10-31T05:30:00",
  "validationErrors": [
    {
      "field": "email",
      "message": "Email must be valid"
    }
  ]
}
```

#### Giriş
```bash
POST /api/v1/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "password123"
}
```

**Başarılı Response (200)**:
```json
{
  "code": 200,
  "data": {
    "token": "eyJhbGciOiJIUzM4NCJ9...",
    "courierId": 1,
    "name": "John Doe",
    "email": "john@example.com",
    "status": "Login successful"
  },
  "message": "Login successful"
}
```

### Test Kullanıcıları

```bash
# Test User 1
Email: courier1@test.com
Password: password123

# Test User 2
Email: courier2@test.com
Password: password123

# Test User 3
Email: courier3@test.com
Password: password123
```

---

## 💻 Geliştirme

### Development Workflow

```bash
# 1. Feature branch oluştur
git checkout -b feature/123-new-feature

# 2. Değişikliklerini yap
# ... kod yaz

# 3. Test et
./mvnw test

# 4. Commit et (Conventional Commits)
git commit -m "feat(orders): add order tracking endpoint"

# 5. Push ve PR oluştur
git push -u origin feature/123-new-feature
```

### Çalışma Komutları

```bash
# Uygulamayı başlat
./scripts/start.sh

# Uygulamayı durdur
./scripts/stop.sh

# Build
./mvnw clean install

# Testleri çalıştır
./mvnw test

# Sadece derle
./mvnw compile

# Package oluştur
./mvnw package -DskipTests

# Logları görüntüle
tail -f logs/app.log
```

### Code Style

- **Format**: Google Java Style Guide
- **Line Length**: 120 characters
- **Indentation**: 4 spaces
- **Naming**: camelCase (methods/variables), PascalCase (classes)

---

## 🤝 Katkıda Bulunma

Katkılarınızı bekliyoruz! 

### Katkı Süreci

1. **Fork** edin
2. **Feature branch** oluşturun (`git checkout -b feature/amazing-feature`)
3. **Commit** edin (`git commit -m 'feat: add amazing feature'`)
4. **Push** edin (`git push origin feature/amazing-feature`)
5. **Pull Request** açın

### Kurallar

- ✅ [Contributing Guide](docs/guides/CONTRIBUTING.md) oku
- ✅ [Git Workflow](docs/guides/GIT_WORKFLOW.md) takip et
- ✅ Conventional Commits kullan
- ✅ Test yaz
- ✅ Code review bekle

---

## 🧪 Testing

### Unit Tests
```bash
./mvnw test
```

### Integration Tests
```bash
./mvnw verify
```

### Test Coverage
```bash
./mvnw jacoco:report
# Rapor: target/site/jacoco/index.html
```

---

## 📊 Teknoloji Stack

| Kategori | Teknoloji |
|----------|-----------|
| **Backend** | Spring Boot 3.5.4 |
| **Language** | Java 21 |
| **Database** | PostgreSQL 16 + PostGIS |
| **ORM** | Spring Data JPA / Hibernate |
| **Security** | Spring Security + JWT |
| **Migration** | Flyway |
| **Validation** | Jakarta Validation |
| **API Docs** | SpringDoc OpenAPI |
| **Build Tool** | Maven |
| **Container** | Docker & Docker Compose |

---

## 📈 Roadmap

### v1.0.0 (Current) ✅
- [x] Clean Layered Architecture
- [x] Courier authentication
- [x] Error handling
- [x] Documentation
- [x] CI/CD setup

### v1.1.0 (Next)
- [ ] Order management
- [ ] Order tracking
- [ ] Courier assignment
- [ ] Real-time updates

### v1.2.0
- [ ] Business dashboard
- [ ] Analytics
- [ ] Notifications
- [ ] Mobile API

### v2.0.0
- [ ] Microservices architecture
- [ ] Mobile app
- [ ] Advanced features
- [ ] Scale optimization

---

## 🐛 Sorun Bildirme

Bir sorun mu buldunuz? [Issue açın](https://github.com/YOUR_USERNAME/i-need-courier/issues/new/choose)

Templates:
- 🐛 [Bug Report](.github/ISSUE_TEMPLATE/bug_report.md)
- ✨ [Feature Request](.github/ISSUE_TEMPLATE/feature_request.md)

---

## 📞 İletişim

- **Issues**: [GitHub Issues](https://github.com/YOUR_USERNAME/i-need-courier/issues)
- **Discussions**: [GitHub Discussions](https://github.com/YOUR_USERNAME/i-need-courier/discussions)
- **Documentation**: [docs/](docs/)

---

## 📄 Lisans

Bu proje [MIT](LICENSE) lisansı altında lisanslanmıştır.

---

## 🙏 Teşekkürler

- Spring Boot ekibine
- PostgreSQL topluluğuna
- Açık kaynak topluluğuna

---

## 🔗 Bağlantılar

- 📚 [Tüm Dokümantasyon](docs/)
- 🚀 [Quick Start](docs/guides/QUICKSTART.md)
- 🏗️ [Architecture Guide](docs/guides/CLEAN_LAYERED_ARCHITECTURE.md)
- 🤝 [Contributing](docs/guides/CONTRIBUTING.md)
- 🌿 [Git Workflow](docs/guides/GIT_WORKFLOW.md)

---

<div align="center">

**⭐ Projeyi beğendiyseniz yıldız vermeyi unutmayın!**

Made with ❤️ using Clean Architecture

</div>

