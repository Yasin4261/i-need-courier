# Contributing to I Need Courier

Thank you for considering contributing to I Need Courier! This document outlines the process and guidelines.

## 📋 Table of Contents
- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Workflow](#development-workflow)
- [Git Workflow](#git-workflow)
- [Coding Standards](#coding-standards)
- [Commit Messages](#commit-messages)
- [Pull Request Process](#pull-request-process)

## Code of Conduct

- Be respectful and inclusive
- Focus on constructive feedback
- Accept criticism gracefully
- Prioritize the project's best interests

## Getting Started

1. **Fork the repository**
2. **Clone your fork**
   ```bash
   git clone https://github.com/YOUR_USERNAME/i-need-courier.git
   cd i-need-courier
   ```
3. **Add upstream remote**
   ```bash
   git remote add upstream https://github.com/Yasin4261/i-need-courier.git
   ```
4. **Set up development environment**
   ```bash
   docker compose up -d postgres
   ./mvnw clean install
   ```

## Development Workflow

### Branch Strategy

We use a simplified Git Flow:

- `main` - Production-ready code, always deployable
- `feature/<issue>-<short-desc>` - New features
- `fix/<issue>-<short-desc>` - Bug fixes
- `hotfix/<version>-<short-desc>` - Emergency production fixes
- `chore/<short-desc>` - Maintenance tasks
- `docs/<short-desc>` - Documentation updates

### Creating a Feature Branch

```bash
# Update your local main
git checkout main
git pull upstream main

# Create feature branch
git checkout -b feature/123-add-order-tracking
```

### Keeping Your Branch Updated

```bash
# Fetch latest changes
git fetch upstream

# Rebase on main
git rebase upstream/main

# Or merge if you prefer
git merge upstream/main
```

## Git Workflow

### 1. Create Branch
```bash
git checkout -b feature/123-description
```

### 2. Make Changes
- Write clean, tested code
- Follow coding standards
- Update documentation

### 3. Commit Changes
```bash
git add .
git commit -m "feat(auth): add JWT refresh token support"
```

### 4. Push to Your Fork
```bash
git push origin feature/123-description
```

### 5. Create Pull Request
- Go to GitHub
- Create PR from your branch to `main`
- Fill out the PR template
- Link related issues

## Coding Standards

### Java Code Style

- **Formatting**: Follow Google Java Style Guide
- **Line Length**: Maximum 120 characters
- **Indentation**: 4 spaces (no tabs)
- **Naming Conventions**:
  - Classes: `PascalCase`
  - Methods/Variables: `camelCase`
  - Constants: `UPPER_SNAKE_CASE`
  - Packages: `lowercase`

### Clean Layered Architecture

Our project follows Clean Layered Architecture:

```
src/main/java/com/api/demo/
├── controller/           # REST controllers (Presentation Layer)
├── service/             # Business logic (Application Layer)
├── repository/          # Data access (Persistence Layer)
├── model/              # Domain entities
├── dto/                # Data Transfer Objects
├── mapper/             # Entity-DTO mappers
├── exception/          # Custom exceptions
├── config/             # Configuration classes
└── security/           # Security components
```

### Layer Responsibilities

1. **Controller Layer** (`controller/`)
   - Handle HTTP requests/responses
   - Validate input
   - Call service methods
   - Return appropriate HTTP status codes

2. **Service Layer** (`service/`)
   - Business logic implementation
   - Transaction management
   - Orchestrate repository calls
   - Apply business rules

3. **Repository Layer** (`repository/`)
   - Data access logic
   - Database operations
   - Query implementation

4. **Model Layer** (`model/`)
   - Domain entities
   - Business objects
   - JPA entities

### Example Structure

```java
// Controller
@RestController
@RequestMapping("/api/v1/couriers")
public class CourierController {
    private final CourierService courierService;
    
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<CourierResponse>> register(
        @Valid @RequestBody CourierRegistrationRequest request) {
        // ...
    }
}

// Service
@Service
public class CourierService {
    private final CourierRepository courierRepository;
    private final PasswordEncoder passwordEncoder;
    
    public CourierResponse register(CourierRegistrationRequest request) {
        // Business logic here
    }
}

// Repository
public interface CourierRepository extends JpaRepository<Courier, Long> {
    Optional<Courier> findByEmail(String email);
}
```

### Code Quality

- Write unit tests for all services
- Write integration tests for controllers
- Maintain test coverage > 80%
- Use meaningful variable names
- Add comments for complex logic
- Avoid code duplication
- Keep methods small and focused

## Commit Messages

We follow [Conventional Commits](https://www.conventionalcommits.org/):

### Format
```
<type>(<scope>): <subject>

<body>

<footer>
```

### Types
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting)
- `refactor`: Code refactoring
- `test`: Adding/updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements
- `ci`: CI/CD changes

### Examples
```bash
feat(auth): add JWT refresh token support

Implement refresh token mechanism for extended sessions.
Tokens expire after 7 days and can be renewed.

Closes #123
```

```bash
fix(courier): resolve null pointer in status update

Added null check before updating courier status.
Added unit tests to prevent regression.

Fixes #456
```

```bash
docs(readme): update deployment instructions

Added Docker Compose setup steps.
Updated environment variables section.
```

### Scope Examples
- `auth` - Authentication/Authorization
- `courier` - Courier management
- `order` - Order management
- `api` - API changes
- `db` - Database changes
- `config` - Configuration
- `security` - Security features

## Pull Request Process

### Before Creating PR

1. **Update your branch**
   ```bash
   git fetch upstream
   git rebase upstream/main
   ```

2. **Run tests**
   ```bash
   ./mvnw clean test
   ```

3. **Check code quality**
   ```bash
   ./mvnw checkstyle:check
   ```

4. **Build project**
   ```bash
   ./mvnw clean install
   ```

### Creating PR

1. Push to your fork
2. Go to GitHub and create PR
3. Fill out the PR template completely
4. Link related issues (use "Closes #123")
5. Request review from maintainers

### PR Requirements

- ✅ All tests pass
- ✅ Code follows style guidelines
- ✅ Documentation updated
- ✅ No merge conflicts
- ✅ Meaningful commit messages
- ✅ PR description is complete
- ✅ Related issues linked

### PR Review Process

1. **Automated checks** - CI pipeline runs
2. **Code review** - At least 1 approval required
3. **Changes requested** - Address feedback
4. **Approval** - Maintainer approves
5. **Merge** - Squash and merge to main

### After PR is Merged

1. Delete your feature branch
   ```bash
   git branch -d feature/123-description
   git push origin --delete feature/123-description
   ```

2. Update your main
   ```bash
   git checkout main
   git pull upstream main
   ```

## Testing Guidelines

### Unit Tests
- Test individual methods
- Mock dependencies
- Use JUnit 5 and Mockito
- Follow naming: `methodName_scenario_expectedResult`

```java
@Test
void register_withValidData_shouldCreateCourier() {
    // Arrange
    CourierRegistrationRequest request = new CourierRegistrationRequest(/*...*/);
    
    // Act
    CourierResponse response = courierService.register(request);
    
    // Assert
    assertNotNull(response.getCourierId());
    assertEquals(request.getEmail(), response.getEmail());
}
```

### Integration Tests
- Test full request-response cycle
- Use `@SpringBootTest`
- Test database interactions
- Use test containers if needed

## Database Changes

### Creating Migrations

1. Create new migration file in `src/main/resources/db/migration/`
2. Follow naming: `V{version}__{description}.sql`
3. Example: `V7__Add_order_tracking_table.sql`

```sql
-- V7__Add_order_tracking_table.sql
CREATE TABLE order_tracking (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

4. Test migration locally
5. Include migration in PR

## Questions?

- Create an issue with `question` label
- Join discussions in existing issues
- Contact maintainers

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

