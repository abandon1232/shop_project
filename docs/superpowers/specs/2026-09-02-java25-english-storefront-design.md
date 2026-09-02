# Java 25 English Storefront Redesign

## Status

Approved in conversation on 2026-09-02.

This design supersedes the conflicting parts of `2026-09-02-graduate-baseline-design.md`: the runtime moves from Java 21 to Java 25, Spring Boot moves from 3.5 to 4.1, and the storefront now receives a deliberate visual redesign. The earlier document remains the source of truth for the previously approved security, validation, upload, and scope constraints.

## Objective

Turn the `upgrade` branch into a current, English-language portfolio project suitable for a Swedish computer-science master's new graduate. The application remains a straightforward modular monolith and Vue single-page application. It should be easy for its owner to explain in an interview while demonstrating current dependencies, database migrations, authentication and authorization, automated tests, and a polished electronics storefront.

## Product Positioning

The project is an electronics product catalogue and seller-management application, not a complete transactional commerce platform. It supports approved sellers, product discovery, administration, profiles, category browsing, and secure sign-in. It must not claim cart, checkout, payment, delivery, returns, price matching, or store-pickup behaviour that is not implemented.

The storefront brand will be `NorrByte Market`. It is an independent portfolio identity. Elgiganten is used only as a reference for Swedish electronics category structure, concise retail information hierarchy, and product-photography composition. Elgiganten branding, banners, long-form copy, and hosted images will not be copied into the repository.

## Technology Target

### Backend

- Java 25 as the language level, local runtime, and CI runtime.
- Spring Boot 4.1.1 with Maven.
- MyBatis Spring Boot Starter 4.1.0.
- PageHelper Spring Boot Starter 4.1.1, replacing the raw PageHelper dependency and custom interceptor configuration.
- MySQL with Flyway versions managed by Spring Boot.
- Auth0 Java JWT 4.6.0, which is already current.
- Spring Security Crypto for BCrypt, with its version managed by Spring Boot.
- JUnit, Mockito, and Spring Boot Test versions managed by Spring Boot.
- Maven Wrapper committed to the repository for reproducible local and CI builds.

Hutool will be removed rather than upgraded. Its current uses will be replaced with Java 25 string/null checks, `java.time`, and the SLF4J logging API already provided by Spring Boot. This reduces a broad utility dependency and makes the code easier to explain.

The Spring Boot 4 migration must preserve the existing JSON `Result` envelope, routes, role model, ownership rules, safe upload behaviour, and Flyway-managed schema. Compiler and test failures caused by updated Spring, servlet, validation, or MyBatis APIs will be corrected without adding a second framework or changing the application into microservices.

### Frontend

- Vue 3.5.42.
- Vue Router 5.3.1, upgraded from 4.6.4.
- Element Plus 2.14.5.
- Axios 1.20.0.
- Vite 8.2.2 and `@vitejs/plugin-vue` 6.0.8.
- Vitest 4.1.11 and Vue Test Utils 2.5.0.
- Node.js 24 LTS for development and CI.
- A small ESLint configuration and `npm run lint` command for JavaScript and Vue source.

Vue, Vite, Element Plus, Axios, and the existing testing packages are already current and should remain pinned. TypeScript, Pinia, Nuxt, SSR, and a new state-management architecture are outside scope.

### Continuous Integration and Runtime Metadata

- Update GitHub Actions to `actions/checkout@v6`, `actions/setup-java@v5`, and `actions/setup-node@v6`.
- Run the backend with Temurin Java 25 and the frontend with Node 24.
- Preserve Maven caching, `npm ci`, frontend tests, the production build, and the production dependency audit.
- Add `.java-version` and `.nvmrc` so local tooling uses Java 25 and Node 24.
- Add explicit `engines` and package-manager metadata when compatible with the installed npm version.

## English Conversion

English is the only application language. A runtime internationalisation library will not be added.

The conversion includes:

- navigation, headings, buttons, forms, placeholders, validation feedback, empty states, and 403/404 pages;
- router metadata and document titles;
- backend result messages, exceptions, validation messages, and touched log messages;
- source comments, test descriptions, and project documentation;
- image alternative text and accessible labels;
- fixed business-domain values that currently contain Chinese text.

Pull request #1, `docs: translate source comments to English`, may be used as a wording reference. It must not be cherry-picked blindly because the `upgrade` branch has diverged and now contains newer security fixes and a Vue 3 migration.

The obsolete `springboot/src/main/resources/rebel.xml`, which contains a machine-specific path, will be removed.

Free-form product names, descriptions, notices, and categories previously entered by a user are data, not application source. They will be preserved to avoid destructive or speculative translation. The repository-provided interface and fixed domain values must contain no Chinese characters after the migration.

## Business Status Migration

The business approval state will use stable database codes rather than translated display sentences:

| Existing value | New stored value | English UI label |
| --- | --- | --- |
| `审核中` | `PENDING` | `Pending review` |
| `审核通过` | `APPROVED` | `Approved` |
| `审核不通过` | `REJECTED` | `Rejected` |

Add `V2__translate_business_statuses.sql` rather than editing the already published V1 migration. V2 will update existing rows and change the column default to `PENDING`. The Java enum, MyBatis queries, authorization checks, Vue conditions, filters, forms, and tests will all use the new codes. The API may expose the code, while the UI owns the human-readable label.

This migration must be safe to run once on an existing database and must not remove accounts, products, categories, notices, or uploaded files.

## Storefront Content and Visual System

### Content

The category language may follow the concise structure common on the Swedish Elgiganten site:

- Computers & Office
- Appliances
- TV, Audio & Smart Home
- Phones, Tablets & Wearables
- Gaming
- Home & Garden
- Personal Care

System-owned storefront copy will be original English, for example:

- `Technology for everyday life`
- `Explore featured electronics`
- `Shop by category`
- `Products from approved sellers`

Prices will be displayed as Swedish kronor using `Intl.NumberFormat('en-SE', { style: 'currency', currency: 'SEK' })`. Stored decimal values and backend monetary types remain unchanged.

### Layout

The public storefront will use:

1. a compact header with a CSS/text logo, product search, and account controls;
2. a responsive hero area for current product-discovery messaging;
3. a horizontal or wrapping category section driven by API data;
4. a responsive featured-product grid driven by `/goods/featured`;
5. a short explanation that products come from approved sellers.

The current three-column layout, permanently visible user sidebar, decorative page background, and old carousel composition will be removed. Account information remains available through the header and profile page. The manager interface will use the same colour tokens and Element Plus icons but no decorative photography.

The visual palette uses deep navy, ice white, neutral grey, and a restrained warm-orange accent. It should feel Nordic and retail-oriented without reproducing Elgiganten's distinctive brand palette or promotional graphics.

The layout must work at desktop, tablet, and mobile widths. Keyboard focus, contrast, useful alt text, predictable loading states, and readable empty/error states are part of the implementation.

## Image Replacement

Delete all 16 currently tracked files in `vue/src/assets/imgs`, including the Chinese-named icons, old logo, backgrounds, and carousel images.

Replace them with four original, locally stored, compressed images:

- `hero-workspace.webp`: an unbranded laptop, headphones, and desk setup;
- `hero-home.webp`: an unbranded television and modern living-room electronics scene;
- `auth-electronics.webp`: a restrained Nordic technology scene for login and registration;
- `product-placeholder.webp`: a neutral fallback for missing or failed product images.

The new raster assets will be generated specifically for this project after reviewing electronics-commerce photography. They must not contain retailer logos, manufacturer trademarks, readable promotional text, watermarks, or recognisable copied banner designs. The README will state that the static visual assets were generated for the portfolio project.

The new text logo, category icons, account icons, and manager navigation icons will use HTML/CSS and Element Plus icons instead of additional raster files. Product and category images returned by the API remain user-managed data; the new local placeholder handles missing or broken URLs without overwriting that data.

## Error Handling and Compatibility

- A failed product or category image load switches once to the local placeholder and cannot enter a repeated error loop.
- Empty category and featured-product responses render clear English empty states.
- API failures retain the existing shared request error handling and display safe English feedback.
- Spring Boot 4 and MyBatis dependency changes must be checked with Maven's effective dependency tree to avoid mixed incompatible starter versions.
- No secret, database password, JWT key, or machine-specific absolute path may be committed.

## Testing and Verification

Behaviour changes follow red-green-refactor where practical. Tests will first capture migration-sensitive or user-visible behaviour before the corresponding implementation changes.

Backend verification includes:

- compiling and running the existing test suite on Java 25 and Spring Boot 4.1.1;
- updating focused tests for English result messages and `PENDING`/`APPROVED`/`REJECTED` values;
- checking approved-seller authorization after the status migration;
- validating MyBatis and PageHelper pagination after their starter upgrades;
- starting Flyway against a disposable or dedicated test schema where available;
- inspecting the resolved Maven dependency tree for conflicting Spring Boot 3 or MyBatis 3 starter artifacts.

Frontend verification includes:

- tests for Vue Router 5 navigation and existing protected-route behaviour;
- tests for English labels, SEK formatting, empty states, and the image fallback;
- `npm run lint`, `npm test`, `npm run build`, and `npm audit --omit=dev --audit-level=high`;
- a source scan for Chinese characters in application-controlled source and migrations;
- a browser smoke test at desktop and mobile widths with no console errors or broken static assets.

CI must run the same backend and frontend checks using Java 25 and Node 24.

## Delivery Sequence

1. Add or update tests that capture Boot 4 compatibility, approval statuses, English output, and frontend behaviour.
2. Upgrade Java, Spring Boot, MyBatis, PageHelper, CI actions, and runtime metadata.
3. Remove Hutool and resolve all compilation and compatibility changes.
4. Add the Flyway status migration and update backend/frontend status handling.
5. Convert application-controlled text and documentation to English.
6. Generate the new image set, remove all old tracked images, and implement the redesigned storefront and authentication pages.
7. Run complete automated, dependency, source-language, and browser verification.

Commits should remain reviewable and should not mix unrelated user files or work from other projects.

## Non-goals

- No microservices, Kafka, Kubernetes, Redis, event sourcing, advanced DDD, or cloud platform.
- No TypeScript rewrite, Nuxt migration, or new global state framework.
- No cart, order, payment, inventory reservation, shipping, return, or physical-store feature.
- No copying or hotlinking of retailer banners and product images.
- No automatic translation or deletion of arbitrary user-entered database content.
- No CV file edits unless separately requested.

## Acceptance Criteria

- The project builds and tests with Java 25, Spring Boot 4.1.1, MyBatis 4.1.0, Node 24, Vue 3.5.42, and Vue Router 5.3.1.
- Existing authentication, authorization, ownership, validation, pagination, upload, and featured-product behaviour remains correct.
- Existing Chinese business status values migrate safely and all code paths use the new English codes.
- Application-controlled source, UI, API messages, tests, and README are English.
- All 16 old tracked images are removed and every static image reference resolves to one of the four new original assets.
- The storefront uses the `NorrByte Market` identity, SEK formatting, truthful feature text, responsive layout, and accessible image fallbacks.
- Backend verification, frontend linting, tests, production build, dependency audit, and browser smoke testing pass.
- The final README accurately states the implemented scope and exact runtime requirements.
