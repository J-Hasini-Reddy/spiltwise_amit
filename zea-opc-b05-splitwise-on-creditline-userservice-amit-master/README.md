# User Service

> **Splitwise on CreditLine** — A Zeta Academy Project  
> **Author:** ramit@zeta.tech  
> **Version:** 0.0.1-SNAPSHOT

---

## Table of Contents

1. [Overview](#overview)
2. [High-Level Design (HLD)](#high-level-design-hld)
   - [System Context](#system-context)
   - [Architecture Diagram](#architecture-diagram)
   - [Design Principles](#design-principles)
   - [Key Design Decisions](#key-design-decisions)
3. [Low-Level Design (LLD)](#low-level-design-lld)
   - [Domain Model](#domain-model)
   - [Database Schema](#database-schema)
   - [API Specification](#api-specification)
   - [Service Layer Design](#service-layer-design)
4. [Data Flow](#data-flow)
5. [Error Handling](#error-handling)
6. [Cipher/Sandbox Authorization](#ciphersandbox-authorization)
7. [Scalability Considerations](#scalability-considerations)
8. [Trade-offs & Decisions](#trade-offs--decisions)
9. [Running Locally](#running-locally)
10. [Configuration](#configuration)

---

## Overview

The **User Service** is the identity and profile management layer of the Splitwise-on-CreditLine system. It manages user registration, profile information, and most importantly, the **global credit limit** for each user. This service acts as the source of truth for user data and is consumed by both Pool Service (for user validation) and CreditLine Service (for credit limit enforcement).

### Core Responsibilities

- **User Registration**: Create new user accounts with profile information
- **Profile Management**: Query and update user profiles
- **Global Credit Limit**: Define and manage the maximum credit a user can utilize across all pools
- **User Validation**: Provide user existence and status checks for other services
- **Status Management**: Track user account status (ACTIVE/INACTIVE)

---

## High-Level Design (HLD)

### System Context

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              EXTERNAL CLIENTS                                │
│                         (Mobile Apps, Web Frontend)                          │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
                                      ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                               API GATEWAY                                    │
│                    (Authentication, Rate Limiting, Routing)                  │
└─────────────────────────────────────────────────────────────────────────────┘
                                      │
            ┌─────────────────────────┼─────────────────────────┐
            ▼                         │                         ▼
┌───────────────────┐                 │             ┌───────────────────┐
│   USER SERVICE    │◀────────────────┼─────────────│   POOL SERVICE    │
│   (This Service)  │                 │             │                   │
│                   │                 │             │  Validates users  │
│  • Registration   │                 │             │  before adding    │
│  • Profile Mgmt   │                 │             │  to pools         │
│  • Global Limits  │◀────────────────┼─────────────┤                   │
│  • User Status    │                 │             └───────────────────┘
└───────────────────┘                 │
         │                            │
         │                            ▼
         │                  ┌───────────────────┐
         │                  │ CREDITLINE SERVICE│
         │                  │                   │
         │                  │  Uses global limit│
         │                  │  for credit calc  │
         │                  └───────────────────┘
         │
         ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                        GANYMEDE (PostgreSQL)                                 │
│                      Relational Data Store                                   │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            USER SERVICE                                      │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         CONTROLLER LAYER                            │    │
│  │  ┌─────────────────────────────────────────────────────────────┐   │    │
│  │  │                    UserController                           │   │    │
│  │  │                                                             │   │    │
│  │  │  POST   /api/v1/user/register                               │   │    │
│  │  │  GET    /api/v1/user/profile/userId/{userId}                │   │    │
│  │  │  GET    /api/v1/user/profile/email/{email}                  │   │    │
│  │  │  PUT    /api/v1/user/update/{userId}                        │   │    │
│  │  │  PUT    /api/v1/user/update/{userId}/globalCreditLimit/{n}  │   │    │
│  │  └─────────────────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                      │                                       │
│                                      ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                          SERVICE LAYER                              │    │
│  │  ┌─────────────────────────────────────────────────────────────┐   │    │
│  │  │                     UserService                             │   │    │
│  │  │                                                             │   │    │
│  │  │  • registerUser(RegisterRequest)                            │   │    │
│  │  │  • getUserById(Long userId)                                 │   │    │
│  │  │  • getUserByEmail(String email)                             │   │    │
│  │  │  • updateUser(Long userId, RegisterRequest)                 │   │    │
│  │  │  • updateUserGlobalLimit(Long userId, Double newLimit)      │   │    │
│  │  └─────────────────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                      │                                       │
│                                      ▼                                       │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                        REPOSITORY LAYER                             │    │
│  │  ┌─────────────────────────────────────────────────────────────┐   │    │
│  │  │                    UserRepository (JPA)                     │   │    │
│  │  │                                                             │   │    │
│  │  │  • findById(Long id)                                        │   │    │
│  │  │  • findByEmail(String email)                                │   │    │
│  │  │  • existsByEmail(String email)                              │   │    │
│  │  │  • existsByPhone(String phone)                              │   │    │
│  │  │  • save(User user)                                          │   │    │
│  │  └─────────────────────────────────────────────────────────────┘   │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Design Principles

| Principle | Implementation |
|-----------|----------------|
| **Single Responsibility** | User domain only — no pool/credit logic |
| **Stateless Service** | No session state; all data in database |
| **Soft Delete Pattern** | Users marked INACTIVE, never hard deleted |
| **Unique Constraints** | Email and phone are unique identifiers |
| **Validation First** | Input validation via `@Validated` annotations |

### Key Design Decisions

1. **Global Credit Limit**
   - Each user has a `globalCreditLimit` that caps their total credit across ALL pools
   - This is different from per-pool credit limits
   - Used by CreditLine service to calculate available credit
   - Can be updated independently of other profile fields

2. **User Status Management**
   - Users have `ACTIVE` / `INACTIVE` status
   - Inactive users cannot be added to pools
   - Soft delete preserves user data for audit

3. **Unique Email and Phone**
   - Email and phone are unique identifiers
   - Prevents duplicate accounts
   - Enables login via email or phone

4. **Synchronous API Design**
   - Simple CRUD operations
   - No async processing needed
   - Direct database calls for consistency

---

## Low-Level Design (LLD)

### Domain Model

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                            DOMAIN ENTITIES                                   │
├─────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                           User                                      │    │
│  ├─────────────────────────────────────────────────────────────────────┤    │
│  │  userId: Long (PK, Auto-generated)                                  │    │
│  │  name: String (required, max 200 chars)                             │    │
│  │  email: String (unique, max 254 chars)                              │    │
│  │  phone: String (unique, max 32 chars)                               │    │
│  │  globalCreditLimit: Double (required)                               │    │
│  │  status: UserStatus (enum: ACTIVE, INACTIVE)                        │    │
│  │  createdAt: LocalDateTime (auto-set on create)                      │    │
│  │  updatedAt: LocalDateTime (auto-set on update)                      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                         ENUMS                                       │    │
│  ├─────────────────────────────────────────────────────────────────────┤    │
│  │  UserStatus: ACTIVE | INACTIVE                                      │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
│  ┌─────────────────────────────────────────────────────────────────────┐    │
│  │                      JPA Lifecycle Hooks                            │    │
│  ├─────────────────────────────────────────────────────────────────────┤    │
│  │  @PrePersist:                                                       │    │
│  │    - createdAt = LocalDateTime.now()                                │    │
│  │    - updatedAt = LocalDateTime.now()                                │    │
│  │    - status = ACTIVE (if null)                                      │    │
│  │                                                                     │    │
│  │  @PreUpdate:                                                        │    │
│  │    - updatedAt = LocalDateTime.now()                                │    │
│  └─────────────────────────────────────────────────────────────────────┘    │
│                                                                              │
└─────────────────────────────────────────────────────────────────────────────┘
```

### Database Schema

```sql
-- Users Table
CREATE TABLE users (
    user_id             BIGSERIAL PRIMARY KEY,
    name                VARCHAR(200) NOT NULL,
    email               VARCHAR(254) UNIQUE,
    phone               VARCHAR(32) UNIQUE,
    global_credit_limit DOUBLE PRECISION NOT NULL,
    status              VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes for faster lookups
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_users_phone ON users(phone);
CREATE INDEX idx_users_status ON users(status);
```

### API Specification

#### User Management APIs

| Method | Endpoint | Description | Request Body |
|--------|----------|-------------|--------------|
| `POST` | `/api/v1/user/register` | Register new user | `RegisterRequest` |
| `GET` | `/api/v1/user/profile/userId/{userId}` | Get user by ID | - |
| `GET` | `/api/v1/user/profile/email/{email}` | Get user by email | - |
| `PUT` | `/api/v1/user/update/{userId}` | Update user profile | `RegisterRequest` |
| `PUT` | `/api/v1/user/update/{userId}/globalCreditLimit/{newLimit}` | Update global limit | - |

#### Request/Response DTOs

**RegisterRequest (Create/Update User):**
```json
{
  "name": "Ramit Sharma",
  "email": "ramit@zeta.tech",
  "phone": "+91-9876543210",
  "globalCreditLimit": 100000.00
}
```

**UserProfileResponse:**
```json
{
  "userId": 1,
  "name": "Ramit Sharma",
  "email": "ramit@zeta.tech",
  "phone": "+91-9876543210",
  "globalCreditLimit": 100000.00,
  "status": "ACTIVE",
  "createdAt": "2026-01-10T10:00:00",
  "updatedAt": "2026-01-10T10:00:00"
}
```

### Service Layer Design

#### UserService

```java
@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    
    public UserProfileResponse registerUser(RegisterRequest request) {
        // 1. Validate email uniqueness
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException("Email already registered");
        }
        
        // 2. Validate phone uniqueness
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new UserAlreadyExistsException("Phone already registered");
        }
        
        // 3. Create user entity
        User user = new User(
            request.getName(),
            request.getEmail(),
            request.getPhone(),
            request.getGlobalCreditLimit()
        );
        
        // 4. Save and return
        User saved = userRepository.save(user);
        return toUserProfileResponse(saved);
    }
    
    public UserProfileResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        return toUserProfileResponse(user);
    }
    
    public UserProfileResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + email));
        return toUserProfileResponse(user);
    }
    
    public UserProfileResponse updateUser(Long userId, RegisterRequest request) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        // Update fields
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setGlobalCreditLimit(request.getGlobalCreditLimit());
        
        User updated = userRepository.save(user);
        return toUserProfileResponse(updated);
    }
    
    public UserProfileResponse updateUserGlobalLimit(Long userId, Double newLimit) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new UserNotFoundException("User not found: " + userId));
        
        user.setGlobalCreditLimit(newLimit);
        User updated = userRepository.save(user);
        return toUserProfileResponse(updated);
    }
    
    private UserProfileResponse toUserProfileResponse(User user) {
        return UserProfileResponse.builder()
            .userId(user.getUserId())
            .name(user.getName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .globalCreditLimit(user.getGlobalCreditLimit())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .build();
    }
}
```

---

## Data Flow

### User Registration Flow

```
┌───────────┐    ┌───────────────────┐    ┌───────────────────┐
│  Client   │───▶│   UserService     │───▶│    Database       │
│           │    │                   │    │                   │
│ Register  │    │ 1. Validate email │    │                   │
│ Request   │    │    uniqueness     │◀───│ 2. Check exists   │
│           │    │ 3. Validate phone │    │                   │
│           │    │    uniqueness     │◀───│ 4. Check exists   │
│           │    │ 5. Create user    │───▶│ 6. INSERT user    │
│           │◀───│ 7. Return profile │◀───│ 8. Return saved   │
└───────────┘    └───────────────────┘    └───────────────────┘
```

### Global Credit Limit Usage Flow

```
┌───────────┐    ┌───────────────────┐    ┌───────────────────┐    ┌───────────────────┐
│ PoolSvc   │───▶│    UserService    │    │                   │    │ CreditLineSvc     │
│           │    │                   │    │                   │    │                   │
│ Validate  │    │ 1. Get user by ID │    │                   │    │                   │
│ User      │◀───│ 2. Return profile │    │                   │    │                   │
│           │    │    with globalLimit    │                   │    │                   │
│           │    │                   │    │                   │    │                   │
│ Create    │    │                   │    │                   │    │ 3. Receive pool   │
│ Pool      │───────────────────────────────────────────────────▶│    created event  │
│           │    │                   │    │                   │    │ 4. Use globalLimit│
│           │    │                   │    │                   │    │    for credit calc│
└───────────┘    └───────────────────┘    └───────────────────┘    └───────────────────┘
```

### Global Credit Limit Formula

```
Per-Pool Credit Limit = min(Pool's Requested Limit, User's Global Credit Limit / Number of User's Active Pools)

Example:
  User's Global Credit Limit: ₹100,000
  User is in 2 active pools
  Pool A requested limit: ₹50,000
  ─────────────────────────────────────
  Available per pool: ₹100,000 / 2 = ₹50,000
  Pool A credit limit: min(₹50,000, ₹50,000) = ₹50,000
```

---

## Error Handling

| Exception | HTTP Status | Description |
|-----------|-------------|-------------|
| `UserNotFoundException` | 404 | User does not exist |
| `UserAlreadyExistsException` | 409 | Email or phone already registered |
| `InvalidRequestException` | 400 | Invalid input data |
| `UserServiceException` | 500 | Internal service error |

### Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ErrorResponse(
            "USER_NOT_FOUND",
            ex.getMessage(),
            404
        ));
    }
    
    @ExceptionHandler(UserAlreadyExistsException.class)
    public ResponseEntity<ErrorResponse> handleUserExists(UserAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse(
            "USER_EXISTS",
            ex.getMessage(),
            409
        ));
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .collect(Collectors.joining(", "));
        
        return ResponseEntity.badRequest().body(new ErrorResponse(
            "VALIDATION_ERROR",
            message,
            400
        ));
    }
}
```

---

## Cipher/Sandbox Authorization

The User Service integrates with **Zeta Cipher/Sandbox** for fine-grained API authorization. This provides role-based access control (RBAC) at the endpoint level.

### Overview

Cipher is Zeta's authorization framework that evaluates access policies based on:
- **Action**: The operation being performed (e.g., `user.update`, `user.read`)
- **Object**: The resource being accessed (e.g., `SplitwiseAmitCipherUser`)
- **Tenant**: The tenant context (`1001034`)

### Protected Endpoints

| Endpoint | Action | Object Type |
|----------|--------|-------------|
| `PUT /user/update/{userId}/globalCreditLimit/{limit}` | `user.update` | `SplitwiseAmitCipherUser` |

### Implementation

```java
@Component
public class UserProvider implements ObjectProvider<SandboxUser> {
    public static final String OBJECT_TYPE = "SplitwiseAmitCipherUser";

    @Override
    public CompletionStage<Optional<SandboxUser>> getObject(JID jid, Realm realm, Long tenantID) {
        // Provides user object for authorization evaluation
        SandboxUser user = SandboxUser.builder()
                .userId(1L)
                .name("User-" + userId)
                .email("user@example.com")
                .phone("0000000000")
                .globalCreditLimit(0.0)
                .status("ACTIVE")
                .build();
        return CompletableFuture.completedFuture(Optional.of(user));
    }
}
```

### Controller Usage

```java
@PutMapping("/user/update/{userId}/globalCreditLimit/{limit}")
@SandboxAuthorizedSync(
        action = "user.update",
        object = "$$userId$$@" + UserProvider.OBJECT_TYPE + ".cipher.app",
        tenantID = "1001034"
)
public CompletionStage<ResponseEntity<UserResponse>> updateGlobalCreditLimit(
        @PathVariable("userId") Long userId,
        @PathVariable("limit") Double limit) {
    // Authorization is enforced before method execution
}
```

### Configuration

```properties
# Cipher/Sandbox Endpoints
certstore.proteus.endpoint=https://sb1-god-cipher.mum1-pp.zetaapps.in/proteus/zeta.in
sessions.proteus.endpoint=https://sb1-god-cipher.mum1-pp.zetaapps.in/proteus/zeta.in
```

### Package Scan

The application scans for Cipher components:
```java
@SpringBootApplication(scanBasePackages = {
    "com.example.userservice",
    "tech.zeta.academy.olympus.cipher"
})
```

---

## Scalability Considerations

### Current Implementation

| Aspect | Implementation | Scalability Rating |
|--------|----------------|-------------------|
| **Database** | PostgreSQL (Ganymede) | ⭐⭐⭐ (Vertical scaling) |
| **Caching** | None | ⭐⭐ (DB hit every request) |
| **API Design** | Synchronous REST | ⭐⭐⭐ (Simple, predictable) |
| **Unique Constraints** | DB-level enforcement | ⭐⭐⭐⭐ (No race conditions) |

### Future Improvements

1. **Caching**: Add Redis for user profile lookups (high read:write ratio)
2. **Read Replicas**: Use read replicas for GET endpoints
3. **Rate Limiting**: Per-user rate limits on update endpoints
4. **Authentication**: Integrate with Zeta's identity service
5. **Audit Logging**: Add audit trail for profile changes

---

## Trade-offs & Decisions

| Decision | Trade-off | Rationale |
|----------|-----------|-----------|
| **No Password Storage** | External auth dependency | Delegate to Zeta's identity service |
| **Synchronous APIs** | No async benefits | Simple CRUD, low latency required |
| **Global Credit Limit** | Cross-pool coupling | Business requirement for risk management |
| **Soft Delete** | Storage overhead | Audit compliance; referential integrity |
| **Unique Email + Phone** | Flexibility | Multiple contact options; login flexibility |

---

## Running Locally

### Prerequisites

- Java 17+
- Maven 3.8+
- PostgreSQL 14+ (or Ganymede connection)

### Build & Run

```bash
# Build
mvn clean install -DskipTests

# Run locally
mvn spring-boot:run -Dspring.profiles.active=local

# Run tests
mvn test

# Generate coverage report
mvn jacoco:report
```

### Docker

```bash
# Build image
docker build -t user-service:latest .

# Run container
docker run -p 8082:8082 \
  -e SPRING_PROFILES_ACTIVE=local \
  user-service:latest
```

---

## Configuration

### Key Properties

| Property | Description | Default |
|----------|-------------|--------|
| `server.port` | Application port | `8080` |
| `spring.datasource.url` | Database connection URL | Ganymede URL |
| `spring.jpa.hibernate.ddl-auto` | Schema management | `validate` |
| `certstore.proteus.endpoint` | Cipher certificate store endpoint | Sandbox URL |
| `sessions.proteus.endpoint` | Cipher sessions endpoint | Sandbox URL |

### Environment Variables

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/userservice
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=secret

# JPA
SPRING_JPA_HIBERNATE_DDL_AUTO=validate
SPRING_JPA_SHOW_SQL=false

# Cipher/Sandbox
CERTSTORE_PROTEUS_ENDPOINT=https://sb1-god-cipher.mum1-pp.zetaapps.in/proteus/zeta.in
SESSIONS_PROTEUS_ENDPOINT=https://sb1-god-cipher.mum1-pp.zetaapps.in/proteus/zeta.in
```

---

## Related Services

- **[PoolService](../poolservice/README.md)**: Pool, member, expense, and distribution management
- **[CreditLineService](../creditline-service/README.md)**: Credit line and repayment management

---

## API Usage Examples

### Register a New User

```bash
curl -X POST http://localhost:8082/api/v1/user/register \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Ramit Sharma",
    "email": "ramit@zeta.tech",
    "phone": "+91-9876543210",
    "globalCreditLimit": 100000.00
  }'
```

### Get User Profile

```bash
curl http://localhost:8082/api/v1/user/profile/userId/1
```

### Update Global Credit Limit

```bash
curl -X PUT http://localhost:8082/api/v1/user/update/1/globalCreditLimit/150000.00
```

---

*© 2026 Zeta Academy — Built with ❤️ by ramit@zeta.tech*