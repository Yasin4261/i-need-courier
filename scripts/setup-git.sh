

#!/bin/bash

# I Need Courier - Git Repository Setup Script
# Prepares the project for first commit and push to GitHub

set -e

echo "🚀 Setting up Git repository for I Need Courier..."
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Check if git is installed
if ! command -v git &> /dev/null; then
    echo -e "${RED}❌ Git is not installed. Please install Git first.${NC}"
    exit 1
fi

# Step 1: Initialize git repository (if not already)
echo -e "${YELLOW}📦 Step 1: Initializing Git repository...${NC}"
if [ ! -d ".git" ]; then
    git init
    echo -e "${GREEN}✅ Git repository initialized${NC}"
else
    echo -e "${GREEN}✅ Git repository already exists${NC}"
fi

# Step 2: Check for GitHub remote
echo ""
echo -e "${YELLOW}🔗 Step 2: Checking GitHub remote...${NC}"
if git remote | grep -q "origin"; then
    REMOTE_URL=$(git remote get-url origin)
    echo -e "${GREEN}✅ Remote 'origin' already set: ${REMOTE_URL}${NC}"
else
    echo -e "${BLUE}ℹ️  No remote 'origin' found.${NC}"
    echo -e "${YELLOW}Please enter your GitHub repository URL:${NC}"
    echo -e "${BLUE}Example: https://github.com/username/i-need-courier.git${NC}"
    read -p "URL: " REPO_URL

    if [ -n "$REPO_URL" ]; then
        git remote add origin "$REPO_URL"
        echo -e "${GREEN}✅ Remote 'origin' added: ${REPO_URL}${NC}"
    else
        echo -e "${RED}❌ No URL provided. Skipping remote setup.${NC}"
    fi
fi

# Step 3: Check current branch
echo ""
echo -e "${YELLOW}🌿 Step 3: Checking branch...${NC}"
CURRENT_BRANCH=$(git branch --show-current 2>/dev/null || echo "")
if [ -z "$CURRENT_BRANCH" ]; then
    git checkout -b main
    echo -e "${GREEN}✅ Created and switched to 'main' branch${NC}"
elif [ "$CURRENT_BRANCH" != "main" ]; then
    echo -e "${YELLOW}⚠️  Currently on branch '${CURRENT_BRANCH}'${NC}"
    read -p "Switch to 'main' branch? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        git checkout -b main 2>/dev/null || git checkout main
        echo -e "${GREEN}✅ Switched to 'main' branch${NC}"
    fi
else
    echo -e "${GREEN}✅ Already on 'main' branch${NC}"
fi

# Step 4: Show status
echo ""
echo -e "${YELLOW}📊 Step 4: Git status...${NC}"
git status --short

# Step 5: Stage files
echo ""
echo -e "${YELLOW}📝 Step 5: Staging files...${NC}"
git add .
echo -e "${GREEN}✅ All files staged${NC}"

# Step 6: Create initial commit
echo ""
echo -e "${YELLOW}💾 Step 6: Creating initial commit...${NC}"
if git diff --cached --quiet; then
    echo -e "${BLUE}ℹ️  No changes to commit. Repository is up to date.${NC}"
else
    COMMIT_MESSAGE="feat: migrate to clean layered architecture

- Removed hexagonal architecture (ports/adapters)
- Implemented clean layered architecture (controller/service/repository/model)
- Added comprehensive documentation (CONTRIBUTING.md, GIT_WORKFLOW.md, etc.)
- Set up GitHub workflows and templates
- Added error handling with ErrorResponse
- Created startup scripts (start.sh, stop.sh)
- Updated database migrations
- Added unit tests structure

BREAKING CHANGE: Complete architecture refactor from hexagonal to clean layered architecture.

This commit represents v1.0.0 - Initial release with clean architecture migration.
"

    git commit -m "$COMMIT_MESSAGE"
    echo -e "${GREEN}✅ Initial commit created${NC}"
fi

# Step 7: Create and push tag
echo ""
echo -e "${YELLOW}🏷️  Step 7: Creating version tag...${NC}"
if git tag | grep -q "v1.0.0"; then
    echo -e "${BLUE}ℹ️  Tag v1.0.0 already exists${NC}"
else
    TAG_MESSAGE="Release v1.0.0: Clean Layered Architecture Migration

- Complete architecture refactor to clean layered architecture
- Courier authentication system (register/login)
- JWT token implementation
- Error handling with detailed responses
- Comprehensive documentation
- GitHub workflow setup
- CI/CD pipeline
- Branch strategy and contribution guidelines

Breaking Changes:
- Complete restructure of package organization
- New API response format with ErrorResponse
- Updated error handling

Features:
- Courier registration endpoint
- Courier login with JWT
- Validation with detailed error messages
- Global exception handling
- Actuator health checks
- Swagger/OpenAPI documentation
- Docker support

Documentation:
- CLEAN_LAYERED_ARCHITECTURE.md - Architecture guide
- GIT_WORKFLOW.md - Git workflow and branching
- CONTRIBUTING.md - Contribution guidelines
- GITHUB_SETUP_COMPLETE.md - GitHub setup guide
- QUICKSTART.md - Quick start guide
- MIGRATION_TO_CLEAN_ARCHITECTURE.md - Migration details
"

    git tag -a v1.0.0 -m "$TAG_MESSAGE"
    echo -e "${GREEN}✅ Tag v1.0.0 created${NC}"
fi

# Step 8: Push to remote
echo ""
echo -e "${YELLOW}🚀 Step 8: Pushing to GitHub...${NC}"
if git remote | grep -q "origin"; then
    read -p "Push to GitHub? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Pushing main branch..."
        git push -u origin main

        echo "Pushing tags..."
        git push origin --tags

        echo -e "${GREEN}✅ Successfully pushed to GitHub!${NC}"
    else
        echo -e "${YELLOW}⚠️  Skipped pushing to GitHub${NC}"
        echo -e "${BLUE}You can push manually later with:${NC}"
        echo "  git push -u origin main"
        echo "  git push origin --tags"
    fi
else
    echo -e "${RED}❌ No remote 'origin' configured. Skipping push.${NC}"
    echo -e "${BLUE}Add remote and push manually:${NC}"
    echo "  git remote add origin YOUR_REPO_URL"
    echo "  git push -u origin main"
    echo "  git push origin --tags"
fi

# Summary
echo ""
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo -e "${GREEN}🎉 Git Setup Complete!${NC}"
echo -e "${GREEN}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${NC}"
echo ""
echo -e "${BLUE}📌 Next Steps:${NC}"
echo ""
echo "1. Go to GitHub and set up branch protection rules for 'main'"
echo "   Settings → Branches → Add rule"
echo ""
echo "2. Enable GitHub Actions"
echo "   Actions → Enable workflows"
echo ""
echo "3. Add team members"
echo "   Settings → Collaborators → Add people"
echo ""
echo "4. Create your first issue"
echo "   Issues → New issue → Choose template"
echo ""
echo "5. Start working on features"
echo "   git checkout -b feature/YOUR_FEATURE"
echo ""
echo "  - GITHUB_SETUP_COMPLETE.md - Complete GitHub guide"
echo "  - GIT_WORKFLOW.md - Daily Git workflow"
echo "  - CONTRIBUTING.md - How to contribute"
echo ""
echo -e "${GREEN}Happy coding! 🚀${NC}"
echo -e "${BLUE}📚 Documentation:${NC}"
Major Changes:
# 🎯 GitHub Setup Complete - Ready for Version Control

## ✅ Tamamlanan GitHub İşlemleri

### 1. Branch Strategy (Git Flow)
✅ **Branch yapısı tasarlandı**:
- `main` - Production branch (korunmuş)
- `feature/<issue>-<description>` - Yeni özellikler
- `fix/<issue>-<description>` - Bug düzeltmeleri
- `hotfix/<version>-<description>` - Acil düzeltmeler
- `chore/<description>` - Bakım işleri
- `docs/<description>` - Dokümantasyon

### 2. Git Workflow Dokümantasyonu
✅ **Dosya**: `GIT_WORKFLOW.md`
- Detaylı branch stratejisi
- Commit message conventions (Conventional Commits)
- Pull Request süreci
- Code review guidelines
- Release ve tagging stratejisi

### 3. GitHub Templates
✅ **Pull Request Template**: `.github/PULL_REQUEST_TEMPLATE.md`
```markdown
- Description of changes
- Type of change (bug fix, feature, etc.)
- Testing details
- Checklist for reviewers
```

✅ **Issue Templates**:
- `.github/ISSUE_TEMPLATE/bug_report.md` - Bug raporlama
- `.github/ISSUE_TEMPLATE/feature_request.md` - Özellik talebi

### 4. CI/CD Pipeline
✅ **Dosya**: `.github/workflows/ci.yml`
- Otomatik build ve test
- PostgreSQL test database
- Docker image oluşturma
- Security scanning (Trivy)
- Pull Request'lerde otomatik çalışır

### 5. Contributing Guide
✅ **Dosya**: `CONTRIBUTING.md`
- Geliştirme workflow'u
- Kod standartları
- Test gereksinimleri
- Commit message formatı
- Pull Request süreci

### 6. .gitignore
✅ **Dosya**: `.gitignore`
- Java/Maven dosyaları
- IDE dosyaları (IntelliJ, Eclipse, VS Code)
- Log dosyaları
- Environment dosyaları
- Docker ve temporary dosyalar

---

## 🚀 Git'e Gönderme Adımları

### İlk Commit ve Push

```bash
# 1. Git repository başlat (eğer yoksa)
git init

# 2. Remote repository ekle
git remote add origin https://github.com/YOUR_USERNAME/i-need-courier.git

# 3. Main branch oluştur ve dosyaları ekle
git checkout -b main
git add .

# 4. İlk commit (Conventional Commits formatında)
git commit -m "feat: migrate to clean layered architecture

- Removed hexagonal architecture (ports/adapters)
- Implemented clean layered architecture (controller/service/repository/model)
- Added comprehensive documentation
- Set up GitHub workflows and templates
- Added error handling with ErrorResponse
- Created startup scripts
- Updated database migrations

BREAKING CHANGE: Complete architecture refactor
"

# 5. Remote'a push et
git push -u origin main
```

### Branch Protection Rules (GitHub'da Ayarla)

**Settings → Branches → Branch Protection Rules → Add Rule**

`main` branch için:
- ✅ Require a pull request before merging
- ✅ Require approvals (minimum 1)
- ✅ Dismiss stale pull request approvals
- ✅ Require status checks to pass before merging
- ✅ Require branches to be up to date
- ✅ Require conversation resolution before merging
- ✅ Do not allow bypassing the above settings

---

## 📋 Version Tagging Strategy

### Semantic Versioning
Format: `vMAJOR.MINOR.PATCH`

**İlk Release (v1.0.0)**:
```bash
git tag -a v1.0.0 -m "Release v1.0.0: Clean Layered Architecture Migration

Major Changes:
- Complete architecture refactor to clean layered architecture
- Courier authentication system (register/login)
- JWT token implementation
- Error handling with detailed responses
- Comprehensive documentation
- GitHub workflow setup
"

git push origin v1.0.0
```

### Version Bump Examples

**Patch (v1.0.1)** - Bug fixes:
```bash
git tag -a v1.0.1 -m "Release v1.0.1: Bug fixes

- Fixed validation error response format
- Corrected JWT token expiration
- Updated error messages
"
git push origin v1.0.1
```

**Minor (v1.1.0)** - New features:
```bash
git tag -a v1.1.0 -m "Release v1.1.0: Order Management

- Added order creation endpoint
- Implemented order tracking
- Added courier-order assignment
"
git push origin v1.1.0
```

**Major (v2.0.0)** - Breaking changes:
```bash
git tag -a v2.0.0 -m "Release v2.0.0: API v2

BREAKING CHANGES:
- Changed authentication flow
- Updated API endpoints structure
- Modified response format
"
git push origin v2.0.0
```

---

## 🌿 Daily Workflow Örneği

### Senaryo: Yeni Özellik Ekleme

```bash
# 1. Ana branch'i güncelle
git checkout main
git pull origin main

# 2. Issue numarasıyla feature branch oluştur
git checkout -b feature/123-add-order-tracking

# 3. Değişiklikleri yap ve commit et
git add .
git commit -m "feat(orders): add order tracking endpoint

- Added OrderController
- Implemented OrderService
- Created Order entity and repository
- Added unit tests

Closes #123
"

# 4. Remote'a push et
git push -u origin feature/123-add-order-tracking

# 5. GitHub'da Pull Request oluştur
# PR title: "feat(orders): Add order tracking endpoint"
# PR description: Template'i doldur, #123 issue'suna link ver

# 6. Code review sonrası merge
# GitHub'da "Squash and merge" kullan

# 7. Local branch'i temizle
git checkout main
git pull origin main
git branch -d feature/123-add-order-tracking
```

---

## 📊 GitHub Actions Workflow

### CI Pipeline (Otomatik Çalışır)
Her PR ve push'ta:
1. ✅ Java 21 kurulumu
2. ✅ Maven build
3. ✅ Unit testler
4. ✅ Test coverage raporu
5. ✅ Docker image build
6. ✅ Security scanning

**Başarısız olursa**: PR merge edilemez! ❌

---

## 🏷️ Commit Message Örnekleri

### Conventional Commits Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Örnekler

**Feature**:
```bash
git commit -m "feat(auth): add password reset functionality"
```

**Bug Fix**:
```bash
git commit -m "fix(courier): resolve null pointer in status update

Added null check before accessing courier status.
Added unit test to prevent regression.

Fixes #456
"
```

**Documentation**:
```bash
git commit -m "docs(api): update authentication endpoints documentation"
```

**Refactor**:
```bash
git commit -m "refactor(service): simplify courier registration logic

- Extracted validation to separate method
- Improved error messages
- Added logging
"
```

**Breaking Change**:
```bash
git commit -m "feat(api)!: change authentication response format

BREAKING CHANGE: AuthResponse now returns 'accessToken'
instead of 'token' field.

Migration guide:
- Update client code to use 'accessToken'
- Old 'token' field is deprecated
"
```

---

## 🔒 Security Best Practices

### Secrets Management
❌ **ASLA commit etme**:
- `.env` files
- `application-local.properties`
- Database passwords
- API keys
- JWT secrets

✅ **GitHub Secrets kullan**:
```
Settings → Secrets and variables → Actions → New repository secret
```

### Environment Variables
```bash
# Local development (.env - .gitignore'da)
DATABASE_URL=jdbc:postgresql://localhost:5432/courier_db
JWT_SECRET=your-secret-key
```

---

## 📈 Project Milestones

### v1.0.0 - Architecture Migration ✅
- [x] Clean Layered Architecture
- [x] Authentication system
- [x] Documentation
- [x] GitHub setup

### v1.1.0 - Order Management (Planned)
- [ ] Order creation
- [ ] Order tracking
- [ ] Courier assignment
- [ ] Real-time updates

### v1.2.0 - Business Features (Planned)
- [ ] Business registration
- [ ] Business dashboard
- [ ] Order management UI
- [ ] Analytics

### v2.0.0 - Mobile & Scale (Future)
- [ ] Mobile app integration
- [ ] Microservices split
- [ ] Performance optimization
- [ ] Advanced features

---

## 🎓 Team Onboarding Checklist

### Yeni Geliştirici İçin
- [ ] Repository'yi clone et
- [ ] `CONTRIBUTING.md` oku
- [ ] `GIT_WORKFLOW.md` oku
- [ ] `CLEAN_LAYERED_ARCHITECTURE.md` oku
- [ ] Development environment kur (`./start.sh`)
- [ ] İlk issue'yu al ve feature branch oluştur
- [ ] İlk PR'ı gönder
- [ ] Code review sürecine katıl

---

## 📞 Yardım ve Destek

### Sorular?
1. `CONTRIBUTING.md` kontrol et
2. `GIT_WORKFLOW.md` oku
3. Mevcut issue'ları incele
4. Yeni issue oluştur (template kullan)
5. Team chat'te sor

### Problem mi var?
1. Logs kontrol et: `tail -f logs/app.log`
2. GitHub Actions logs kontrol et
3. Issue oluştur (bug template)
4. Stack Overflow'da ara
5. Team'e danış

---

## ✅ GitHub Setup Özeti

| Öğe | Durum | Dosya |
|-----|-------|-------|
| Branch Strategy | ✅ | GIT_WORKFLOW.md |
| PR Template | ✅ | .github/PULL_REQUEST_TEMPLATE.md |
| Issue Templates | ✅ | .github/ISSUE_TEMPLATE/ |
| CI/CD Pipeline | ✅ | .github/workflows/ci.yml |
| Contributing Guide | ✅ | CONTRIBUTING.md |
| .gitignore | ✅ | .gitignore |
| Architecture Docs | ✅ | documents/ |
| Version Strategy | ✅ | Bu belge |

---

## 🎉 Sonuç

**GitHub için her şey hazır!** 🚀

### Hemen yapılacaklar:
1. ✅ Repository oluştur (GitHub)
2. ✅ Remote ekle
3. ✅ İlk commit ve push
4. ✅ Branch protection rules ayarla
5. ✅ v1.0.0 tag'i oluştur
6. ✅ GitHub Actions'ı kontrol et
7. ✅ Team'i davet et

### Sürekli yapılacaklar:
- Feature branch'lerle çalış
- PR template'i kullan
- Conventional commits yaz
- Code review yap
- Düzenli release'ler yap

**Profesyonel Git workflow'u kuruldu! 🎯**

---

*Son Güncelleme: 31 Ekim 2025*
*Hazırlayan: GitHub Copilot AI Assistant*

