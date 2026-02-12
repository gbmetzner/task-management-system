# Task Management API

## Features Implemented (Week 1)
- User registration and authentication (JWT)
- Database schema for Users, Projects, Tasks, Comments
- Redis caching layer
- Basic REST API structure

## Tech Stack
- Spring Boot 4
- Java 25
- PostgreSQL 17
- Redis
- JWT Authentication (jjwt)
- Flyway migrations
- Maven (with Maven Wrapper)
- TestContainers for integration tests
- Spotless (Palantir Java Format)
- JaCoCo for code coverage

## Prerequisites
- Java 25+
- Docker (for PostgreSQL, Redis, and TestContainers)
- Maven (or use the included Maven Wrapper `./mvnw`)

## Setup Instructions

### 1. PostgreSQL

Start a PostgreSQL container:

```bash
docker run -d \
  --name task-db \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -e POSTGRES_DB=task-db \
  -p 5432:5432 \
  postgres:17-alpine
```

The application will automatically create the `tms` schema and run Flyway migrations on startup.

### 2. Redis

Start a Redis container:

```bash
docker run -d \
  --name task-redis \
  -p 6379:6379 \
  redis:7-alpine
```

### 3. Environment Variables

Set the JWT secret before running the application:

```bash
export JWT_SECRET=your-secret-key-here
```

| Variable | Description | Default |
|---|---|---|
| `JWT_SECRET` | Secret key for signing JWT tokens | *(required)* |
| `JWT_EXPIRATION` | Token expiration in milliseconds | `86400000` (24h) |

### 4. Run the Application

```bash
./mvnw spring-boot:run -pl task-api
```

The API will be available at `http://localhost:8080`.

### 5. Run Tests

Tests use TestContainers, so Docker must be running:

```bash
./mvnw test
```

## API Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user |
| POST | `/api/v1/auth/login` | Login and receive a JWT token |

## Database Schema

All tables reside in the `tms` schema. Entity IDs use TSID (Time Sorted IDs).

- **users** - User accounts with email, password, name, and role (`ADMIN`, `USER`)
- **projects** - Projects with name, description, status (`ACTIVE`, `ARCHIVED`), and owner
- **project_members** - Many-to-many relationship between users and projects
- **tasks** - Tasks with title, description, status (`TODO`, `IN_PROGRESS`, `DONE`), priority (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), assignee, reporter, and due date
- **comments** - Comments on tasks with content and author
