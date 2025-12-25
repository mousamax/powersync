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

### 3. Start PowerSync Service
```bash
docker-compose up -d powersync
```

### 4. Verify Setup
```bash
docker-compose logs -f powersync
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
spring.liquibase.enabled=true
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

```yaml
- changeSet:
    id: 00X-add-table-to-publication
    changes:
      - sql:
          sql: ALTER PUBLICATION powersync ADD TABLE new_table;
```

## Production Considerations

- [ ] Change PowerSync user password (default: `powersync_secure_password_change_in_production`)
- [ ] Use environment variables for all credentials
- [ ] Enable SSL/TLS for database connections (`sslmode: verify-full`)
- [ ] Configure proper backup and monitoring
- [ ] Review publication scope (avoid `FOR ALL TABLES` with large datasets)