# PowerSync with Spring Boot

A production-ready example demonstrating how to integrate [PowerSync](https://www.powersync.com/) with Spring Boot for offline-first applications.

## Overview

This project showcases PostgreSQL logical replication setup with PowerSync, using Spring Boot and Liquibase for database management. It demonstrates best practices for managing database schema, user permissions, and publications required for PowerSync synchronization.

## Features

- **Liquibase-managed schema**: Version-controlled database migrations
- **PowerSync integration**: Automated setup of replication user and publications
- **JPA entities**: Clean domain model with audit fields
- **PostgreSQL logical replication**: Enabled for real-time sync
- **Docker Compose**: Full local development environment

## Quick Start

### Prerequisites
- Java 21+
- Docker & Docker Compose
- Maven

### 1. Start Infrastructure
```bash
docker-compose up -d postgres-powersync mongo mongo-rs-init
```

### 2. Run Spring Boot Application
```bash
./mvnw spring-boot:run
```

This will automatically:
- Create database schema via Liquibase
- Set up PowerSync user with replication privileges
- Create `powersync` publication for specified tables
- Start REST API on port 8080 with:
  - `POST /api/auth/login` - Generate JWT tokens
  - `GET /api/auth/token/{memberId}` - Get JWT for member
  - `POST /api/powersync/write-checkpoint` - Handle client writes

### 3. Start PowerSync Service
```bash
docker-compose up -d powersync
```

### 4. Verify Setup
```bash
docker-compose logs -f powersync
```

### 5. Get JWT Token for Testing
```bash
# Get list of members
curl http://localhost:8080/api/auth/token/{member-id}
```

## Architecture

### Database Setup

**Liquibase handles three critical tasks:**

1. **Schema Creation** (`001-create-initial-schema.yaml`)
   - Tables: `family`, `member`, `task_list`, `task`
   - Foreign keys and indexes
   - Audit fields

2. **PowerSync User** (`002-setup-powersync-user.yaml`)
   - Creates `powersync_role` with `REPLICATION` privilege
   - Grants `SELECT` on all tables
   - Auto-grants permissions for future tables

3. **PowerSync Publication** (`003-setup-powersync-publication.yaml`)
   - Creates `powersync` publication (required name)
   - Specifies tables to replicate

### Client Authentication Flow

```
Mobile Client
    ↓
POST /api/auth/login (email, password)
    ↓
Returns JWT with {member_id, family_id} claims
    ↓
Client uses JWT to connect to PowerSync Service (port 8080)
    ↓
PowerSync validates JWT and syncs family-specific data
```

### Sync Rules (Family-Based Isolation)

PowerSync syncs only data belonging to the user's family:
- Each family gets a separate bucket
- JWT `family_id` claim determines which bucket
- Users only see their family's: members, task lists, tasks

### Write Operations Flow

```
Mobile Client makes local changes
    ↓
Client SDK batches operations (PUT/PATCH/DELETE)
    ↓
POST /api/powersync/write-checkpoint
    ↓
Synchronously writes to PostgreSQL
    ↓
# JWT Authentication (HS512)
client_auth:
  jwks:
    keys:
      - kty: 'oct'
        k: '<base64-encoded-secret>'
        alg: 'HS512'
  audience: ['powersync-dev']

# Family-based sync rules
sync_rules:
  content: |
    bucket_definitions:
      family_data:
        parameters: SELECT token.parameters->>'family_id' AS family_id
        data:
          - SELECT * FROM family WHERE id = bucket.family_id
          - SELECT * FROM member WHERE family_id = bucket.family_id
          - SELECT * FROM task_list WHERE family_id = bucket.family_id
          - SELECT * FROM task WHERE task_list_id IN (...)

### Execution Flow

```
Application Start
    ↓
Liquibase Migrations
    ├─ Create schema (tables, FKs, indexes)
    ├─ Create PowerSync user
    └─ Create PowerSync publication
    ↓
Hibernate Validates Schema
    ↓
REST Controllers Ready
    ├─ /api/auth/** (JWT generation)
    └─ /api/powersync/** (write checkpoint)
    ↓
Application Ready
```

## Configuration

### Key Settings

**PostgreSQL** ([docker-compose.yml](docker-compose.yml))
```yaml
command: ["postgres", "-c", "wal_level=logical"]
```

**Hibernate** ([application.properties](src/main/resources/application.properties))
```properties
spring.jpa.hibernate.ddl-auto=validate
spring.lcontroller/          # REST endpoints
│   │   ├── AuthController.java         # JWT token generation
│   │   └── PowerSyncController.java    # Write checkpoint
│   ├── security/            # JWT service
│   │   └── JwtService.java
│   ├── config/              # Security configuration
│   │   └── SecurityConfig.java
│   ├── dto/                 # Request/Response objects
│   │   ├── LoginRequest.java
│   │   ├── TokenResponse.java
│   │   └── WriteCheckpointRequest.java
│   ├── iquibase.enabled=true
```

**PowerSync** ([config/config.yaml](config/config.yaml))
```yaml
replication:
  connections:
    - type: postgresql
      uri: postgresql://powersync_role:***@postgres:4321/postgres
```

## Project Structure

```
src/main/
├── java/com/familymind/powersync/
│   ├── entity/              # JPA entities
│   ├── repository/          # Spring Data repositories
│   └── bootstrap/           # Sample data initializer
└── resources/
    ├── application.properties
    └── db/changelog/
        ├── db.changelog-master.yaml
        └── changesets/
            ├── 001-create-initial-schema.yaml
            ├── 002-setup-powersync-user.yaml
            └── 003-setup-powersync-publication.yaml
```

## Adding New Tables

1. Create JPA entity
2. Add Liquibase changeset for table structure
3. Add table to PowerSync publication:
JWT secret (current: default dev secret)
- [ ] Change PowerSync user password (default: `powersync_secure_password_change_in_production`)
- [ ] Use environment variables for all credentials
- [ ] Enable SSL/TLS for database connections (`sslmode: verify-full`)
- [ ] Implement password hashing and verification in AuthController
- [ ] Add rate limiting on auth endpoints
- [ ] Configure proper CORS policies (currently allows all origins)
- [ ] Add request validation and family-based authorization
- [ ] Configure proper backup and monitoring
- [ ] Review publication scope (avoid `FOR ALL TABLES` with large datasets)

## API Endpoints

### Authentication
- `POST /api/auth/login` - Login with email/password (returns JWT)
  ```json
  {
    "email": "john.smith@example.com",
    "password": "password"
  }
  ```
- `GET /api/auth/token/{memberId}` - Get JWT for member ID (dev only)

### PowerSync
- `POST /api/powersync/write-checkpoint` - Accept client write operations
  ```json
  {
    "operations": [
      {
        "op": "PUT",
        "table": "task",
        "data": {
          "id": "uuid",
          "title": "New Task",
          "task_list_id": "list-uuid"
        }
      }
    ]
  }
  ```
      - sql:
          sql: ALTER PUBLICATION powersync ADD TABLE new_table;
```

## Production Considerations

- [ ] Change PowerSync user password (default: `powersync_secure_password_change_in_production`)
- [ ] Use environment variables for all credentials
- [ ] Enable SSL/TLS for database connections (`sslmode: verify-full`)
- [ ] Configure proper backup and monitoring
- [ ] Review publication scope (avoid `FOR ALL TABLES` with large datasets)