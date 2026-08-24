# Product Management and Recommendation Platform

A full-stack product management and storefront application built with Spring Boot, MyBatis, MySQL, and Vue 2. The project supports administrator, merchant, and customer accounts, product and category management, search, pagination, file uploads, and a user-similarity recommendation flow.

> This repository is being modernized for graduate backend and full-stack software engineering applications. The current version is a modular monolith and is intentionally kept simple while testing, security, and deployment automation are improved.

## Current features

- Registration and login for administrators, merchants, and customers
- JWT-based authentication
- Product, category, notice, user, and merchant management
- Storefront product browsing, category filtering, and search
- Pagination and batch management operations
- Product recommendation based on user similarity
- Consistent API responses and centralized exception handling
- File upload and retrieval

## Technology stack

### Backend

- Java 25 LTS
- Spring Boot 3.5
- MyBatis and PageHelper
- MySQL 8 and Flyway
- Maven 3.9
- Auth0 Java JWT

### Frontend

- Vue 2
- Vue Router
- Axios
- Element UI
- ECharts

## Repository structure

```text
shop_project/
|- springboot/   # REST API and business logic
|- vue/          # Storefront and management interface
|- files/        # Local runtime uploads (not tracked by Git)
`- .env.example # Local configuration template
```

## Local development

### Prerequisites

- JDK 25
- Maven 3.9+
- Node.js 20+
- MySQL 8

### Create the local database

Open MySQL Workbench, connect to the local server as `root`, and run the
following statements. Replace the example password before running them and use
the same value for `DB_PASSWORD` below.

```sql
CREATE DATABASE IF NOT EXISTS manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'shop_app'@'localhost'
  IDENTIFIED BY 'replace-with-a-strong-local-password';

GRANT ALL PRIVILEGES ON manager.* TO 'shop_app'@'localhost';
FLUSH PRIVILEGES;
```

Flyway creates and versions the application tables automatically when the API
starts. Application credentials are intentionally separate from the MySQL
`root` account.

### Backend configuration

Set the following environment variables before starting the backend. Do not commit real credentials.

```text
DB_URL=jdbc:mysql://localhost:3306/manager
DB_USERNAME=shop_app
DB_PASSWORD=your-local-password
FILE_STORAGE_PATH=./files/
APP_HOST=localhost
CORS_ALLOWED_ORIGIN=http://localhost:8080
INITIAL_ADMIN_USERNAME=admin
INITIAL_ADMIN_PASSWORD=choose-a-strong-temporary-password
```

Start the backend:

```bash
cd springboot
mvn spring-boot:run
```

The API runs at `http://localhost:9090` by default.

The two `INITIAL_ADMIN_*` variables are optional and are used only to create the
first administrator. Remove them from the run configuration after the first
successful startup.

### Frontend

```bash
cd vue
npm ci
npm run serve
```

The frontend uses `http://localhost:9090` by default. Override it with
`VUE_APP_BASEURL` when the API runs at a different address.

## Security notice

The original course-project version stored local database credentials in `application.yml`. Configuration now uses environment variables. If the original repository was public, rotate the exposed database password even when the database was only available locally.

Password hashing and endpoint-level role authorization are part of the current modernization work. Do not deploy the application publicly before those changes are complete.

## Modernization roadmap

- [x] Remove secrets from tracked configuration
- [x] Add repository documentation and ignore generated files
- [x] Add BCrypt password hashing with automatic migration of legacy passwords
- [x] Add an initial password security test suite
- [x] Add GitHub Actions backend verification and frontend build
- [x] Upgrade to Java 25 and Spring Boot 3.5
- [x] Add versioned Flyway database migrations
- [x] Prevent administrator creation through public registration
- [x] Add endpoint-level role authorization and merchant ownership checks
- [ ] Add order, order-item, and inventory transaction modules
- [ ] Expand unit tests and add database integration tests
- [ ] Add Docker Compose for the application and database
- [ ] Rebuild the client with React and TypeScript

## Author

Chu Wang - MSc Computer Science student at Uppsala University, graduating in June 2027.
