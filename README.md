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

- Java 8
- Spring Boot 2.5
- MyBatis and PageHelper
- MySQL
- Maven
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

- JDK 8 or later
- Maven 3.8+
- Node.js 16+
- MySQL 8

### Backend configuration

Set the following environment variables before starting the backend. Do not commit real credentials.

```text
DB_URL=jdbc:mysql://localhost:3306/manager
DB_USERNAME=root
DB_PASSWORD=your-local-password
FILE_STORAGE_PATH=./files/
APP_HOST=localhost
```

Start the backend:

```bash
cd springboot
mvn spring-boot:run
```

The API runs at `http://localhost:9090` by default.

### Frontend

```bash
cd vue
npm install
npm run serve
```

The frontend development server forwards API requests to the backend according to `vue.config.js`.

## Security notice

The original course-project version stored local database credentials in `application.yml`. Configuration now uses environment variables. If the original repository was public, rotate the exposed database password even when the database was only available locally.

Password hashing and endpoint-level role authorization are part of the current modernization work. Do not deploy the application publicly before those changes are complete.

## Modernization roadmap

- [x] Remove secrets from tracked configuration
- [x] Add repository documentation and ignore generated files
- [x] Add BCrypt password hashing with automatic migration of legacy passwords
- [x] Add an initial password security test suite
- [x] Add GitHub Actions backend verification and frontend build
- [ ] Add endpoint-level role authorization
- [ ] Add order, order-item, and inventory transaction modules
- [ ] Expand unit tests and add database integration tests
- [ ] Add Docker Compose for the application and database
- [ ] Upgrade to a current Java LTS and Spring Boot 3
- [ ] Rebuild the client with React and TypeScript

## Author

Chu Wang - MSc Computer Science student at Uppsala University, graduating in June 2027.
