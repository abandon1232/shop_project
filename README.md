# Product Management and Storefront Platform

A full-stack portfolio project for administrator, merchant, and customer workflows. It is intentionally implemented as a maintainable modular monolith: substantial enough to demonstrate graduate-level backend and full-stack fundamentals without adding distributed-system complexity that the product does not need.

## Features

- Login for administrators, merchants, and customers, with public registration limited to merchants and customers
- JWT authentication through the `token` HTTP header
- Role checks, self-service account access, merchant ownership checks, and merchant approval rules
- BCrypt password storage and Bean Validation request checks
- Product and category management with non-negative price and stock validation
- Storefront browsing, pagination, category filtering, search, and a deterministic latest-products feed
- JPEG, PNG, and GIF uploads with decoded-content validation, a 5 MiB limit, generated filenames, and path isolation
- Flyway-managed MySQL schema migrations
- JUnit/Mockito backend tests, Vitest frontend tests, and GitHub Actions verification

`count` represents current stock. The latest-products feed is deliberately not described as a personalized recommendation system because the application has no interaction data or recommender evaluation.

## Technology stack

### Backend

- Java 21 and Spring Boot 3.5
- MyBatis, PageHelper, MySQL 8, and Flyway
- Maven, JUnit 5, and Mockito
- Auth0 Java JWT and BCrypt

### Frontend

- Vue 3 with the Options API
- Vite, Vue Router 4, Element Plus, and Axios
- Vitest and Vue Test Utils

## Repository structure

```text
shop_project/
|- springboot/   # REST API, authorization, business rules, and migrations
|- vue/          # Storefront and management SPA
`- .env.example # Configuration reference
```

With the documented startup command, local uploads are written under `springboot/files/` by default and are not tracked by Git. `FILE_STORAGE_PATH` can point to a different runtime directory.

## Local development

### Prerequisites

- JDK 21
- Maven 3.9+
- Node.js 24
- MySQL 8

### 1. Create the database

Run the following with a MySQL administrator. Replace the example password and use the same value for `DB_PASSWORD`.

```sql
CREATE DATABASE IF NOT EXISTS manager
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;

CREATE USER IF NOT EXISTS 'shop_app'@'localhost'
  IDENTIFIED BY 'replace-with-a-strong-local-password';

GRANT ALL PRIVILEGES ON manager.* TO 'shop_app'@'localhost';
FLUSH PRIVILEGES;
```

Flyway creates and versions the tables when the API starts.

### 2. Configure and start the backend

Use [.env.example](.env.example) as a reference and set the backend variables in your shell or IDE run configuration. Do not commit real credentials.

```text
DB_URL=jdbc:mysql://localhost:3306/manager?useUnicode=true&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
DB_USERNAME=shop_app
DB_PASSWORD=your-local-password
FILE_STORAGE_PATH=./files/
APP_HOST=localhost
CORS_ALLOWED_ORIGIN=http://localhost:8080
INITIAL_ADMIN_USERNAME=admin
INITIAL_ADMIN_PASSWORD=choose-a-strong-temporary-password
```

Start the API:

```bash
cd springboot
mvn spring-boot:run
```

The API runs at `http://localhost:9090`. The two `INITIAL_ADMIN_*` values are optional and create the first administrator only when both are present and the username does not already exist. Remove them from the run configuration after the first successful startup.

### 3. Configure and start the frontend

The default frontend API address is `http://localhost:9090`. To override it, create `vue/.env.local`:

```text
VITE_API_BASE_URL=http://localhost:9090
```

Then start Vite:

```bash
cd vue
npm ci
npm run dev
```

Open `http://localhost:8080`.

## Verification

Run the same main checks used by continuous integration:

```bash
cd springboot
mvn clean verify
```

```bash
cd vue
npm ci
npm test
npm run build
npm audit --omit=dev --audit-level=high
```

## Authorization model

- Public users may log in and register customer or merchant accounts; public administrator registration is blocked.
- Customers may read or update only their own account profile.
- Merchants may read or update only their own profile and may modify only their own products after approval.
- Administrators may manage accounts, approvals, categories, notices, products, and uploaded files.

## Scope

This is a shop management and catalogue storefront, not a production commerce platform. Orders, checkout, payment, shipping, transactional inventory history, and personalized recommendations are outside its current scope. The single backend and SPA keep deployment and code review understandable for an early-career portfolio.

## Author

Chu Wang — MSc Computer Science student at Uppsala University, expected graduation June 2027.
