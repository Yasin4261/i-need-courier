# Git Workflow Guide

## 🌳 Branch Strategy

We use a **simplified Git Flow** suitable for small to medium-sized Spring Boot projects.

### Main Branches

#### `main` (Protected)
- **Purpose**: Production-ready code, always deployable
- **Protection**: Requires PR with at least 1 approval and passing CI
- **Merge**: Only via Pull Request (squash or rebase merge)
- **Tagging**: All releases are tagged here (e.g., `v1.0.0`)

### Temporary Branches

#### Feature Branches: `feature/<issue>-<short-description>`
- **Purpose**: New features or enhancements
- **Base**: Always branch from `main`
- **Lifetime**: Short-lived, delete after merge
- **Examples**:
  - `feature/123-add-order-tracking`
  - `feature/45-implement-notifications`
  - `feature/789-courier-ratings`

#### Fix Branches: `fix/<issue>-<short-description>`
- **Purpose**: Bug fixes
- **Base**: Branch from `main`
- **Lifetime**: Short-lived
- **Examples**:
  - `fix/234-login-validation-error`
  - `fix/567-null-pointer-orders`

#### Hotfix Branches: `hotfix/<version>-<short-description>`
- **Purpose**: Emergency production fixes
- **Base**: Branch from `main`
- **Merge**: Back to `main` with immediate tag
- **Examples**:
  - `hotfix/1.2.1-security-patch`
  - `hotfix/1.0.3-database-connection`

#### Chore Branches: `chore/<short-description>`
- **Purpose**: Maintenance tasks, dependencies, config
- **Examples**:
  - `chore/update-dependencies`
  - `chore/improve-logging`

#### Docs Branches: `docs/<short-description>`
- **Purpose**: Documentation updates
- **Examples**:
  - `docs/update-api-guide`
  - `docs/add-architecture-diagrams`

## 🔄 Workflow Steps

### 1. Start New Work

```bash
# Update your local main
git checkout main
git pull origin main

# Create feature branch
git checkout -b feature/123-add-order-tracking

# Or use issue number from GitHub
git checkout -b feature/GH-123-add-order-tracking
```

### 2. Work on Feature

```bash
# Make changes
# Add files
git add .

# Commit with conventional commit format
git commit -m "feat(orders): add order tracking endpoint"

# Push to your fork or origin
git push -u origin feature/123-add-order-tracking
```

### 3. Keep Branch Updated

```bash
# Fetch latest changes
git fetch origin

# Option 1: Rebase (cleaner history)
git rebase origin/main

# Option 2: Merge (preserves history)
git merge origin/main

# Push (force if rebased)
git push --force-with-lease origin feature/123-add-order-tracking
```

### 4. Create Pull Request

1. Go to GitHub repository
2. Click "New Pull Request"
3. Select your branch
4. Fill out PR template:
   - Description of changes
   - Link to issue: `Closes #123`
   - Testing details
   - Screenshots if applicable
5. Request review from team members
6. Wait for CI to pass

### 5. Address Review Comments

```bash
# Make requested changes
git add .
git commit -m "fix(orders): address review comments"
git push origin feature/123-add-order-tracking
```

### 6. Merge PR

Once approved and CI passes:
- **Squash and Merge**: For multiple commits (preferred)
- **Rebase and Merge**: For clean single commits
- **Merge Commit**: For preserving history (rarely)

### 7. Cleanup

```bash
# After PR is merged, delete local branch
git checkout main
git pull origin main
git branch -d feature/123-add-order-tracking

# Delete remote branch (if not auto-deleted)
git push origin --delete feature/123-add-order-tracking
```

## 📝 Commit Message Format

We use **Conventional Commits** format:

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types

- **feat**: New feature
- **fix**: Bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, no logic change)
- **refactor**: Code refactoring (no feature or bug fix)
- **test**: Adding or updating tests
- **chore**: Maintenance tasks (dependencies, build config)
- **perf**: Performance improvements
- **ci**: CI/CD changes

### Scopes (Optional)

Common scopes in our project:
- `auth` - Authentication/Authorization
- `courier` - Courier management
- `order` - Order management
- `api` - API changes
- `db` - Database changes
- `config` - Configuration
- `security` - Security features
- `test` - Testing

### Examples

```bash
# Feature
git commit -m "feat(auth): add JWT refresh token support"

# Bug fix
git commit -m "fix(courier): resolve null pointer in status update"

# Multiple lines
git commit -m "feat(orders): implement order tracking

- Add GET /api/v1/orders/{id}/tracking endpoint
- Add OrderTrackingService
- Add unit tests
- Update API documentation

Closes #123"

# Breaking change
git commit -m "feat(api)!: change authentication response format

BREAKING CHANGE: AuthResponse now returns 'accessToken' instead of 'token'"

# Documentation
git commit -m "docs(readme): update deployment instructions"

# Chore
git commit -m "chore(deps): upgrade Spring Boot to 3.5.4"
```

## 🏷️ Release Tagging

### Semantic Versioning

We follow [Semantic Versioning](https://semver.org/): `MAJOR.MINOR.PATCH`

- **MAJOR**: Breaking changes (e.g., `2.0.0`)
- **MINOR**: New features, backward compatible (e.g., `1.3.0`)
- **PATCH**: Bug fixes, backward compatible (e.g., `1.2.1`)

### Creating a Release

```bash
# Ensure you're on main and up to date
git checkout main
git pull origin main

# Create annotated tag
git tag -a v1.2.0 -m "Release v1.2.0: Add order tracking feature"

# Push tag
git push origin v1.2.0

# Or push all tags
git push origin --tags
```

### GitHub Release

1. Go to GitHub Releases
2. Click "Draft a new release"
3. Choose tag: `v1.2.0`
4. Release title: `v1.2.0 - Order Tracking`
5. Description:
   ```markdown
   ## What's New
   - Added order tracking endpoint (#123)
   - Improved courier status updates (#124)
   
   ## Bug Fixes
   - Fixed login validation error (#125)
   
   ## Breaking Changes
   None
   ```
6. Publish release

## 🔥 Hotfix Workflow

For urgent production fixes:

```bash
# 1. Branch from main
git checkout main
git pull origin main
git checkout -b hotfix/1.2.1-security-patch

# 2. Make fix
git add .
git commit -m "fix(security): patch JWT vulnerability"

# 3. Push and create PR
git push -u origin hotfix/1.2.1-security-patch

# 4. After PR approval and merge
git checkout main
git pull origin main

# 5. Create hotfix tag
git tag -a v1.2.1 -m "Hotfix v1.2.1: Security patch"
git push origin v1.2.1

# 6. Deploy immediately
```

## 🛡️ Branch Protection Rules

Configure on GitHub:

### Main Branch Protection
- ✅ Require pull request before merging
- ✅ Require at least 1 approval
- ✅ Dismiss stale reviews when new commits are pushed
- ✅ Require status checks to pass (CI)
- ✅ Require branches to be up to date before merging
- ✅ Require conversation resolution before merging
- ✅ Do not allow bypassing the above settings
- ✅ Restrict who can push to matching branches

## 📊 Example Workflows

### Scenario 1: Adding New Feature

```bash
# 1. Create branch
git checkout main
git pull origin main
git checkout -b feature/456-add-ratings

# 2. Implement feature with multiple commits
git commit -m "feat(courier): add rating model"
git commit -m "feat(courier): add rating service"
git commit -m "feat(courier): add rating endpoints"
git commit -m "test(courier): add rating tests"

# 3. Keep updated
git fetch origin
git rebase origin/main

# 4. Push
git push origin feature/456-add-ratings

# 5. Create PR (will squash all commits into one on merge)
# 6. After merge, delete branch
```

### Scenario 2: Quick Bug Fix

```bash
# 1. Create fix branch
git checkout main
git pull origin main
git checkout -b fix/789-login-error

# 2. Fix bug
git commit -m "fix(auth): resolve login validation error"

# 3. Push and create PR
git push -u origin fix/789-login-error

# 4. After merge, done
```

### Scenario 3: Multiple People on Same Feature

```bash
# Developer 1: Creates feature branch
git checkout -b feature/111-complex-feature
git push -u origin feature/111-complex-feature

# Developer 2: Works on same feature
git fetch origin
git checkout feature/111-complex-feature
git pull origin feature/111-complex-feature

# Both developers regularly sync
git pull --rebase origin feature/111-complex-feature

# When ready, create single PR from feature branch
```

## ⚠️ Important Rules

### DO ✅
- Always branch from `main`
- Use descriptive branch names
- Write clear commit messages
- Keep branches short-lived (< 1 week)
- Update branch regularly
- Request code reviews
- Run tests before pushing
- Link PRs to issues

### DON'T ❌
- Commit directly to `main`
- Push incomplete work
- Create long-lived branches
- Force push to `main`
- Merge without review
- Ignore CI failures
- Leave branches undeleted
- Use generic commit messages like "fix bug"

## 🔍 Useful Git Commands

```bash
# Check current branch
git branch

# Check status
git status

# View commit history
git log --oneline --graph --all

# View changes
git diff

# Stash changes
git stash
git stash pop

# View remote branches
git branch -r

# Delete local branch
git branch -d feature/branch-name

# Delete remote branch
git push origin --delete feature/branch-name

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Amend last commit
git commit --amend

# Interactive rebase (squash commits)
git rebase -i HEAD~3
```

## 📚 Additional Resources

- [Git Documentation](https://git-scm.com/doc)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)

## 🤝 Need Help?

- Ask in team chat
- Check this guide
- Review existing PRs
- Consult with senior developers
-- Insert sample courier data with password hashes for testing
-- Password for all test couriers: "password123"
-- BCrypt hash: $2a$10$rQZ5VvZ5Z5Z5Z5Z5Z5Z5Z.Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z5Z

INSERT INTO couriers (name, email, phone, password_hash, status, created_at, updated_at) 
VALUES 
    ('Test Courier 1', 'courier1@test.com', '+905551111111', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'OFFLINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Test Courier 2', 'courier2@test.com', '+905552222222', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'OFFLINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Test Courier 3', 'courier3@test.com', '+905553333333', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'OFFLINE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (email) DO NOTHING;

