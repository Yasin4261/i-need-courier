#!/bin/bash

# GitHub Push Script for i-need-courier v1.2.0

echo "🚀 I Need Courier - GitHub Push Script"
echo "======================================"
echo ""

# Colors
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m'

# Check if remote exists
REMOTE_EXISTS=$(git remote -v | grep -c "origin")

if [ "$REMOTE_EXISTS" -eq 0 ]; then
    echo -e "${YELLOW}⚠️  Remote 'origin' bulunamadı!${NC}"
    echo ""
    echo "GitHub'da repo oluşturmak için:"
    echo "1. https://github.com/new adresine git"
    echo "2. Repository name: i-need-courier"
    echo "3. Description: Modern courier management system with business order management"
    echo "4. Public/Private seç"
    echo "5. README, .gitignore, license EKLEME (zaten var)"
    echo "6. Create repository tıkla"
    echo ""
    echo -e "${YELLOW}Sonra remote ekle:${NC}"
    echo "git remote add origin https://github.com/KULLANICI_ADIN/i-need-courier.git"
    echo ""
    echo -e "${YELLOW}Ya da SSH ile:${NC}"
    echo "git remote add origin git@github.com:KULLANICI_ADIN/i-need-courier.git"
    echo ""
    exit 1
fi

echo -e "${GREEN}✅ Remote 'origin' bulundu${NC}"
git remote -v
echo ""

# Check current branch
CURRENT_BRANCH=$(git branch --show-current)
echo -e "${YELLOW}📍 Current branch: $CURRENT_BRANCH${NC}"
echo ""

# Check if there are unpushed commits
UNPUSHED=$(git log origin/$CURRENT_BRANCH..$CURRENT_BRANCH --oneline 2>/dev/null | wc -l)

if [ "$UNPUSHED" -gt 0 ]; then
    echo -e "${YELLOW}📤 $UNPUSHED unpushed commit(s) found${NC}"
    echo ""
    echo "Unpushed commits:"
    git log origin/$CURRENT_BRANCH..$CURRENT_BRANCH --oneline
    echo ""
fi

# Push main branch
echo -e "${YELLOW}🚀 Pushing $CURRENT_BRANCH branch...${NC}"
git push -u origin $CURRENT_BRANCH

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Branch pushed successfully!${NC}"
    echo ""
else
    echo -e "${RED}❌ Branch push failed!${NC}"
    exit 1
fi

# Push tags
echo -e "${YELLOW}🏷️  Pushing tags...${NC}"
git push origin --tags

if [ $? -eq 0 ]; then
    echo -e "${GREEN}✅ Tags pushed successfully!${NC}"
    echo ""
else
    echo -e "${RED}❌ Tag push failed!${NC}"
    exit 1
fi

# Show pushed tags
echo -e "${GREEN}📋 Pushed tags:${NC}"
git tag -l
echo ""

# Summary
echo -e "${GREEN}======================================"
echo "✅ GitHub Push Tamamlandı!"
echo -e "======================================${NC}"
echo ""
echo "GitHub'da Release oluşturmak için:"
echo "1. https://github.com/KULLANICI_ADIN/i-need-courier/releases adresine git"
echo "2. 'Draft a new release' tıkla"
echo "3. Tag: v1.2.0 seç"
echo "4. Title: 'v1.2.0 - Business Order Management System'"
echo "5. Description: CHANGELOG.md'den v1.2.0 kısmını kopyala"
echo "6. 'Publish release' tıkla"
echo ""
echo -e "${GREEN}🎉 Tamamlandı!${NC}"

