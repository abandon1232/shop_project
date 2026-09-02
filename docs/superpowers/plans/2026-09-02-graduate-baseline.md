# Graduate Baseline Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Convert the shop project into a secure, supported, and honestly described junior-level Java/Vue portfolio application.

**Architecture:** Keep the Spring Boot/MyBatis/MySQL modular monolith and separate SPA. Centralize request identity in the JWT interceptor, enforce authorization in services, replace the unsupported collaborative-filtering claim with a deterministic featured-products query, isolate file storage behind a small service, and migrate the UI to supported Vue 3 tooling.

**Tech Stack:** Java 21, Spring Boot 3.5.16, Maven, MyBatis, MySQL 8, Flyway, JUnit 5, Mockito, Node.js 24, Vue 3.5.42, Vite 8.2.2, Element Plus 2.14.5, Vue Router 4.6.4, Axios 1.20.0, Vitest 4.1.11, Vue Test Utils 2.5.0.

**Spec:** `docs/superpowers/specs/2026-09-02-graduate-baseline-design.md`

## Global Constraints

- Target Java 21 and run CI on Temurin 21.
- Use Node.js 24 for frontend development and CI.
- Keep a single Spring Boot backend and a separate Vue SPA.
- Preserve the existing `Result` JSON envelope and current user-visible flows.
- Accept JWTs only from the `token` HTTP header.
- Use plain-text product descriptions; do not accept or render product HTML.
- Do not add Docker, cloud services, microservices, Kafka, or a recommendation framework.
- Every production behaviour change must be preceded by a focused failing test.

---

### Task 1: Establish Java 21 and a Single Request Identity

**Files:**
- Modify: `springboot/pom.xml`
- Modify: `springboot/src/main/java/com/example/common/Constants.java`
- Modify: `springboot/src/main/java/com/example/common/config/JwtInterceptor.java`
- Modify: `springboot/src/main/java/com/example/utils/TokenUtils.java`
- Create: `springboot/src/test/java/com/example/common/config/JwtInterceptorTest.java`
- Create: `springboot/src/test/java/com/example/utils/TokenUtilsTest.java`

**Interfaces:**
- Produces: `Constants.CURRENT_USER`, the request attribute holding the authenticated `Account`.
- Produces: `TokenUtils.getCurrentUser()` returning that exact request account or throwing `CustomException(USER_NOT_LOGIN)`.
- Produces: `JwtInterceptor.preHandle(...)` accepting only the `token` header and setting `CURRENT_USER` after verification.

- [ ] **Step 1: Write the failing request-identity tests**

Add tests whose named break is reintroducing query-token authentication or reparsing identity downstream:

```java
@Test
void rejectsTokenProvidedOnlyAsQueryParameter() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setParameter(Constants.TOKEN, "query-token");

    CustomException error = assertThrows(CustomException.class,
            () -> interceptor.preHandle(request, new MockHttpServletResponse(), new Object()));

    assertEquals(ResultCodeEnum.TOKEN_INVALID_ERROR.code, error.getCode());
}

@Test
void returnsTheAccountStoredByTheInterceptor() {
    Account account = new Account();
    account.setId(7);
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(Constants.CURRENT_USER, account);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    assertSame(account, TokenUtils.getCurrentUser());
}

@Test
void failsClosedWhenNoAuthenticatedAccountExists() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

    CustomException error = assertThrows(CustomException.class, TokenUtils::getCurrentUser);

    assertEquals(ResultCodeEnum.USER_NOT_LOGIN.code, error.getCode());
}
```

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `mvn -Dtest=JwtInterceptorTest,TokenUtilsTest test`

Expected: query-token test fails because the interceptor accepts the parameter; request-attribute tests fail because `TokenUtils` reparses the header and returns a new empty account.

- [ ] **Step 3: Implement the single identity flow**

Set `<java.version>21</java.version>`, add `spring-boot-starter-validation`, add `String CURRENT_USER = "currentUser"`, delete the query-parameter fallback, and set the verified account:

```java
String token = request.getHeader(Constants.TOKEN);
if (ObjectUtil.isEmpty(token)) {
    throw new CustomException(ResultCodeEnum.TOKEN_INVALID_ERROR);
}
// existing decode, account lookup, signature verification, and authorize call
request.setAttribute(Constants.CURRENT_USER, account);
```

Replace static service injection and token reparsing in `TokenUtils`:

```java
public static Account getCurrentUser() {
    ServletRequestAttributes attributes =
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    if (attributes == null) {
        throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
    }
    Object account = attributes.getRequest().getAttribute(Constants.CURRENT_USER);
    if (account instanceof Account authenticated) {
        return authenticated;
    }
    throw new CustomException(ResultCodeEnum.USER_NOT_LOGIN);
}
```

- [ ] **Step 4: Run focused and full backend tests and verify GREEN**

Run: `mvn -Dtest=JwtInterceptorTest,TokenUtilsTest test`

Run: `mvn test`

Expected: all tests pass and the compiler reports release 21.

- [ ] **Step 5: Commit**

```bash
git add springboot/pom.xml springboot/src/main/java/com/example/common/Constants.java springboot/src/main/java/com/example/common/config/JwtInterceptor.java springboot/src/main/java/com/example/utils/TokenUtils.java springboot/src/test/java/com/example/common/config/JwtInterceptorTest.java springboot/src/test/java/com/example/utils/TokenUtilsTest.java
git commit -m "fix: centralize authenticated request identity"
```

### Task 2: Validate Authentication Requests and Protect Account Data

**Files:**
- Create: `springboot/src/main/java/com/example/controller/request/AuthRequest.java`
- Create: `springboot/src/main/java/com/example/controller/request/PasswordChangeRequest.java`
- Modify: `springboot/src/main/java/com/example/controller/WebController.java`
- Modify: `springboot/src/main/java/com/example/controller/BusinessController.java`
- Modify: `springboot/src/main/java/com/example/controller/UserController.java`
- Modify: `springboot/src/main/java/com/example/exception/GlobalExceptionHandler.java`
- Modify: `springboot/src/main/java/com/example/service/AdminService.java`
- Modify: `springboot/src/main/java/com/example/service/BusinessService.java`
- Modify: `springboot/src/main/java/com/example/service/UserService.java`
- Create: `springboot/src/test/java/com/example/controller/WebControllerTest.java`
- Create: `springboot/src/test/java/com/example/service/AccountAccessTest.java`

**Interfaces:**
- Produces: `AuthRequest.toAccount()` and `PasswordChangeRequest.toAccount()`.
- Produces: `BusinessService.selectAccessibleById(Integer)` and `UserService.selectAccessibleById(Integer)`.
- Preserves: administrator lookup methods used by `JwtInterceptor`.

- [ ] **Step 1: Write failing controller and account-access tests**

```java
@Test
void loginRejectsUnknownRole() {
    Account request = new Account();
    request.setUsername("alice");
    request.setPassword("secret123");
    request.setRole("OWNER");

    Result result = controller.login(AuthRequest.from(request));

    assertEquals(ResultCodeEnum.PARAM_ERROR.code, result.getCode());
}

@Test
void userCannotReadAnotherUserProfile() {
    bindCurrentAccount(4, RoleEnum.USER);

    CustomException error = assertThrows(CustomException.class,
            () -> userService.selectAccessibleById(5));

    assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
}

@Test
void administratorCanReadAnyBusinessProfile() {
    bindCurrentAccount(1, RoleEnum.ADMIN);
    Business stored = business(8);
    when(businessMapper.selectById(8)).thenReturn(stored);

    assertSame(stored, businessService.selectAccessibleById(8));
}
```

- [ ] **Step 2: Run the focused tests and verify RED**

Run: `mvn -Dtest=WebControllerTest,AccountAccessTest test`

Expected: compilation or assertions fail because validated request records and accessible lookup methods do not exist and unknown roles currently return success.

- [ ] **Step 3: Implement request validation and explicit role switching**

Create records with `@NotBlank` fields and conversion methods. Use `@Valid` in the controller and exhaustive switches:

```java
return switch (request.role()) {
    case "ADMIN" -> Result.success(adminService.login(request.toAccount()));
    case "BUSINESS" -> Result.success(businessService.login(request.toAccount()));
    case "USER" -> Result.success(userService.login(request.toAccount()));
    default -> Result.error(ResultCodeEnum.PARAM_ERROR);
};
```

Registration returns forbidden for `ADMIN`, delegates only for `BUSINESS` and `USER`, and returns `PARAM_ERROR` for every other value. Add validation-exception handlers that return `PARAM_ERROR` through the existing `Result` envelope.

- [ ] **Step 4: Implement self-or-admin profile access and supplied passwords**

Add access methods with this exact rule:

```java
public User selectAccessibleById(Integer id) {
    Account current = TokenUtils.getCurrentUser();
    if (!RoleEnum.ADMIN.name().equals(current.getRole())
            && !(RoleEnum.USER.name().equals(current.getRole())
            && Objects.equals(current.getId(), id))) {
        throw new CustomException(ResultCodeEnum.FORBIDDEN_ERROR);
    }
    return userMapper.selectById(id);
}
```

Implement the equivalent business rule, make the two controllers call the accessible methods, and make `AdminService.add`, `BusinessService.add`, and `UserService.add` throw `PARAM_LOST_ERROR` when password is blank instead of assigning `123`. Remove `USER_DEFAULT_PASSWORD`.

- [ ] **Step 5: Run focused and full tests and verify GREEN**

Run: `mvn -Dtest=WebControllerTest,AccountAccessTest test`

Run: `mvn test`

Expected: all tests pass; unknown roles and cross-account reads are rejected.

- [ ] **Step 6: Commit**

```bash
git add springboot/src/main/java/com/example/controller springboot/src/main/java/com/example/exception/GlobalExceptionHandler.java springboot/src/main/java/com/example/service springboot/src/main/java/com/example/common/Constants.java springboot/src/test/java/com/example/controller/WebControllerTest.java springboot/src/test/java/com/example/service/AccountAccessTest.java
git commit -m "fix: validate auth and protect account profiles"
```

### Task 3: Enforce Product Rules and Replace Fake Recommendations

**Files:**
- Create: `springboot/src/main/java/com/example/controller/request/GoodsRequest.java`
- Modify: `springboot/src/main/java/com/example/controller/GoodsController.java`
- Modify: `springboot/src/main/java/com/example/entity/Goods.java`
- Modify: `springboot/src/main/java/com/example/service/GoodsService.java`
- Modify: `springboot/src/main/java/com/example/mapper/GoodsMapper.java`
- Modify: `springboot/src/main/resources/mapper/GoodsMapper.xml`
- Delete: `springboot/src/main/java/com/example/entity/RelateDTO.java`
- Delete: `springboot/src/main/java/com/example/utils/CoreMath.java`
- Delete: `springboot/src/main/java/com/example/utils/UserCF.java`
- Create: `springboot/src/test/java/com/example/service/GoodsServiceTest.java`
- Create: `springboot/src/test/java/com/example/controller/GoodsValidationTest.java`

**Interfaces:**
- Produces: `GoodsRequest.toGoods()` with `BigDecimal price`.
- Produces: `GoodsService.featured()` returning `goodsMapper.selectFeatured(10)`.
- Produces: `GoodsMapper.selectFeatured(int limit)`.
- Replaces: `GET /goods/recommend` with `GET /goods/featured`.
- Removes: `GET /goods/selectTop15` and `GoodsMapper.selectTop15()` because the application has no sales events that could support a hot-sales ranking.

- [ ] **Step 1: Write failing authorization and approval tests**

```java
@Test
void batchDeleteChecksEveryProductBeforeDeletingAny() {
    bindBusiness(7);
    when(goodsMapper.selectById(11)).thenReturn(goods(11, 7));
    when(goodsMapper.selectById(12)).thenReturn(goods(12, 9));

    assertThrows(CustomException.class, () -> service.deleteBatch(List.of(11, 12)));

    verify(goodsMapper, never()).deleteById(anyInt());
}

@Test
void pendingBusinessCannotCreateProduct() {
    bindBusiness(7);
    when(businessMapper.selectById(7)).thenReturn(business(7, StatusEnum.CHECKING.status));

    CustomException error = assertThrows(CustomException.class,
            () -> service.add(goods(null, 7)));

    assertEquals(ResultCodeEnum.FORBIDDEN_ERROR.code, error.getCode());
    verify(goodsMapper, never()).insert(any());
}
```

- [ ] **Step 2: Run the service tests and verify RED**

Run: `mvn -Dtest=GoodsServiceTest test`

Expected: batch deletion performs at least one delete before discovering no ownership, and pending business creation reaches the mapper.

- [ ] **Step 3: Implement fail-before-write authorization**

Inject `BusinessMapper`, add `requireApprovedBusiness(Account)`, and validate every ID before deleting any:

```java
public void deleteBatch(List<Integer> ids) {
    Account current = TokenUtils.getCurrentUser();
    requireApprovedBusiness(current);
    ids.forEach(this::requireBusinessOwnership);
    ids.forEach(goodsMapper::deleteById);
}
```

Apply approval and ownership rules consistently to create, update, single delete, and batch delete. Administrators bypass business approval and ownership rules.

- [ ] **Step 4: Write failing featured-feed and validation tests**

```java
@Test
void featuredReturnsTheTenNewestProducts() {
    List<Goods> expected = IntStream.rangeClosed(1, 10)
            .mapToObj(id -> goods(21 - id, 7))
            .toList();
    when(goodsMapper.selectFeatured(10)).thenReturn(expected);

    assertEquals(expected, service.featured());
}

@Test
void rejectsNegativeProductPrice() throws Exception {
    mockMvc.perform(post("/goods/add")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\":\"Milk\",\"price\":-1,\"count\":2,\"typeId\":1}"))
            .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));
}
```

- [ ] **Step 5: Run the new tests and verify RED**

Run: `mvn -Dtest=GoodsServiceTest,GoodsValidationTest test`

Expected: featured API and validated request type are missing; negative prices are accepted by the existing entity endpoint.

- [ ] **Step 6: Implement simple product validation and feed**

Use `BigDecimal` for `Goods.price`. Define `GoodsRequest` with `@NotBlank String name`, `@NotNull @DecimalMin("0.00") BigDecimal price`, `@NotNull @Min(0) Integer count`, and `@NotNull @Positive Integer typeId`. Add `@Valid` to add/update endpoints and `@Min(1) @Max(100)` to page-size parameters.

Add the mapper query:

```xml
<select id="selectFeatured" resultType="com.example.entity.Goods">
    select goods.*, type.name as typeName, business.name as businessName
    from goods
    left join type on goods.type_id = type.id
    left join business on goods.business_id = business.id
    order by goods.id desc
    limit #{limit}
</select>
```

Delete the collaborative-filtering classes and expose only `/goods/featured`.
Delete `selectTop15` from the controller, service, and mapper. Keep `count` as a required non-negative stock quantity.

- [ ] **Step 7: Run focused and full backend tests and verify GREEN**

Run: `mvn -Dtest=GoodsServiceTest,GoodsValidationTest test`

Run: `mvn test`

Expected: all tests pass; fake recommendation code is no longer compiled.

- [ ] **Step 8: Commit**

```bash
git add springboot/src/main/java/com/example/controller springboot/src/main/java/com/example/entity springboot/src/main/java/com/example/service/GoodsService.java springboot/src/main/java/com/example/mapper/GoodsMapper.java springboot/src/main/java/com/example/utils springboot/src/main/resources/mapper/GoodsMapper.xml springboot/src/test/java/com/example/service/GoodsServiceTest.java springboot/src/test/java/com/example/controller/GoodsValidationTest.java
git commit -m "fix: enforce product rules and add featured feed"
```

### Task 4: Make File Storage Safe and Truthful

**Files:**
- Create: `springboot/src/main/java/com/example/service/FileStorageService.java`
- Modify: `springboot/src/main/java/com/example/controller/FileController.java`
- Create: `springboot/src/test/java/com/example/service/FileStorageServiceTest.java`
- Create: `springboot/src/test/java/com/example/controller/FileControllerTest.java`

**Interfaces:**
- Produces: `String FileStorageService.storeImage(MultipartFile file)`.
- Produces: `Resource FileStorageService.load(String storedName)`.
- Produces: `void FileStorageService.delete(String storedName)`.
- Removes: `POST /files/wang/upload`.

- [ ] **Step 1: Write failing storage tests**

```java
@Test
void rejectsNonImageContentEvenWhenMimeTypeClaimsPng() {
    MockMultipartFile file = new MockMultipartFile(
            "file", "attack.png", "image/png", "<script>alert(1)</script>".getBytes(UTF_8));

    assertThrows(CustomException.class, () -> service.storeImage(file));
}

@Test
void storesDecodedImageUnderGeneratedName() {
    MockMultipartFile file = validOnePixelPng("../../avatar.png");

    String storedName = service.storeImage(file);

    assertTrue(storedName.matches("[0-9a-f-]{36}\\.png"));
    assertTrue(Files.exists(tempDirectory.resolve(storedName)));
}

@Test
void refusesPathsOutsideTheUploadDirectory() {
    assertThrows(CustomException.class, () -> service.delete("../application.yml"));
}
```

- [ ] **Step 2: Run tests and verify RED**

Run: `mvn -Dtest=FileStorageServiceTest,FileControllerTest test`

Expected: service classes are missing and the existing controller trusts the original filename and reports success after failed writes.

- [ ] **Step 3: Implement storage behind a bounded service**

Resolve every operation from `uploadRoot.toAbsolutePath().normalize()`. Reject empty files and files over `5 * 1024 * 1024` bytes. Decode with `ImageIO.read(...)`; accept only decoded JPEG, PNG, or GIF data; derive the extension from the verified format; write to a UUID filename; catch `IOException`, log it, and throw `CustomException(SYSTEM_ERROR)`.

For reads and deletes, resolve and normalize the requested stored name and require `resolved.startsWith(uploadRoot)`. Return a Spring `Resource` for downloads and a normal `Result` from deletion. The controller builds the URL only after `storeImage` returns successfully.

- [ ] **Step 4: Run focused and full tests and verify GREEN**

Run: `mvn -Dtest=FileStorageServiceTest,FileControllerTest test`

Run: `mvn test`

Expected: unsafe inputs fail, successful files remain inside the temporary upload directory, and all tests pass.

- [ ] **Step 5: Commit**

```bash
git add springboot/src/main/java/com/example/controller/FileController.java springboot/src/main/java/com/example/service/FileStorageService.java springboot/src/test/java/com/example/service/FileStorageServiceTest.java springboot/src/test/java/com/example/controller/FileControllerTest.java
git commit -m "fix: validate and isolate image storage"
```

### Task 5: Migrate the Frontend Toolchain and Core Runtime to Vue 3

**Files:**
- Modify: `vue/package.json`
- Modify: `vue/package-lock.json`
- Create: `vue/vite.config.js`
- Create: `vue/vitest.config.js`
- Delete: `vue/babel.config.js`
- Delete: `vue/vue.config.js`
- Modify: `vue/index.html`
- Modify: `vue/src/main.js`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/utils/request.js`
- Create: `vue/src/router/index.test.js`
- Create: `vue/src/utils/request.test.js`

**Interfaces:**
- Produces: `createRouter()` configured with `createWebHistory(import.meta.env.BASE_URL)`.
- Produces: unique route names `ManagerHome`, `ManagerType`, `StoreHome`, and `StoreType`.
- Produces: exported `applyAuthHeader(config, storage)` used by the Axios interceptor.

- [ ] **Step 1: Install the supported toolchain and test runner**

Set scripts to `dev: vite`, `serve: vite`, `build: vite build`, and `test: vitest run`. Use Vue `3.5.42`, Vite `8.2.2`, plugin-vue `6.0.8`, Element Plus `2.14.5`, Vue Router `4.6.4`, Axios `1.20.0`, Vue Test Utils `2.5.0`, and Vitest `4.1.11`. Remove Vue CLI, Element UI, and `vue-template-compiler`; keep wangEditor only until Task 6 removes the rich-text feature.

Run: `npm install`

Expected: a regenerated lockfile containing Vue 3 and no `wangeditor` or `element-ui` package.

- [ ] **Step 2: Write failing router and header tests**

```javascript
it('has unique route names', () => {
  const names = router.getRoutes().map(route => route.name).filter(Boolean)
  expect(new Set(names).size).toBe(names.length)
})

it('redirects a signed-out user away from protected routes', async () => {
  localStorage.removeItem('xm-user')
  await router.push('/home')
  await router.isReady()
  expect(router.currentRoute.value.path).toBe('/login')
})

it('adds only the token header from stored account data', () => {
  const storage = { getItem: () => JSON.stringify({ token: 'abc' }) }
  const config = applyAuthHeader({ headers: {} }, storage)
  expect(config.headers).toMatchObject({ token: 'abc' })
  expect(config.params).toBeUndefined()
})
```

- [ ] **Step 3: Run tests and verify RED**

Run: `npm test -- src/router/index.test.js src/utils/request.test.js`

Expected: Vue Router 3 construction is incompatible, route names are duplicated, guard behaviour is disabled, and `applyAuthHeader` does not exist.

- [ ] **Step 4: Implement the Vue 3 runtime**

Use `createApp(App).use(router).use(ElementPlus)`, attach `$request` and `$baseUrl` through `app.config.globalProperties`, and change environment access to `import.meta.env.VITE_API_BASE_URL`. Convert the three storefront carousel `require(...)` expressions to static ES imports in this runtime task so component tests can load the Home view under Vite.

Build the router with Vue Router 4, use `/:pathMatch(.*)*` for the 404 route, give every route a unique name, and enable a guard with these rules:

```javascript
if (to.meta.public) return true
const user = JSON.parse(localStorage.getItem('xm-user') || '{}')
if (!user.token) return { path: '/login', query: { redirect: to.fullPath } }
if (to.path === '/') return user.role === 'USER' ? '/front/home' : '/home'
return true
```

Export and use `applyAuthHeader`. Do not place tokens in query parameters.

- [ ] **Step 5: Run focused tests and verify GREEN**

Run: `npm test -- src/router/index.test.js src/utils/request.test.js`

Expected: all core runtime tests pass.

- [ ] **Step 6: Commit**

```bash
git add vue/package.json vue/package-lock.json vue/vite.config.js vue/vitest.config.js vue/index.html vue/src/main.js vue/src/router/index.js vue/src/utils/request.js vue/src/router/index.test.js vue/src/utils/request.test.js vue/babel.config.js vue/vue.config.js
git commit -m "build: migrate frontend runtime to Vue 3"
```

### Task 6: Migrate Views and Remove Unsafe Rich Text

**Files:**
- Modify: every `.vue` file under `vue/src/views/`
- Modify: `vue/src/views/manager/Goods.vue`
- Modify: `vue/src/views/front/Home.vue`
- Modify: `vue/src/views/front/Search.vue`
- Modify: `vue/src/views/Front.vue`
- Create: `vue/src/views/manager/Goods.test.js`
- Create: `vue/src/views/front/Home.test.js`

**Interfaces:**
- Preserves: current page routes and CRUD calls except the intentional `/goods/featured` replacement.
- Removes: all `v-html` product rendering and wangEditor integration.

- [ ] **Step 1: Write failing security and featured-feed component tests**

Mount `Goods.vue` with Element Plus components stubbed and assert on rendered behaviour rather than source text:

```javascript
it('renders a product description as text', async () => {
  const wrapper = mount(Goods, { global: { stubs: elementStubs } })
  await wrapper.vm.viewEditor('<img src=x onerror=alert(1)>')
  expect(wrapper.text()).toContain('<img src=x onerror=alert(1)>')
  expect(wrapper.find('img[src="x"]').exists()).toBe(false)
})
```

Mount `Home.vue` with a fake request boundary that returns one featured product, wait for mounted promises, and assert the product appears under `最新商品`. This catches a regression back to the removed recommendation endpoint because the fake rejects every URL except `/goods/featured`, `/goods/selectTop15`, and `/type/selectAll`.

- [ ] **Step 2: Run component tests and verify RED**

Run: `npm test -- src/views/manager/Goods.test.js src/views/front/Home.test.js`

Expected: Goods renders injected markup with `v-html`, imports wangEditor, and Home calls `/goods/recommend` with the `猜你喜欢` label.

- [ ] **Step 3: Apply Vue 3 template compatibility changes**

Across the 21 views:

- change `:visible.sync="value"` to `v-model="value"`;
- change `slot-scope="scope"` to `<template #default="scope">`;
- change `slot="footer"` to `<template #footer>`;
- change dropdown/menu title slots to Vue 3 `#dropdown` and `#title` syntax;
- remove `.native` from component click handlers;
- change Element Plus button size `mini` to `small`;
- replace `this.$set(object, key, value)` with `object[key] = value`;
- replace CommonJS image `require(...)` calls with top-level ES imports;
- remove obsolete `el-icon-*` class-only icons where no icon component is needed.

Keep the Options API and existing component methods so the migration does not become an application rewrite.

- [ ] **Step 4: Remove rich text and update storefront wording**

In `Goods.vue`, remove the editor import, global editor variable, initialization function, editor DOM node, and wangEditor upload URL. Bind `form.description` to `<el-input type="textarea" :rows="6">`, add a required non-negative numeric input for `form.count`, and label the table column `库存数量`. Render `viewData` with interpolation inside a `white-space: pre-wrap` container. Remove `wangeditor` from `package.json` and regenerate the lockfile after the component no longer imports it.

Remove Home's unsupported hot-sales section and its `/goods/selectTop15` request. Change Home and Search from `/goods/recommend` to `/goods/featured`, rename the remaining section to `最新商品`, change `PORDUCT catergary` to `商品分类`, change `搜素` to `搜索`, and initialize `Front.vue` search name as `name: ''`.

- [ ] **Step 5: Run focused tests, full tests, and build**

Run: `npm test`

Run: `npm run build`

Run: `rg "v-html|wangeditor|goods/recommend|:visible\\.sync|slot-scope|@click\\.native|process\\.env|require\\(" vue/src vue/package.json`

Expected: tests and build pass; the scan returns no obsolete or unsafe patterns.

- [ ] **Step 6: Commit**

```bash
git add vue/src vue/package.json vue/package-lock.json
git commit -m "fix: migrate views and render descriptions safely"
```

### Task 7: Align CI, Documentation, and Final Evidence

**Files:**
- Modify: `.github/workflows/ci.yml`
- Modify: `README.md`
- Modify: `.env.example`
- Create: `docs/cv-project-description.md`

**Interfaces:**
- Produces: CI running Maven verification, Vitest, frontend build, and production audit.
- Produces: honest English CV wording without collaborative-filtering claims.

- [ ] **Step 1: Update CI to the supported baseline**

Use Temurin Java 21 and Node.js 24. In the frontend job run `npm ci`, `npm test`, `npm run build`, and `npm audit --omit=dev --audit-level=high`.

- [ ] **Step 2: Rewrite documentation against implemented behaviour**

README must describe exactly these features: Java 21/Spring Boot modular monolith, MyBatis/MySQL/Flyway, Vue 3/Vite/Element Plus, header JWT, role and ownership rules, Bean Validation, BCrypt, safe image uploads, featured products, and automated tests. Include executable setup commands and environment variable names from `.env.example` and `application.yml`.

Create `docs/cv-project-description.md` with this wording:

```text
Product Management and Storefront Platform — Java 21, Spring Boot, MyBatis, MySQL, Vue 3
- Built a role-based REST application for administrator, merchant, and customer workflows, with JWT authentication, BCrypt password storage, ownership checks, and validated product operations.
- Managed schema changes with Flyway and added automated backend/frontend verification through Maven, Vitest, and GitHub Actions.
- Implemented pagination, search, safe image uploads, and a deterministic featured-products feed in a maintainable modular monolith.
```

- [ ] **Step 3: Run the complete verification matrix**

Run: `mvn clean verify`

Run: `npm ci`

Run: `npm test`

Run: `npm run build`

Run: `npm audit --omit=dev --audit-level=high`

Run a browser smoke test for login, protected-route redirect, administrator navigation, merchant product editing, plain-text description display, and the storefront featured feed. Inspect the console and require zero duplicate-route warnings and zero uncaught application errors.

- [ ] **Step 4: Review the final diff and requirements**

Run: `git diff --check`

Run: `git status --short`

Read each acceptance criterion in the design spec and map it to a passing automated test or browser observation. Do not claim completion for criteria without evidence.

- [ ] **Step 5: Commit**

```bash
git add .github/workflows/ci.yml README.md .env.example docs/cv-project-description.md
git commit -m "docs: align portfolio claims with verified project"
```
