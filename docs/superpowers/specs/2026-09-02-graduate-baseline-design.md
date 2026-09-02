# Graduate Baseline Project Design

## Purpose

Turn the existing shop application into a reliable, explainable portfolio project for a Swedish computer-science master's graduate. The result should demonstrate solid junior-level engineering without introducing distributed systems, cloud infrastructure, or a complex recommendation model that the available data cannot support.

## Positioning

The project is a small full-stack product-management and storefront application. It demonstrates authentication, role-based authorization, CRUD operations, pagination, database migrations, file uploads, validation, automated tests, and a supported single-page frontend.

The project must not claim machine-learning recommendations, production-scale architecture, or infrastructure experience that it does not contain.

## Technology Baseline

- Java 21 as the language and CI runtime.
- Spring Boot 3.5.x with Maven.
- MyBatis, MySQL 8, and Flyway.
- JUnit 5 and Mockito for focused backend tests.
- Vue 3 with Vite and Element Plus.
- Vitest and Vue Test Utils for focused frontend tests.
- Axios for HTTP requests.
- JWT authentication with one token source: the `token` HTTP header.

The implementation will not add microservices, Kafka, Kubernetes, cloud services, event sourcing, advanced DDD, or a machine-learning framework.

## Architecture

The backend remains a modular monolith with controllers, services, mappers, entities, and small request DTOs where validation is required. Authentication remains the existing custom JWT flow, but the interceptor becomes the single component that parses a token and loads the account. It stores that authenticated account on the request so downstream authorization and ownership checks all use the same identity.

The frontend remains a separate SPA. It will migrate from Vue 2/Vue CLI/Element UI to Vue 3/Vite/Element Plus while retaining the current pages and Options API where practical. The rich-text editor will be removed; product descriptions become plain text so the application does not need to accept or render user-supplied HTML.

## Backend Behaviour

### Authentication and authorization

- Tokens are accepted only from the `token` request header. Query-string tokens are rejected.
- The JWT interceptor validates the token, loads the account, enforces `@RequireRoles`, and stores the same account in the request authentication context.
- Service-level ownership checks use that request authentication context and fail closed when identity is absent.
- Invalid login and registration roles return a parameter error rather than a successful no-op response.
- Public registration remains limited to `BUSINESS` and `USER`; public administrator registration remains forbidden.
- An authenticated business may log in while pending approval so it can see its account status, but it may create, update, or delete products only when its status is `审核通过`.
- A business may mutate only products it owns, including every ID in a batch delete.
- A normal business or user may retrieve only its own account by ID; an administrator may retrieve any account.

### Validation

- Add Spring Bean Validation.
- Login and registration require a non-blank username, password, and supported role.
- Product creation and update require a non-blank name, a non-negative price, a non-negative stock count, and valid referenced identifiers.
- Pagination parameters must be positive and page size must have a reasonable upper bound of 100.
- Monetary values use `BigDecimal` in Java while retaining the existing MySQL `DECIMAL` column.
- Validation failures return the existing `Result` envelope with a clear parameter-error code and message.

### Product feed

- Remove `CoreMath`, `UserCF`, `RelateDTO`, and the user-similarity claim.
- Replace `/goods/recommend` with `/goods/featured`.
- `/goods/featured` returns at most ten distinct products ordered by descending product ID, which acts as a simple newest-products feed for the current schema.
- An empty catalogue returns an empty list.
- The frontend home page uses the new featured-products endpoint.
- The existing `count` field is presented consistently as stock quantity; the unsupported `热卖商品` section and its count-based ranking endpoint are removed.

### File uploads

- Accept only JPEG, PNG, and GIF image uploads, and verify that uploaded bytes decode as an image rather than trusting only the client-provided MIME type.
- Reject empty files and files larger than 5 MiB.
- Generate server-side UUID filenames and never concatenate an untrusted original filename into a filesystem path.
- A failed write returns an error; it never returns a success URL.
- Remove the wangEditor-specific upload endpoint after the frontend editor is removed.
- Keep authenticated deletion and validate that the requested file resolves inside the configured upload directory.

### Passwords and error handling

- Remove the predictable default password `123`. Administrative account creation must provide a valid password.
- Continue using BCrypt and the existing transparent migration of legacy plaintext passwords.
- Replace `System.out` and `System.err` error reporting in touched paths with structured application logging.
- Preserve the existing JSON `Result` contract to avoid unnecessary API redesign, while ensuring error responses are semantically correct inside that contract.

## Frontend Behaviour

- All route names are unique; manager and storefront home/type routes use distinct names.
- The global navigation guard requires a valid local account for protected routes and sends unauthenticated users to `/login`.
- Axios sends the JWT only in the `token` header.
- Product descriptions use a normal multiline text field and render with text interpolation, never `v-html`.
- Replace the Chinese typos `搜素` and `PORDUCT catergary` with clear interface text.
- Existing administrator, business, user, product, type, notice, profile, login, registration, search, and storefront flows remain available.
- The UI remains intentionally straightforward; the migration is for supported dependencies and maintainability, not for a visual redesign.

## Testing Strategy

Every production behaviour change follows red-green-refactor: first add a test that fails for the existing bug, confirm the expected failure, implement the smallest correction, and rerun the focused and full suites.

Backend tests cover:

- batch product deletion ownership;
- approved-business product mutations;
- header-only token authentication and shared request identity;
- business/user self-access versus administrator access;
- invalid login and registration roles;
- product request validation and pagination limits;
- featured-product ordering, limit, distinctness, and empty data;
- file type, size, path, successful-write, and failed-write behaviour;
- supplied-password requirements and existing BCrypt migration.

Frontend tests cover:

- unique route names and protected-route redirects;
- token-header request configuration;
- plain-text product description rendering;
- featured-products API usage.

Final verification includes the complete Maven test suite, frontend test suite, frontend production build, production-dependency audit, and a browser smoke test with no duplicate-route warnings.

## Documentation and CV Alignment

- Rewrite the README around the honest scope: a Java 21/Spring Boot modular monolith with Vue 3, MySQL, Flyway, JWT/RBAC, validation, uploads, and tests.
- Document local setup, environment variables, test commands, role rules, and the featured-products behaviour.
- Remove claims about collaborative filtering and production-scale architecture.
- Keep the existing roadmap short and realistic.
- Provide corrected CV project wording, but do not modify the CV PDF in this implementation unless separately requested.

## Non-goals

- No order, payment, inventory-reservation, messaging, or analytics subsystem.
- No Docker or deployment platform requirement.
- No microservices or asynchronous messaging.
- No recommendation algorithm based on inferred preferences.
- No broad visual redesign.
- No changes to unrelated portfolio projects.

## Acceptance Criteria

- No authenticated business can read another business profile or mutate another business's products.
- No authenticated user can read another user's profile.
- Pending businesses cannot mutate products through direct API calls.
- Query-string tokens do not authenticate requests.
- User-controlled product descriptions cannot execute as HTML.
- Invalid roles, invalid product data, unsafe uploads, and failed file writes return errors.
- Featured products are simple, deterministic, distinct, limited to ten, and described honestly.
- Backend and frontend automated tests pass.
- The frontend production build passes without duplicate route-name warnings.
- `npm audit --omit=dev` reports no high or critical production vulnerabilities.
- README claims match the implemented behaviour and technology versions.
