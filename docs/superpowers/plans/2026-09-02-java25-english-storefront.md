# Java 25 English Storefront Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Upgrade the existing `upgrade` branch to Java 25 and Spring Boot 4.1.1, convert all application-owned language and approval statuses to English, and replace the old imagery and storefront with the approved NorrByte Market design.

**Architecture:** Preserve the existing Spring Boot modular monolith, MyBatis mappers, Flyway schema history, custom JWT request context, and Vue Options API. Apply compatibility changes in small commits, use a new Flyway migration for persisted status values, centralize frontend status and currency presentation, and keep all new static imagery local and original.

**Tech Stack:** Java 25, Spring Boot 4.1.1, Maven 3.9.16 Wrapper, MyBatis Spring Boot 4.1.0, PageHelper Spring Boot 4.1.1, MySQL, Flyway, Vue 3.5.42, Vue Router 5.3.0, Element Plus 2.14.5, Axios 1.20.0, Vite 8.2.2, Vitest 4.1.11, Vue Test Utils 2.5.0, Node.js 24 LTS.

**Spec:** `docs/superpowers/specs/2026-09-02-java25-english-storefront-design.md`

## Global Constraints

- Use Java 25 and Spring Boot 4.1.1; do not introduce preview Java language features.
- Keep a single Spring Boot application and a separate Vue SPA; do not add microservices or infrastructure services.
- Preserve the current `Result` JSON shape, API URLs, role codes, ownership checks, validation, upload security, and `/goods/featured` behaviour.
- English is the only application language; do not add an i18n framework.
- Store approval codes as `PENDING`, `APPROVED`, and `REJECTED`; use a new Flyway V2 migration and do not edit V1.
- Preserve arbitrary user-entered database content.
- Use `NorrByte Market`, English Swedish-market copy, and SEK formatting without claiming unimplemented commerce features.
- Delete all 16 existing tracked image files and add only the four approved original local images.
- Do not copy or hotlink retailer imagery, logos, or promotional text.
- Keep secrets and machine-specific paths out of Git.

---

### Task 1: Upgrade the backend runtime and framework integration

**Files:**
- Modify: `springboot/pom.xml`
- Delete: `springboot/src/main/java/com/example/common/config/PageHelperConfig.java`
- Create: `.java-version`
- Create: `.mvn/wrapper/maven-wrapper.properties`
- Create: `mvnw`
- Create: `mvnw.cmd`
- Test: all files under `springboot/src/test/java`

**Interfaces:**
- Consumes: the existing Maven project and regression tests.
- Produces: a Java 25/Spring Boot 4.1.1 build using MyBatis 4.1.0 and PageHelper's Boot 4 starter.

- [ ] **Step 1: Establish the pre-upgrade regression baseline**

Run from `springboot`:

```powershell
mvn --batch-mode verify
```

Expected: all existing tests pass before dependency changes. Record any environment-only Java mismatch separately from test failures.

- [ ] **Step 2: Update the Maven runtime and dependency declarations**

Change the parent and Java property in `springboot/pom.xml`:

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.1.1</version>
</parent>

<properties>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
    <java.version>25</java.version>
</properties>
```

Set MyBatis to 4.1.0 and replace the raw PageHelper dependency:

```xml
<dependency>
    <groupId>org.mybatis.spring.boot</groupId>
    <artifactId>mybatis-spring-boot-starter</artifactId>
    <version>4.1.0</version>
</dependency>
<dependency>
    <groupId>com.github.pagehelper</groupId>
    <artifactId>pagehelper-spring-boot-starter</artifactId>
    <version>4.1.1</version>
</dependency>
```

Keep Hutool temporarily during this compatibility step. Keep Java JWT at 4.6.0. Allow Spring Boot to manage Spring Security Crypto, MySQL Connector, Flyway, validation, and test dependency versions.

- [ ] **Step 3: Remove the duplicate manual PageHelper registration**

Delete `PageHelperConfig.java`; the PageHelper Boot 4 starter must be the only component registering the `PageInterceptor`.

- [ ] **Step 4: Add reproducible Java and Maven metadata**

Create `.java-version` containing:

```text
25
```

Generate Maven Wrapper 3.9.16 from the repository root:

```powershell
mvn -N wrapper:wrapper -Dmaven=3.9.16
```

Confirm `.mvn/wrapper/maven-wrapper.properties` points to Apache Maven 3.9.16 and that `mvnw` and `mvnw.cmd` are present.

- [ ] **Step 5: Verify Boot 4 compilation and tests**

Run:

```powershell
./mvnw.cmd --batch-mode verify
./mvnw.cmd --batch-mode dependency:tree "-Dincludes=org.springframework.boot:*,org.mybatis*:*,com.github.pagehelper:*"
```

Expected: tests pass; the dependency tree contains Spring Boot 4.1.1, MyBatis Spring Boot 4.1.0, and PageHelper Starter 4.1.1, with no Spring Boot 3.x starter.

- [ ] **Step 6: Commit the backend platform upgrade**

```powershell
git add springboot/pom.xml springboot/src/main/java/com/example/common/config/PageHelperConfig.java .java-version .mvn mvnw mvnw.cmd
git commit -m "build: upgrade backend to Java 25 and Spring Boot 4"
```

---

### Task 2: Remove Hutool and use standard Java and Spring APIs

**Files:**
- Modify: `springboot/pom.xml`
- Modify: `springboot/src/main/java/com/example/utils/TokenUtils.java`
- Modify: `springboot/src/main/java/com/example/common/config/JwtInterceptor.java`
- Modify: `springboot/src/main/java/com/example/service/AdminService.java`
- Modify: `springboot/src/main/java/com/example/service/BusinessService.java`
- Modify: `springboot/src/main/java/com/example/service/UserService.java`
- Modify: `springboot/src/main/java/com/example/service/TypeService.java`
- Modify: `springboot/src/main/java/com/example/service/NoticeService.java`
- Modify: `springboot/src/main/java/com/example/exception/GlobalExceptionHandler.java`
- Test: `springboot/src/test/java/com/example/utils/TokenUtilsTest.java`
- Test: `springboot/src/test/java/com/example/service/AccountAccessTest.java`

**Interfaces:**
- Consumes: the Java 25 backend from Task 1.
- Produces: the same token, blank-value, date, null-check, and logging behaviour without `cn.hutool:hutool-all`.

- [ ] **Step 1: Strengthen the token expiration regression test**

In `TokenUtilsTest`, decode a newly created token and assert its expiry is after now and no more than two hours plus a small test tolerance:

```java
Instant expiresAt = JWT.decode(token).getExpiresAtAsInstant();
assertTrue(expiresAt.isAfter(Instant.now()));
assertTrue(expiresAt.isBefore(Instant.now().plus(Duration.ofHours(2)).plusSeconds(5)));
```

- [ ] **Step 2: Run the focused tests before replacing utilities**

```powershell
./mvnw.cmd --batch-mode -Dtest=TokenUtilsTest,AccountAccessTest test
```

Expected: PASS, establishing current behaviour.

- [ ] **Step 3: Replace Hutool calls with standard APIs**

Use:

```java
Date.from(Instant.now().plus(Duration.ofHours(2)))
```

for token expiry, `value == null || value.isBlank()` for required strings, direct `value == null`/`value != null` checks for objects, and `LocalDate.now().toString()` for date-only values. Replace Hutool logging with:

```java
private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
```

and parameterized `log.error(...)` calls.

- [ ] **Step 4: Remove the Hutool dependency**

Delete the complete `cn.hutool:hutool-all` dependency block from `springboot/pom.xml`.

- [ ] **Step 5: Verify no Hutool usage remains**

```powershell
rg "cn\.hutool|DateUtil|ObjectUtil|StrUtil|LogFactory" springboot/src springboot/pom.xml
./mvnw.cmd --batch-mode verify
```

Expected: `rg` returns no matches and all tests pass.

- [ ] **Step 6: Commit the dependency simplification**

```powershell
git add springboot/pom.xml springboot/src/main springboot/src/test/java/com/example/utils/TokenUtilsTest.java
git commit -m "refactor: replace Hutool with standard Java APIs"
```

---

### Task 3: Migrate business approval statuses to English codes

**Files:**
- Create: `springboot/src/main/resources/db/migration/V2__translate_business_statuses.sql`
- Modify: `springboot/src/main/java/com/example/common/enums/StatusEnum.java`
- Modify: `springboot/src/main/java/com/example/service/GoodsService.java`
- Modify: `springboot/src/main/resources/mapper/GoodsMapper.xml`
- Create: `springboot/src/test/java/com/example/common/enums/StatusEnumTest.java`
- Modify: `springboot/src/test/java/com/example/service/GoodsServiceTest.java`

**Interfaces:**
- Consumes: existing `business.status` string values and Flyway V1 schema.
- Produces: `StatusEnum.PENDING`, `StatusEnum.APPROVED`, and `StatusEnum.REJECTED`; the database stores the enum names.

- [ ] **Step 1: Write tests for the new stable status codes**

Create `StatusEnumTest.java`:

```java
package com.example.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class StatusEnumTest {
    @Test
    void exposesStableEnglishDatabaseCodes() {
        assertEquals("PENDING", StatusEnum.PENDING.code());
        assertEquals("APPROVED", StatusEnum.APPROVED.code());
        assertEquals("REJECTED", StatusEnum.REJECTED.code());
    }
}
```

Update `GoodsServiceTest` fixtures to use `StatusEnum.PENDING.code()` and `StatusEnum.APPROVED.code()`.

- [ ] **Step 2: Run the new status tests and confirm failure**

```powershell
./mvnw.cmd --batch-mode -Dtest=StatusEnumTest,GoodsServiceTest test
```

Expected: compilation fails because the new enum members and `code()` do not exist.

- [ ] **Step 3: Implement the enum and authorization comparison**

Replace the enum with:

```java
public enum StatusEnum {
    PENDING,
    APPROVED,
    REJECTED;

    public String code() {
        return name();
    }
}
```

Change seller approval checks to compare against `StatusEnum.APPROVED.code()`.

- [ ] **Step 4: Add the forward-only Flyway migration**

Create V2 with these statements:

```sql
UPDATE business SET status = 'PENDING' WHERE status = '审核中';
UPDATE business SET status = 'APPROVED' WHERE status = '审核通过';
UPDATE business SET status = 'REJECTED' WHERE status = '审核不通过';
ALTER TABLE business ALTER COLUMN status SET DEFAULT 'PENDING';
```

Change `GoodsMapper.xml` to filter approved sellers with `business.status = 'APPROVED'`. Do not alter V1.

- [ ] **Step 5: Verify status behaviour and migration text**

```powershell
./mvnw.cmd --batch-mode -Dtest=StatusEnumTest,GoodsServiceTest test
rg -n "审核中|审核通过|审核不通过" springboot/src/main/java springboot/src/main/resources/mapper
```

Expected: focused tests pass and Chinese status strings remain only in V2's migration predicates.

- [ ] **Step 6: Commit the database status migration**

```powershell
git add springboot/src/main/resources/db/migration/V2__translate_business_statuses.sql springboot/src/main/java/com/example/common/enums/StatusEnum.java springboot/src/main/java/com/example/service/GoodsService.java springboot/src/main/resources/mapper/GoodsMapper.xml springboot/src/test
git commit -m "feat: migrate business statuses to English codes"
```

---

### Task 4: Convert backend-owned messages and comments to English

**Files:**
- Modify: `springboot/src/main/java/com/example/common/enums/ResultCodeEnum.java`
- Modify: `springboot/src/main/java/com/example/controller/WebController.java`
- Modify: `springboot/src/main/java/com/example/exception/GlobalExceptionHandler.java`
- Modify: every Java/XML file reported by the Chinese source scan under `springboot/src/main`
- Delete: `springboot/src/main/resources/rebel.xml`
- Modify: backend tests whose expected messages change

**Interfaces:**
- Consumes: the existing `Result` error envelope and PR #1 only as a wording reference.
- Produces: English API messages, logs, comments, and validation output without changing result codes.

- [ ] **Step 1: Add English message expectations to controller tests**

Update focused assertions in `WebControllerTest` and `GoodsValidationTest` to expect concise messages such as `Invalid username or password`, `Unsupported account role`, and `Invalid request` while preserving existing result codes.

- [ ] **Step 2: Run focused tests and confirm the old Chinese output fails the new assertions**

```powershell
./mvnw.cmd --batch-mode -Dtest=WebControllerTest,GoodsValidationTest test
```

Expected: assertions fail on old Chinese result text.

- [ ] **Step 3: Translate application-controlled backend text**

Translate every backend-owned message, comment, and log string. Keep messages short and avoid leaking exception internals. Preserve numeric/string result codes and controller response shapes.

- [ ] **Step 4: Remove the machine-specific JRebel file**

Delete `springboot/src/main/resources/rebel.xml`; no runtime code references it.

- [ ] **Step 5: Verify backend English and regression behaviour**

```powershell
rg -n "[\p{Han}]" springboot/src/main/java springboot/src/main/resources/mapper springboot/src/test
./mvnw.cmd --batch-mode verify
```

Expected: no Chinese characters in Java, mapper, or test files; all backend tests pass. Chinese source literals are allowed only inside the V2 data-conversion migration.

- [ ] **Step 6: Commit the backend English conversion**

```powershell
git add springboot/src
git commit -m "refactor: translate backend-owned text to English"
```

---

### Task 5: Upgrade frontend routing and add shared presentation helpers

**Files:**
- Modify: `vue/package.json`
- Modify: `vue/package-lock.json`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/router/index.test.js`
- Create: `vue/src/constants/businessStatus.js`
- Create: `vue/src/constants/businessStatus.test.js`
- Create: `vue/src/utils/format.js`
- Create: `vue/src/utils/format.test.js`
- Create: `vue/eslint.config.js`
- Create: `.nvmrc`

**Interfaces:**
- Produces: `BUSINESS_STATUS`, `businessStatusLabel(code)`, and `formatSek(value)` for later views.
- Consumes: Vue Router's existing manual route table and the status codes from Task 3.

- [ ] **Step 1: Write helper tests**

Test the exact interfaces:

```javascript
expect(businessStatusLabel(BUSINESS_STATUS.PENDING)).toBe('Pending review')
expect(businessStatusLabel(BUSINESS_STATUS.APPROVED)).toBe('Approved')
expect(businessStatusLabel(BUSINESS_STATUS.REJECTED)).toBe('Rejected')
expect(businessStatusLabel('UNKNOWN')).toBe('Unknown')
expect(formatSek(1299)).toMatch(/1[\s\u00a0]299,00\s*kr/)
```

- [ ] **Step 2: Run the helper tests and confirm they fail**

```powershell
npm test -- businessStatus.test.js format.test.js
```

Expected: FAIL because both modules are missing.

- [ ] **Step 3: Implement status and currency helpers**

`businessStatus.js` exports frozen codes and a label lookup. `format.js` creates one formatter:

```javascript
const sekFormatter = new Intl.NumberFormat('en-SE', {
  style: 'currency',
  currency: 'SEK',
})

export const formatSek = value => sekFormatter.format(Number(value) || 0)
```

- [ ] **Step 4: Upgrade routing and add lint tooling**

Set `vue-router` to `5.3.0`. Add `eslint`, `@eslint/js`, `eslint-plugin-vue`, and `globals` as pinned dev dependencies compatible with Node 24. Add:

```json
"lint": "eslint .",
"engines": { "node": ">=24 <25" }
```

Create a flat ESLint configuration for `src/**/*.{js,vue}` that ignores `dist` and disables only `vue/multi-word-component-names` for the existing page names. Create `.nvmrc` containing `24` and regenerate the lockfile with `npm install`.

- [ ] **Step 5: Verify helpers, Router 5, and lint configuration**

```powershell
npm test
npm run lint
npm run build
```

Expected: route guard and unique-name tests still pass under Router 5; helpers pass; lint and build succeed.

- [ ] **Step 6: Commit frontend platform changes**

```powershell
git add vue/package.json vue/package-lock.json vue/eslint.config.js vue/src/router vue/src/constants vue/src/utils/format.js vue/src/utils/format.test.js .nvmrc
git commit -m "build: update frontend routing and quality checks"
```

---

### Task 6: Convert the Vue application to English and new status codes

**Files:**
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/views/404.vue`
- Modify: `vue/src/views/Front.vue`
- Modify: `vue/src/views/Login.vue`
- Modify: `vue/src/views/Register.vue`
- Modify: all files under `vue/src/views/front`
- Modify: all files under `vue/src/views/manager`
- Modify: related Vitest files

**Interfaces:**
- Consumes: `BUSINESS_STATUS`, `businessStatusLabel`, and `formatSek` from Task 5.
- Produces: an English-only UI using `PENDING`, `APPROVED`, and `REJECTED` and SEK prices.

- [ ] **Step 1: Update user-visible frontend tests first**

Change `Home.test.js` fixtures and assertions to use English product data and require `Featured products`, `Shop by category`, and SEK output. Add manager goods assertions that a business with `PENDING` status sees `Your seller account must be approved before you can publish products`.

- [ ] **Step 2: Run focused tests and confirm failure on the Chinese UI**

```powershell
npm test -- Home.test.js Goods.test.js
```

Expected: tests fail because the current labels and business comparison are Chinese.

- [ ] **Step 3: Translate public and authentication views**

Translate headers, search controls, account controls, login, registration, profile, search, category, empty states, validation, and navigation. Use `formatSek` in Home, Search, and Type product cards. Keep roles stored as `ADMIN`, `BUSINESS`, and `USER`, with human labels `Administrator`, `Seller`, and `Customer`.

- [ ] **Step 4: Translate manager views and use status helpers**

Translate all table columns, form labels, dialog titles, filters, confirmation prompts, success/error notices, menu items, and account pages. Business status selectors submit the uppercase codes and display labels through `businessStatusLabel`.

- [ ] **Step 5: Translate router titles and source comments**

Every route title and source comment under `vue/src` must be English. Keep route paths and API endpoints stable.

- [ ] **Step 6: Verify the complete English UI**

```powershell
rg -n "[\p{Han}]" vue/src
npm run lint
npm test
npm run build
```

Expected: no Chinese characters under `vue/src`; all checks pass.

- [ ] **Step 7: Commit the English frontend**

```powershell
git add vue/src
git commit -m "refactor: translate the application interface to English"
```

---

### Task 7: Generate original imagery and implement the NorrByte storefront

**Files:**
- Delete: all current files under `vue/src/assets/imgs`
- Create: `vue/src/assets/imgs/hero-workspace.webp`
- Create: `vue/src/assets/imgs/hero-home.webp`
- Create: `vue/src/assets/imgs/auth-electronics.webp`
- Create: `vue/src/assets/imgs/product-placeholder.webp`
- Modify: `vue/src/views/Front.vue`
- Modify: `vue/src/views/front/Home.vue`
- Modify: `vue/src/views/front/Home.test.js`
- Modify: `vue/src/views/front/Search.vue`
- Modify: `vue/src/views/front/Type.vue`
- Modify: `vue/src/views/Login.vue`
- Modify: `vue/src/views/Register.vue`
- Modify: `vue/src/views/Manager.vue`
- Modify: `vue/src/assets/css/global.css`
- Modify: `vue/src/assets/css/front.css`
- Modify: `vue/src/assets/css/manager.css`
- Create: `vue/src/utils/imageFallback.js`
- Create: `vue/src/utils/imageFallback.test.js`

**Interfaces:**
- Produces: `useImageFallback(event)` that replaces one failed image URL with the local placeholder and then clears its own error handler.
- Consumes: API-driven category/product data and the English helpers from Tasks 5-6.

- [ ] **Step 1: Write the image fallback test**

Test that a failed image is replaced exactly once:

```javascript
const event = { target: { src: 'broken.jpg', onerror: () => {} } }
useImageFallback(event)
expect(event.target.src).toContain('product-placeholder')
expect(event.target.onerror).toBeNull()
```

- [ ] **Step 2: Run the fallback test and confirm failure**

```powershell
npm test -- imageFallback.test.js
```

Expected: FAIL because `imageFallback.js` does not exist.

- [ ] **Step 3: Generate the four approved image concepts**

Use the image generation workflow to create four original, unbranded electronics images. Prompts must exclude logos, readable text, watermarks, identifiable retailer campaigns, and copied banner layouts. Inspect every result before adding it to the project, then convert/export optimized WebP files with the exact approved filenames.

- [ ] **Step 4: Remove old images and implement the fallback**

Delete the 16 tracked legacy images. Implement `useImageFallback(event)` by clearing `event.target.onerror` before assigning the imported local placeholder.

- [ ] **Step 5: Rebuild the public storefront**

Implement the approved NorrByte header, search, responsive hero, API-driven category row, featured-product grid, approved-seller note, English empty/error states, and SEK formatting. Remove the old three-column sidebar composition, old carousel, decorative background, and permanent user panel.

- [ ] **Step 6: Restyle authentication and manager layouts**

Use `auth-electronics.webp` for a responsive login/register split layout. Replace raster logos and manager navigation imagery with the `NorrByte Market` text mark and Element Plus icons. Apply shared navy, ice-white, neutral-grey, and warm-orange CSS variables.

- [ ] **Step 7: Verify visual asset integrity and frontend behaviour**

```powershell
git ls-files "vue/src/assets/imgs/*"
rg -n "carousel-|bg[0-9]?\.(jpg|png)|logo\.png|right\.png|购物车|订单|收藏|店铺" vue/src
npm run lint
npm test
npm run build
```

Expected: exactly four tracked WebP images, no legacy image references, and all frontend checks pass.

- [ ] **Step 8: Browser smoke-test desktop and mobile layouts**

Run the backend and frontend, then verify `/front/home`, `/login`, `/register`, manager navigation, product search, category navigation, image fallbacks, and protected-route redirects at desktop and mobile widths. Expected: no console errors, horizontal overflow, unreadable copy, or broken static assets.

- [ ] **Step 9: Commit the visual redesign**

```powershell
git add vue/src
git commit -m "feat: redesign the NorrByte electronics storefront"
```

---

### Task 8: Update CI, documentation, and run final verification

**Files:**
- Modify: `.github/workflows/ci.yml`
- Modify: `.env.example`
- Modify: `README.md`
- Modify: plan checkboxes in this file as tasks complete

**Interfaces:**
- Consumes: the completed backend, migration, English UI, and image redesign.
- Produces: reproducible setup instructions and CI checks matching local verification.

- [ ] **Step 1: Update GitHub Actions runtimes and actions**

Use:

```yaml
- uses: actions/checkout@v6
- uses: actions/setup-java@v5
  with:
    distribution: temurin
    java-version: '25'
    cache: maven
- uses: actions/setup-node@v6
  with:
    node-version: '24'
```

Run backend verification through `./mvnw --batch-mode verify`. Add `npm run lint` before frontend tests and build. Add top-level `permissions: contents: read`.

- [ ] **Step 2: Rewrite README setup and project claims in English**

Document NorrByte Market's actual catalogue/seller-management scope, exact versions, Java 25 and Node 24 prerequisites, MySQL schema/user environment variables, Flyway migration behaviour, PowerShell run commands, role rules, test commands, and the four generated static assets. Do not claim cart, payment, delivery, price matching, machine learning, microservices, or production deployment.

- [ ] **Step 3: Verify environment templates contain no secrets**

Use placeholders in `.env.example` and README. Run:

```powershell
rg -n "YourStrongPassword123|wangchu|DB_PASSWORD\s*=\s*[^<]" .env.example README.md springboot/src vue/src
```

Expected: no real or previously shown database password is committed.

- [ ] **Step 4: Run complete local verification**

```powershell
./mvnw.cmd --batch-mode verify
./mvnw.cmd --batch-mode dependency:tree "-Dincludes=org.springframework.boot:*,org.mybatis*:*,com.github.pagehelper:*"
Set-Location vue
npm ci
npm run lint
npm test
npm run build
npm audit --omit=dev --audit-level=high
Set-Location ..
rg -n "[\p{Han}]" springboot/src/main/java springboot/src/main/resources/mapper springboot/src/test vue/src README.md
git diff --check origin/upgrade...HEAD
git status --short
```

Expected: all tests, lint, builds, and audit pass; no Chinese application-owned source remains; diff check is clean; only the V2 migration contains legacy Chinese values; working tree is clean after the final commit.

- [ ] **Step 5: Commit documentation and CI**

```powershell
git add .github/workflows/ci.yml .env.example README.md docs/superpowers/plans/2026-09-02-java25-english-storefront.md
git commit -m "docs: document the Java 25 NorrByte project"
```

- [ ] **Step 6: Perform final branch review**

Review `git log --oneline origin/upgrade..HEAD` and `git diff --stat origin/upgrade...HEAD`. Confirm no unrelated user files changed and do not push until the user explicitly asks for a remote update.

## Self-Review Results

- Spec coverage: runtime, dependencies, CI, English conversion, status migration, imagery, truthful copy, responsive design, tests, and documentation each have an implementation task.
- Placeholder scan: the plan contains no deferred implementation markers; each new interface and migration mapping is named explicitly.
- Type consistency: backend statuses are strings derived from `StatusEnum.code()`; frontend consumes the same uppercase codes; `formatSek(value)` and `useImageFallback(event)` names are consistent across production and tests.
