# Catalogue, Ordering, and Management Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the stale demo catalogue with eighteen illustrated products, add product details and persisted ordering, and provide clear role-specific administrator and seller management screens.

**Architecture:** Keep the existing Vue single-page application and Spring Boot/MyBatis monolith. Add one transactional order service and two focused REST controllers, reuse the existing JWT account context for role scoping, store catalogue images as Vue public assets, and use Flyway for deterministic catalogue and order-schema changes.

**Tech Stack:** Java 25, Spring Boot 4.1.1, MyBatis 4.1.0, Flyway, MySQL 8, Vue 3.5, Vue Router 5, Element Plus 2.14, Vite 8, Vitest 4.

**Spec:** `docs/superpowers/specs/2026-09-02-catalog-orders-management-design.md`

## Global Constraints

- Keep all source code, user-facing copy, seeded data, and comments in English.
- Display prices only as Swedish kronor, for example `1 299 kr`; never render a unit suffix.
- Keep the existing monolithic Vue + Spring Boot + MyBatis design and do not add payment, microservice, broker, or client-state frameworks.
- Delete only existing catalogue rows (`goods` and `type`); preserve administrator, seller, and customer accounts.
- Generate original catalogue images with no brands, logos, text, or watermarks.
- Enforce order ownership and stock rules on the backend rather than trusting the browser.

## File Structure

New backend files:

- `springboot/src/main/resources/db/migration/V6__replace_demo_catalog.sql` — destructive replacement of catalogue rows and deterministic English seed data.
- `springboot/src/main/resources/db/migration/V7__create_customer_orders.sql` — order table, indexes, and foreign keys.
- `springboot/src/main/java/com/example/common/enums/OrderStatus.java` — allowed persisted order statuses.
- `springboot/src/main/java/com/example/entity/CustomerOrder.java` — order persistence and API view model.
- `springboot/src/main/java/com/example/controller/request/PlaceOrderRequest.java` — validated purchase input.
- `springboot/src/main/java/com/example/controller/request/OrderStatusRequest.java` — validated fulfilment input.
- `springboot/src/main/java/com/example/mapper/OrderMapper.java` and `springboot/src/main/resources/mapper/OrderMapper.xml` — order persistence and role-filtered queries.
- `springboot/src/main/java/com/example/service/OrderService.java` — transaction, stock, scope, and status-transition rules.
- `springboot/src/main/java/com/example/controller/OrderController.java` — order HTTP endpoints.
- `springboot/src/main/java/com/example/controller/response/DashboardSummary.java` — fixed dashboard response shape.
- `springboot/src/main/java/com/example/service/DashboardService.java` and `springboot/src/main/java/com/example/controller/DashboardController.java` — role-scoped management metrics.
- `springboot/src/test/java/com/example/db/CatalogMigrationTest.java`, `springboot/src/test/java/com/example/service/OrderServiceTest.java`, and `springboot/src/test/java/com/example/service/DashboardServiceTest.java` — backend contracts.

New frontend files:

- `vue/src/components/ProductCard.vue` — one clickable, unit-free catalogue card.
- `vue/src/components/ProductCard.test.js` — card presentation and navigation tests.
- `vue/src/views/front/ProductDetail.vue` and `vue/src/views/front/ProductDetail.test.js` — detail and purchase experience.
- `vue/src/views/front/Orders.vue` and `vue/src/views/front/Orders.test.js` — customer order history.
- `vue/src/views/manager/Orders.vue` and `vue/src/views/manager/Orders.test.js` — administrator/seller fulfilment table.
- `vue/src/constants/orderStatus.js` and `vue/src/constants/orderStatus.test.js` — labels and allowed UI actions.
- `vue/public/images/catalog/categories/*.webp` — six category images.
- `vue/public/images/catalog/products/*.webp` — eighteen product images.

Existing files modified together:

- Goods mapper/service/controller files expose a purchasable-product query and atomic stock updates.
- `vue/src/views/front/Home.vue`, `Type.vue`, and `Search.vue` consume `ProductCard`.
- `vue/src/router/index.js`, `vue/src/views/Front.vue`, and their tests expose detail and order navigation.
- `vue/src/views/Manager.vue`, `manager/Home.vue`, `manager/Goods.vue`, `manager/Type.vue`, `manager/Business.vue`, and `vue/src/assets/css/manager.css` form the refreshed management experience.
- Mapper interfaces for goods, types, businesses, and users expose simple aggregate counts used by `DashboardService`.
- `README.md` documents catalogue replacement, demo ordering, and the two-terminal run flow.

---

### Task 1: Replace the Catalogue and Add Original Images

**Files:**
- Create: `springboot/src/test/java/com/example/db/CatalogMigrationTest.java`
- Create: `springboot/src/main/resources/db/migration/V6__replace_demo_catalog.sql`
- Modify: `springboot/src/main/java/com/example/mapper/GoodsMapper.java`
- Modify: `springboot/src/main/resources/mapper/GoodsMapper.xml`
- Modify: `springboot/src/main/java/com/example/service/GoodsService.java`
- Modify: `springboot/src/main/java/com/example/controller/GoodsController.java`
- Create: `vue/public/images/catalog/categories/computers-tablets.webp`
- Create: `vue/public/images/catalog/categories/phones-wearables.webp`
- Create: `vue/public/images/catalog/categories/tv-audio.webp`
- Create: `vue/public/images/catalog/categories/gaming.webp`
- Create: `vue/public/images/catalog/categories/kitchen-appliances.webp`
- Create: `vue/public/images/catalog/categories/smart-home.webp`
- Create: eighteen files under `vue/public/images/catalog/products/` matching the slugs in Step 3.

**Interfaces:**
- Produces: `GoodsMapper.selectPurchasableById(Integer id)`, `GoodsMapper.decreaseStock(Integer id, Integer quantity)`, and `GoodsMapper.increaseStock(Integer id, Integer quantity)`.
- Produces: `GoodsService.selectPurchasableById(Integer id)` for both detail display and Task 3 order creation.
- Produces: public asset URLs rooted at `/images/catalog/`.

- [ ] **Step 1: Write the failing catalogue contract test**

Create a test that loads `db/migration/V6__replace_demo_catalog.sql`, asserts that it contains `DELETE FROM goods` before `DELETE FROM type`, and asserts every exact category and product name listed in Step 3. Also assert all 24 expected `.webp` paths are present in the migration or filesystem.

```java
@Test
void migrationReplacesOldCatalogueWithSixCategoriesAndEighteenProducts() throws IOException {
    String sql = new ClassPathResource("db/migration/V6__replace_demo_catalog.sql")
            .getContentAsString(StandardCharsets.UTF_8);
    assertTrue(sql.indexOf("DELETE FROM goods") < sql.indexOf("DELETE FROM type"));
    assertAll(CATEGORY_NAMES.stream().map(name -> () -> assertTrue(sql.contains(name))));
    assertAll(PRODUCT_NAMES.stream().map(name -> () -> assertTrue(sql.contains(name))));
    assertEquals(18, PRODUCT_NAMES.size());
}
```

- [ ] **Step 2: Run the catalogue test and confirm the missing migration failure**

Run: `cd springboot; .\mvnw.cmd -Dtest=CatalogMigrationTest test`

Expected: FAIL because `V6__replace_demo_catalog.sql` does not exist.

- [ ] **Step 3: Add the deterministic English catalogue migration**

Write `V6__replace_demo_catalog.sql` with this exact inventory:

| Category | Product slug | Product name | Price (SEK) | Stock |
|---|---|---|---:|---:|
| Computers & Tablets | `nordbook-air-14` | NordBook Air 14 Laptop | 10990 | 14 |
| Computers & Tablets | `fjordview-27-monitor` | FjordView 27 QHD Monitor | 3490 | 22 |
| Computers & Tablets | `birch-wireless-keyboard` | Birch Wireless Keyboard | 899 | 35 |
| Phones & Wearables | `aurora-phone` | Aurora 5G Smartphone | 7490 | 18 |
| Phones & Wearables | `pulse-smartwatch` | Pulse Active Smartwatch | 2290 | 26 |
| Phones & Wearables | `pocket-usbc-charger` | Pocket 65W USB-C Charger | 549 | 48 |
| TV & Audio | `horizon-55-tv` | Horizon 55 4K Smart TV | 8990 | 10 |
| TV & Audio | `quietwave-headphones` | QuietWave Wireless Headphones | 1990 | 31 |
| TV & Audio | `roombeat-speaker` | RoomBeat Portable Speaker | 1190 | 29 |
| Gaming | `arcade-controller` | Arcade Pro Wireless Controller | 849 | 24 |
| Gaming | `velocity-headset` | Velocity Surround Gaming Headset | 1290 | 17 |
| Gaming | `glide-gaming-mouse` | Glide Precision Gaming Mouse | 699 | 40 |
| Kitchen Appliances | `compact-air-fryer` | Compact Air Fryer 5L | 1590 | 19 |
| Kitchen Appliances | `barista-coffee-maker` | Barista Filter Coffee Maker | 1290 | 16 |
| Kitchen Appliances | `nordic-kettle` | Nordic Temperature Kettle 1.7L | 799 | 27 |
| Smart Home | `glow-smart-bulbs` | Glow Smart Bulb Starter Set | 649 | 42 |
| Smart Home | `homeguard-camera` | HomeGuard Indoor Camera | 999 | 21 |
| Smart Home | `climate-sensor` | Climate Mini Sensor | 449 | 38 |

The migration must run these operations in order:

```sql
DELETE FROM goods;
DELETE FROM type;
SET @catalog_business_id = (
    SELECT id FROM business WHERE status = 'APPROVED' ORDER BY id LIMIT 1
);
```

Insert six category rows with `/images/catalog/categories/<slug>.webp`. Insert eighteen product rows with clear one- or two-sentence English descriptions, an empty `unit`, the table price and stock, category IDs resolved by category name, `@catalog_business_id`, and `/images/catalog/products/<slug>.webp`.

- [ ] **Step 4: Generate and save the 24 catalogue images**

Use the built-in image generation tool once per asset. Product prompt template:

```text
Use case: product-mockup
Asset type: square ecommerce catalogue product image
Primary request: studio catalogue photograph of <exact product subject from the table>
Scene/backdrop: warm off-white seamless background with a pale Nordic-grey surface
Style/medium: photorealistic Scandinavian electronics retail photography
Composition/framing: centered three-quarter product view, full object visible, generous breathing room
Lighting/mood: soft daylight studio lighting, clean and trustworthy
Color palette: graphite, soft white, muted blue accents
Constraints: one product family only; no people; no logos; no text; no watermark
```

Category prompt template:

```text
Use case: product-mockup
Asset type: landscape ecommerce category tile
Primary request: curated still life representing <exact category name>
Scene/backdrop: bright Scandinavian home setting with restrained decor
Style/medium: photorealistic premium retail catalogue photography
Composition/framing: a balanced group of three relevant products with safe crop space
Lighting/mood: natural northern daylight, calm and welcoming
Constraints: no people; no logos; no text; no watermark
```

Inspect each result, copy it into the exact public asset path, and convert to WebP only with a lossless/quality-preserving image utility if the generated format differs.

- [ ] **Step 5: Add public-product and atomic-stock mapper methods**

Use a left join so platform products remain public:

```sql
where goods.id = #{id}
  and (goods.business_id is null or business.status = 'APPROVED')
```

Use guarded stock mutation:

```java
@Update("update goods set count = count - #{quantity} where id = #{id} and count >= #{quantity}")
int decreaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);

@Update("update goods set count = count + #{quantity} where id = #{id}")
int increaseStock(@Param("id") Integer id, @Param("quantity") Integer quantity);
```

Change featured, category, and search queries from inner seller joins to left joins with the same platform-or-approved predicate.

- [ ] **Step 6: Route public detail reads through the purchasable query**

Implement:

```java
public Goods selectPurchasableById(Integer id) {
    Goods goods = goodsMapper.selectPurchasableById(id);
    if (goods == null) {
        throw new CustomException(ResultCodeEnum.PRODUCT_NOT_FOUND);
    }
    return goods;
}
```

Make `GET /goods/selectById?id=...` call this method. Add `PRODUCT_NOT_FOUND("4041", "Product not found")` to `ResultCodeEnum`.

- [ ] **Step 7: Run the catalogue contract and existing goods tests**

Run: `cd springboot; .\mvnw.cmd -Dtest=CatalogMigrationTest,GoodsServiceTest,GoodsValidationTest test`

Expected: PASS.

- [ ] **Step 8: Commit the catalogue slice**

```powershell
git add springboot/src vue/public/images/catalog
git commit -m "feat: replace demo catalogue with illustrated products"
```

### Task 2: Add Reusable Product Cards and the Detail Page

**Files:**
- Create: `vue/src/components/ProductCard.vue`
- Create: `vue/src/components/ProductCard.test.js`
- Create: `vue/src/views/front/ProductDetail.vue`
- Create: `vue/src/views/front/ProductDetail.test.js`
- Modify: `vue/src/views/front/Home.vue`
- Modify: `vue/src/views/front/Type.vue`
- Modify: `vue/src/views/front/Search.vue`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/router/index.test.js`

**Interfaces:**
- Consumes: `GET /goods/selectById?id=<integer>` and `/images/catalog/**` from Task 1.
- Produces: `ProductCard` with required `product` prop and `select` emit.
- Produces: public route `/front/product/:id` named `ProductDetail`.

- [ ] **Step 1: Write failing product-card tests**

Mount a product containing `unit: 'p'`. Verify its text contains `1 299,00 kr`, does not contain `/ p`, renders the product image/name/category, and emits the product when clicked.

```js
expect(wrapper.text()).toMatch(/1[\s\u00a0]299,00\s*kr/)
expect(wrapper.text()).not.toContain('/ p')
await wrapper.get('article').trigger('click')
expect(wrapper.emitted('select')[0][0]).toEqual(product)
```

- [ ] **Step 2: Run the component test and confirm it fails**

Run: `cd vue; npm test -- src/components/ProductCard.test.js`

Expected: FAIL because `ProductCard.vue` is missing.

- [ ] **Step 3: Implement the focused product card**

The card imports `formatSek`, displays one image with the current fallback handler, category, product name, price, and an availability label. Add keyboard support using a real `<button>` or an article containing a full-width button; do not attach click behavior to an inaccessible `<div>`.

- [ ] **Step 4: Write failing detail-page and route tests**

Assert the router contains `/front/product/:id` with `meta.public === true`. Mount `ProductDetail` with a mocked product response and verify seller fallback, stock, description, price without `/ p`, quantity controls, and `Buy now`.

- [ ] **Step 5: Run the detail tests and confirm they fail**

Run: `cd vue; npm test -- src/router/index.test.js src/views/front/ProductDetail.test.js`

Expected: FAIL because the route and view are missing.

- [ ] **Step 6: Implement the public detail page**

Load the product from `this.$route.params.id`, clamp quantity between 1 and current stock, disable purchase at stock zero, and expose a `buyNow()` method for Task 4. Until Task 4 connects the API, `buyNow()` redirects guests and returns for signed-in users without writing fake order data.

```js
if (!this.user.token) {
  return this.$router.push({ path: '/login', query: { redirect: this.$route.fullPath } })
}
```

- [ ] **Step 7: Replace repeated cards on all catalogue pages**

Import `ProductCard` in `Home.vue`, `Type.vue`, and `Search.vue`; route selection with:

```js
openProduct(product) {
  this.$router.push({ name: 'ProductDetail', params: { id: product.id } })
}
```

Remove every rendered `item.unit`, `.unit`, and `.price-unit` block from those views.

- [ ] **Step 8: Run frontend route, component, and catalogue tests**

Run: `cd vue; npm test -- src/router/index.test.js src/components/ProductCard.test.js src/views/front/Home.test.js src/views/front/ProductDetail.test.js`

Expected: PASS.

- [ ] **Step 9: Commit the browsing slice**

```powershell
git add vue/src
git commit -m "feat: add product details and clickable catalogue cards"
```

### Task 3: Implement Transactional Orders

**Files:**
- Create: `springboot/src/main/resources/db/migration/V7__create_customer_orders.sql`
- Create: `springboot/src/main/java/com/example/common/enums/OrderStatus.java`
- Create: `springboot/src/main/java/com/example/entity/CustomerOrder.java`
- Create: `springboot/src/main/java/com/example/controller/request/PlaceOrderRequest.java`
- Create: `springboot/src/main/java/com/example/controller/request/OrderStatusRequest.java`
- Create: `springboot/src/main/java/com/example/mapper/OrderMapper.java`
- Create: `springboot/src/main/resources/mapper/OrderMapper.xml`
- Create: `springboot/src/main/java/com/example/service/OrderService.java`
- Create: `springboot/src/main/java/com/example/controller/OrderController.java`
- Create: `springboot/src/test/java/com/example/service/OrderServiceTest.java`
- Modify: `springboot/src/main/java/com/example/common/enums/ResultCodeEnum.java`

**Interfaces:**
- Consumes: `GoodsService.selectPurchasableById`, `GoodsMapper.decreaseStock`, and `GoodsMapper.increaseStock` from Task 1.
- Produces: `CustomerOrder placeOrder(PlaceOrderRequest request)`, `PageInfo<CustomerOrder> selectPage(int pageNum, int pageSize)`, and `void updateStatus(OrderStatusRequest request)`.
- Produces: authenticated endpoints `POST /orders/add`, `GET /orders/selectPage`, and `PUT /orders/status`.

- [ ] **Step 1: Write failing order-service tests**

Use Mockito and the existing `MockHttpServletRequest` account-binding pattern. Cover these exact behaviours:

```java
@Test void customerOrderUsesServerPriceAndDecreasesStock() { }
@Test void insufficientStockDoesNotInsertOrder() { }
@Test void nonCustomerCannotPlaceOrder() { }
@Test void sellerPageIsRestrictedToAuthenticatedSellerId() { }
@Test void customerPageIsRestrictedToAuthenticatedCustomerId() { }
@Test void sellerCannotUpdateAnotherSellersOrder() { }
@Test void placedOrderCanMoveToProcessing() { }
@Test void shippedOrderCannotBeCancelled() { }
@Test void cancellingPlacedOrderRestoresStockOnce() { }
```

Capture the inserted order and assert `unitPrice`, `totalPrice`, product snapshots, user ID, seller ID, `PLACED`, and an `NB-` order number.

- [ ] **Step 2: Run the order test and confirm missing-type failures**

Run: `cd springboot; .\mvnw.cmd -Dtest=OrderServiceTest test`

Expected: compilation FAIL because the order types do not exist.

- [ ] **Step 3: Add the order schema and model**

`customer_order` uses `DECIMAL(12,2)` amounts, `INT` quantity, `VARCHAR(32)` status, and `TIMESTAMP(6)` timestamps. Foreign keys use `ON DELETE SET NULL`, while snapshot name/image fields remain non-null where needed. Add indexes on order number, user ID, business ID, status, and created time.

`OrderStatus` contains only:

```java
PLACED, PROCESSING, SHIPPED, CANCELLED
```

Requests are records:

```java
public record PlaceOrderRequest(@NotNull @Positive Integer goodsId,
                                @NotNull @Min(1) @Max(99) Integer quantity) {}

public record OrderStatusRequest(@NotNull @Positive Integer id,
                                 @NotNull OrderStatus status) {}
```

- [ ] **Step 4: Add mapper queries and role filters**

`OrderMapper.selectAll(CustomerOrder filter)` joins current customer, seller, and product rows while selecting snapshot fields from `customer_order`. Filter by `userId` or `businessId` when supplied and order by `created_at desc, id desc`.

Provide:

```java
int insert(CustomerOrder order);
CustomerOrder selectById(Integer id);
List<CustomerOrder> selectAll(CustomerOrder filter);
int updateStatus(@Param("id") Integer id, @Param("status") String status);
```

- [ ] **Step 5: Implement order creation and status transitions**

Annotate `placeOrder` and `updateStatus` with `@Transactional`. Calculate totals with:

```java
BigDecimal total = goods.getPrice().multiply(BigDecimal.valueOf(request.quantity()));
```

Treat a zero-row guarded stock update as `INSUFFICIENT_STOCK("4091", "Not enough stock is available")`. Add `ORDER_NOT_FOUND`, `INVALID_ORDER_STATUS`, and `ORDER_ACCESS_DENIED` result codes with clear English messages.

Use this transition map:

```java
private static final Map<OrderStatus, Set<OrderStatus>> ALLOWED_TRANSITIONS = Map.of(
    OrderStatus.PLACED, Set.of(OrderStatus.PROCESSING, OrderStatus.CANCELLED),
    OrderStatus.PROCESSING, Set.of(OrderStatus.SHIPPED, OrderStatus.CANCELLED),
    OrderStatus.SHIPPED, Set.of(),
    OrderStatus.CANCELLED, Set.of()
);
```

On the first valid transition to `CANCELLED`, restore the stored quantity to the referenced product before updating the status.

- [ ] **Step 6: Add the validated controller**

```java
@PostMapping("/add")
@RequireRoles(RoleEnum.USER)
public Result add(@Valid @RequestBody PlaceOrderRequest request) {
    return Result.success(orderService.placeOrder(request));
}

@GetMapping("/selectPage")
@RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS, RoleEnum.USER})
public Result selectPage(@RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
                         @RequestParam(defaultValue = "10") @Min(1) @Max(100) Integer pageSize) {
    return Result.success(orderService.selectPage(pageNum, pageSize));
}

@PutMapping("/status")
@RequireRoles({RoleEnum.ADMIN, RoleEnum.BUSINESS})
public Result updateStatus(@Valid @RequestBody OrderStatusRequest request) {
    orderService.updateStatus(request);
    return Result.success();
}
```

- [ ] **Step 7: Run the order and full backend tests**

Run: `cd springboot; .\mvnw.cmd -Dtest=OrderServiceTest test`

Expected: PASS.

Run: `cd springboot; .\mvnw.cmd test`

Expected: PASS.

- [ ] **Step 8: Commit the order backend slice**

```powershell
git add springboot/src
git commit -m "feat: add transactional customer orders"
```

### Task 4: Connect Purchasing and Customer Order History

**Files:**
- Create: `vue/src/constants/orderStatus.js`
- Create: `vue/src/constants/orderStatus.test.js`
- Create: `vue/src/views/front/Orders.vue`
- Create: `vue/src/views/front/Orders.test.js`
- Modify: `vue/src/views/front/ProductDetail.vue`
- Modify: `vue/src/views/front/ProductDetail.test.js`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/router/index.test.js`
- Modify: `vue/src/views/Front.vue`
- Modify: `vue/src/assets/css/front.css`

**Interfaces:**
- Consumes: Task 3 order endpoints.
- Produces: protected route `/front/orders` named `CustomerOrders`.
- Produces: `orderStatusLabel(code)` and status-tone helpers shared with Task 6.

- [ ] **Step 1: Write failing order-label, purchase, and history tests**

Verify exact labels `Placed`, `Processing`, `Shipped`, and `Cancelled`. In `ProductDetail.test.js`, bind a `USER` token, click `Buy now`, and assert:

```js
expect(request.post).toHaveBeenCalledWith('/orders/add', { goodsId: 7, quantity: 2 })
expect(message.success).toHaveBeenCalledWith(expect.stringContaining('NB-'))
```

In `Orders.test.js`, mock `/orders/selectPage` and verify order number, snapshot name, formatted total, quantity, status, and date are rendered.

- [ ] **Step 2: Run the focused frontend tests and confirm failures**

Run: `cd vue; npm test -- src/constants/orderStatus.test.js src/views/front/ProductDetail.test.js src/views/front/Orders.test.js`

Expected: FAIL because order helpers and history view are missing and `buyNow` is not connected.

- [ ] **Step 3: Connect `Buy now` to the API**

Prevent double submission with `purchasing`, post the server request, show `Order <number> placed successfully`, reload product stock, and offer navigation to `/front/orders`. Do not calculate or submit a total from the browser.

- [ ] **Step 4: Implement responsive customer order history**

Use a simple list/card layout rather than a dense desktop-only table. Load ten records per page from `/orders/selectPage`, render the snapshot image with fallback handling, and include empty, loading, and pagination states.

- [ ] **Step 5: Add protected navigation**

Register the protected customer route and show `My orders` in the front header only when `user.role === 'USER'`. Preserve the login redirect query so a customer returns to the original product.

- [ ] **Step 6: Run the customer-flow tests**

Run: `cd vue; npm test -- src/constants/orderStatus.test.js src/views/front/ProductDetail.test.js src/views/front/Orders.test.js src/router/index.test.js`

Expected: PASS.

- [ ] **Step 7: Commit the customer order slice**

```powershell
git add vue/src
git commit -m "feat: connect storefront purchasing and order history"
```

### Task 5: Add Role-Scoped Dashboard Metrics

**Files:**
- Create: `springboot/src/main/java/com/example/controller/response/DashboardSummary.java`
- Create: `springboot/src/main/java/com/example/service/DashboardService.java`
- Create: `springboot/src/main/java/com/example/controller/DashboardController.java`
- Create: `springboot/src/test/java/com/example/service/DashboardServiceTest.java`
- Modify: `springboot/src/main/java/com/example/mapper/GoodsMapper.java`
- Modify: `springboot/src/main/java/com/example/mapper/OrderMapper.java`
- Modify: `springboot/src/main/java/com/example/mapper/BusinessMapper.java`
- Modify: `springboot/src/main/java/com/example/mapper/UserMapper.java`
- Modify: `springboot/src/main/java/com/example/mapper/TypeMapper.java`

**Interfaces:**
- Produces: `DashboardSummary summary()` and authenticated `GET /dashboard/summary`.
- Produces JSON fields `products`, `categories`, `orders`, `customers`, `sellers`, `lowStockProducts`, and `revenue`.

- [ ] **Step 1: Write failing administrator and seller summary tests**

For `ADMIN`, verify every global count is queried. For `BUSINESS` ID 7, verify only seller-scoped product, low-stock, order, and revenue queries receive ID 7, while customer and seller totals are zero.

```java
assertEquals(7, summary.products());
assertEquals(2, summary.lowStockProducts());
assertEquals(new BigDecimal("2498.00"), summary.revenue());
```

- [ ] **Step 2: Run the dashboard test and confirm missing-type failures**

Run: `cd springboot; .\mvnw.cmd -Dtest=DashboardServiceTest test`

Expected: compilation FAIL because the dashboard classes and mapper methods do not exist.

- [ ] **Step 3: Add simple aggregate mapper methods**

Use `@Select` methods with `count(*)`, `count(*) where count <= 5`, and `coalesce(sum(total_price), 0)` for non-cancelled orders. Supply separate global and `business_id` methods rather than building dynamic dashboard SQL.

- [ ] **Step 4: Implement role-aware summary and controller**

Create an immutable record:

```java
public record DashboardSummary(long products, long categories, long orders,
                               long customers, long sellers, long lowStockProducts,
                               BigDecimal revenue) {}
```

Require `ADMIN` or `BUSINESS` on `GET /dashboard/summary`; the service obtains the current account and selects global or seller-scoped mapper calls.

- [ ] **Step 5: Run dashboard and full backend tests**

Run: `cd springboot; .\mvnw.cmd -Dtest=DashboardServiceTest test`

Expected: PASS.

Run: `cd springboot; .\mvnw.cmd test`

Expected: PASS.

- [ ] **Step 6: Commit the metrics slice**

```powershell
git add springboot/src
git commit -m "feat: add role-scoped management metrics"
```

### Task 6: Redesign Administrator and Seller Management

**Files:**
- Create: `vue/src/views/manager/Orders.vue`
- Create: `vue/src/views/manager/Orders.test.js`
- Create: `vue/src/views/manager/Home.test.js`
- Modify: `vue/src/views/Manager.vue`
- Modify: `vue/src/views/manager/Home.vue`
- Modify: `vue/src/views/manager/Goods.vue`
- Modify: `vue/src/views/manager/Goods.test.js`
- Modify: `vue/src/views/manager/Type.vue`
- Modify: `vue/src/views/manager/Business.vue`
- Modify: `vue/src/assets/css/manager.css`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/router/index.test.js`

**Interfaces:**
- Consumes: `GET /dashboard/summary`, `GET /orders/selectPage`, and `PUT /orders/status`.
- Consumes: `orderStatusLabel` from Task 4.
- Produces: protected route `/orders` named `ManagerOrders`.

- [ ] **Step 1: Write failing role-navigation and dashboard tests**

Mount as `ADMIN` and verify navigation contains Dashboard, Products, Categories, Orders, Sellers, Customers, and Administrators. Mount as `BUSINESS` and verify Dashboard, Products, Orders, and Profile are visible while account administration is absent. Mock dashboard data and verify the correct role-specific metric labels.

- [ ] **Step 2: Write failing management-order tests**

Mock a `PLACED` order and assert administrator/seller views render product, customer, quantity, total, order number, and status. Trigger `Processing` and assert:

```js
expect(request.put).toHaveBeenCalledWith('/orders/status', {
  id: 41,
  status: 'PROCESSING',
})
```

Verify no action buttons appear for terminal `SHIPPED` and `CANCELLED` rows.

- [ ] **Step 3: Run focused management tests and confirm failures**

Run: `cd vue; npm test -- src/views/manager/Home.test.js src/views/manager/Orders.test.js src/router/index.test.js`

Expected: FAIL because the new dashboard and orders management UI are missing.

- [ ] **Step 4: Refresh the shared manager shell**

Use a 248-pixel desktop sidebar, a compact mobile header, navy/white surfaces, orange action accents, a visible `Administrator` or `Seller` role badge, and role-specific navigation. Keep Element Plus menus and the existing account/profile behavior.

- [ ] **Step 5: Replace the welcome panel with metric cards**

Load `/dashboard/summary`; show four cards per role, a low-stock callout for sellers, an approval/status callout where relevant, and quick links implemented with `$router.push`. Use `formatSek` for revenue.

- [ ] **Step 6: Implement the role-scoped order table**

Add search-free paginated order management with image, order number, customer, product, total, quantity, date, status tag, and valid next-action buttons. Reload the current page after a successful status update.

- [ ] **Step 7: Refresh catalogue and seller-management pages**

Apply a common `management-page` structure with page title, explanatory subtitle, grouped toolbar, bordered table card, clear empty state, and responsive dialogs. Remove the `Unit` table column and form field from `Goods.vue`. Preserve ownership rules and existing CRUD API calls.

- [ ] **Step 8: Run focused and full frontend tests**

Run: `cd vue; npm test -- src/views/manager/Home.test.js src/views/manager/Orders.test.js src/views/manager/Goods.test.js src/router/index.test.js`

Expected: PASS.

Run: `cd vue; npm test`

Expected: PASS.

- [ ] **Step 9: Commit the management slice**

```powershell
git add vue/src
git commit -m "feat: redesign administrator and seller management"
```

### Task 7: Documentation and Complete Verification

**Files:**
- Modify: `README.md`
- Modify only if verification reveals a defect: implementation or test files from Tasks 1–6.

**Interfaces:**
- Consumes: all completed backend and frontend slices.
- Produces: reproducible run and demo instructions.

- [ ] **Step 1: Document the demo flow**

Explain that Flyway replaces existing catalogue rows when V6 first runs, preserves accounts, creates `customer_order` in V7, and does not process real payments. Add exact administrator/seller/customer walkthroughs and keep the existing two-terminal commands.

- [ ] **Step 2: Run backend verification on Java 25**

```powershell
cd springboot
java -version
.\mvnw.cmd clean test
```

Expected: Java 25 and `BUILD SUCCESS` with zero failing tests.

- [ ] **Step 3: Run frontend verification on Node 24**

```powershell
cd ..\vue
node -v
npm test
npm run lint
npm run build
```

Expected: Node 24, all Vitest files passing, ESLint exit code 0, and Vite production build exit code 0.

- [ ] **Step 4: Run MySQL-backed API smoke checks**

Start the backend with the configured `DB_USERNAME` and `DB_PASSWORD`. Verify Flyway applies V6 and V7, then confirm:

```text
GET  /type/selectAll                 -> 6 categories
GET  /goods/featured                 -> up to 10 illustrated products
GET  /goods/selectById?id=<seed id>  -> one detail record
POST /orders/add                     -> USER token creates an NB- order
GET  /orders/selectPage              -> USER sees only that order
PUT  /orders/status                  -> ADMIN/owner seller can advance it
GET  /dashboard/summary              -> role-scoped totals
```

Compare product stock before and after ordering, then cancel a fresh order and verify stock is restored once.

- [ ] **Step 5: Run responsive visual checks**

Start Vite, inspect desktop and narrow widths for `/front/home`, one category, search results, one product detail, `/front/orders`, `/home`, `/goods`, `/orders`, and `/business`. Confirm all images load, no card displays a unit suffix, actions are keyboard reachable, and horizontal overflow is absent.

- [ ] **Step 6: Review the final diff**

Run:

```powershell
git diff --check
git status --short
git diff HEAD~6 --stat
```

Inspect for secrets, accidental generated build output, stale Chinese copy, and unrelated changes.

- [ ] **Step 7: Commit documentation or verification fixes**

```powershell
git add README.md springboot vue
git commit -m "docs: document catalogue and order demo"
```

- [ ] **Step 8: Record final evidence**

Capture the exact backend test count, frontend test count, lint/build exit codes, Flyway versions applied, and the routes manually checked. Do not claim completion unless every required command has fresh successful output.
