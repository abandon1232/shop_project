# Cart Checkout and Storefront UI Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a persistent customer cart and atomic checkout while fixing the category heading, password labels, seller display, and product-detail purchase copy.

**Architecture:** A focused Spring cart module owns authenticated cart CRUD and coordinates checkout through the existing transactional order service. The Vue storefront adds a protected cart route and keeps its header quantity in sync through child-to-layout events. Existing `customer_order` rows remain the fulfilment unit, so each cart line produces one order inside one outer transaction.

**Tech Stack:** Java 25, Spring Boot 4.1.1, MyBatis, MySQL 8, Flyway, JUnit 5, Mockito, Vue 3 Options API, Vue Router 5, Element Plus, Axios, Vitest, Vue Test Utils, and ESLint.

**Spec:** `docs/superpowers/specs/2026-09-02-cart-checkout-ui-polish-design.md`

## Global Constraints

- Work only on `feat/cart-checkout-ui-polish`; do not commit directly to `master`.
- Use the existing database-backed `cart_item` table from migration V4.
- Do not edit any existing Flyway migration.
- Keep `customer_order` as the active order model and create one row per cart line.
- Checkout must be atomic: every line succeeds and the cart clears, or every database change rolls back.
- Only authenticated `USER` accounts may read or mutate a cart.
- Derive user identity from `TokenUtils.getCurrentUser()`; never accept a user ID from the client.
- Read product price, stock, seller, and availability from the database.
- Quantities are integers from 1 through 99 and may not exceed current stock.
- Keep the project at an understandable graduate-portfolio level; add no payment, shipping, coupon, tax, aggregate-order, queue, or distributed-system feature.
- Keep all user-facing source text and comments in English.

---

### Task 1: Cart domain, mapper, and authenticated CRUD service

**Files:**
- Create: `springboot/src/main/java/com/example/entity/CartItem.java`
- Create: `springboot/src/main/java/com/example/controller/request/AddCartItemRequest.java`
- Create: `springboot/src/main/java/com/example/controller/request/UpdateCartItemRequest.java`
- Create: `springboot/src/main/java/com/example/mapper/CartMapper.java`
- Create: `springboot/src/main/resources/mapper/CartMapper.xml`
- Create: `springboot/src/main/java/com/example/service/CartService.java`
- Modify: `springboot/src/main/java/com/example/common/enums/ResultCodeEnum.java`
- Test: `springboot/src/test/java/com/example/service/CartServiceTest.java`

**Interfaces:**
- Consumes: `GoodsService.selectPurchasableById(Integer)`, `TokenUtils.getCurrentUser()`, and the existing V4 `cart_item` schema.
- Produces: `CartService.selectCurrentCart()`, `add(AddCartItemRequest)`, `update(Integer, UpdateCartItemRequest)`, and `delete(Integer)` for the controller and later checkout task.
- Produces: `CartMapper.selectByUserId(Integer)`, `selectByUserAndGoods(Integer, Integer)`, `selectOwnedById(Integer, Integer)`, `insert(CartItem)`, `updateQuantity(Integer, Integer, Integer)`, `deleteOwned(Integer, Integer)`, and `deleteByUserId(Integer)`.

- [ ] **Step 1: Write failing cart service tests**

Create `CartServiceTest` with Mockito mocks for `CartMapper`, `GoodsService`, and `OrderService`. Bind an authenticated account through `MockHttpServletRequest`, matching `OrderServiceTest`.

```java
@Test
void addingExistingProductIncrementsOwnedLine() {
    bindAccount(3, RoleEnum.USER);
    Goods goods = goods(7, 8, "QuietWave Headphones", "1990.00");
    CartItem existing = cartItem(14, 3, 7, 2);
    when(goodsService.selectPurchasableById(7)).thenReturn(goods);
    when(cartMapper.selectByUserAndGoods(3, 7)).thenReturn(existing);
    when(cartMapper.updateQuantity(14, 3, 5)).thenReturn(1);
    when(cartMapper.selectOwnedById(14, 3)).thenReturn(cartItem(14, 3, 7, 5));

    CartItem result = service.add(new AddCartItemRequest(7, 3));

    assertEquals(5, result.getQuantity());
    verify(cartMapper).updateQuantity(14, 3, 5);
    verify(cartMapper, never()).insert(any());
}

@Test
void customerCannotUpdateAnotherCustomersLine() {
    bindAccount(3, RoleEnum.USER);
    when(cartMapper.selectOwnedById(99, 3)).thenReturn(null);

    CustomException error = assertThrows(CustomException.class,
            () -> service.update(99, new UpdateCartItemRequest(2)));

    assertEquals("4043", error.getCode());
    verify(cartMapper, never()).updateQuantity(any(), any(), any());
}

@Test
void addingMoreThanCurrentStockIsRejected() {
    bindAccount(3, RoleEnum.USER);
    when(goodsService.selectPurchasableById(7))
            .thenReturn(goods(7, 2, "QuietWave Headphones", "1990.00"));

    CustomException error = assertThrows(CustomException.class,
            () -> service.add(new AddCartItemRequest(7, 3)));

    assertEquals("4091", error.getCode());
    verifyNoInteractions(cartMapper);
}

@Test
void sellerCannotReadCustomerCart() {
    bindAccount(7, RoleEnum.BUSINESS);

    CustomException error = assertThrows(CustomException.class, service::selectCurrentCart);

    assertEquals("4032", error.getCode());
    verifyNoInteractions(cartMapper);
}
```

Use these concrete test helpers so stock and ownership are explicit:

```java
private Goods goods(int id, int stock, String name, String price) {
    Goods goods = new Goods();
    goods.setId(id);
    goods.setCount(stock);
    goods.setName(name);
    goods.setPrice(new BigDecimal(price));
    return goods;
}

private CartItem cartItem(int id, int userId, int goodsId, int quantity) {
    CartItem item = new CartItem();
    item.setId(id);
    item.setUserId(userId);
    item.setGoodsId(goodsId);
    item.setQuantity(quantity);
    return item;
}

private CartItem detailedCartItem(
        int id, int userId, int goodsId, int quantity, int stock) {
    CartItem item = cartItem(id, userId, goodsId, quantity);
    item.setStock(stock);
    return item;
}

private void bindAccount(int id, RoleEnum role) {
    Account account = new Account();
    account.setId(id);
    account.setRole(role.name());
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setAttribute(Constants.CURRENT_USER, account);
    RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
}
```

Reset `RequestContextHolder` in `@AfterEach`, exactly as `OrderServiceTest` does.

- [ ] **Step 2: Run the new tests and verify they fail**

Run:

```powershell
cd E:\shop_project\springboot
.\mvnw.cmd -Dtest=CartServiceTest test
```

Expected: compilation fails because the cart classes do not exist.

- [ ] **Step 3: Add request records, result codes, and the cart response entity**

Define the request contracts exactly as follows:

```java
public record AddCartItemRequest(
        @NotNull @Positive Integer goodsId,
        @NotNull @Min(1) @Max(99) Integer quantity) {
}

public record UpdateCartItemRequest(
        @NotNull @Min(1) @Max(99) Integer quantity) {
}
```

Add these enum values without changing existing codes:

```java
CART_ITEM_NOT_FOUND("4043", "Cart item not found"),
CART_ACCESS_DENIED("4032", "You cannot access this cart"),
CART_EMPTY("4093", "Your cart is empty"),
```

`CartItem` contains `id`, `userId`, `goodsId`, `quantity`, `productName`, `productImg`, `unitPrice`, `stock`, `businessName`, `businessStatus`, `createdAt`, and `updatedAt`, with ordinary getters and setters. Use `BigDecimal` for `unitPrice` and `LocalDateTime` for timestamps.

- [ ] **Step 4: Add mapper interfaces and owned-row SQL**

Define `CartMapper`:

```java
public interface CartMapper {
    List<CartItem> selectByUserId(Integer userId);
    CartItem selectByUserAndGoods(@Param("userId") Integer userId,
                                  @Param("goodsId") Integer goodsId);
    CartItem selectOwnedById(@Param("id") Integer id,
                             @Param("userId") Integer userId);
    int insert(CartItem item);
    int updateQuantity(@Param("id") Integer id,
                       @Param("userId") Integer userId,
                       @Param("quantity") Integer quantity);
    int deleteOwned(@Param("id") Integer id,
                    @Param("userId") Integer userId);
    int deleteByUserId(Integer userId);
}
```

Use one reusable result mapping in `CartMapper.xml`. Every select joins `goods` and `business` and returns current catalogue data:

```xml
<sql id="Cart_Select">
    select cart_item.id,
           cart_item.user_id as userId,
           cart_item.goods_id as goodsId,
           cart_item.quantity,
           cart_item.created_at as createdAt,
           cart_item.updated_at as updatedAt,
           goods.name as productName,
           goods.img as productImg,
           goods.price as unitPrice,
           goods.count as stock,
           business.name as businessName,
           business.status as businessStatus
    from cart_item
    join goods on cart_item.goods_id = goods.id
    left join business on goods.business_id = business.id
</sql>
```

Every ID-based update and delete includes `user_id = #{userId}`. Order `selectByUserId` by `updated_at desc, id desc`.

- [ ] **Step 5: Implement minimal authenticated CRUD behavior**

Implement these public signatures in `CartService`:

```java
public List<CartItem> selectCurrentCart()
public CartItem add(AddCartItemRequest request)
public CartItem update(Integer id, UpdateCartItemRequest request)
public void delete(Integer id)
```

Use a private method that rejects non-customers:

```java
private Account currentCustomer() {
    Account account = TokenUtils.getCurrentUser();
    if (!RoleEnum.USER.name().equals(account.getRole())) {
        throw new CustomException(ResultCodeEnum.CART_ACCESS_DENIED);
    }
    return account;
}
```

For add, load the product through `selectPurchasableById`, compute the combined quantity for an existing line, and reject totals above 99 or above `goods.getCount()`. For update, first load the owned row, load the current product, and validate the replacement quantity. Treat a mapper update/delete result other than one as `CART_ITEM_NOT_FOUND`.

- [ ] **Step 6: Run the focused backend tests**

Run:

```powershell
.\mvnw.cmd -Dtest=CartServiceTest test
```

Expected: all CRUD, stock, role, and ownership tests pass.

- [ ] **Step 7: Commit the cart persistence layer**

```powershell
git add springboot/src/main/java/com/example/entity/CartItem.java springboot/src/main/java/com/example/controller/request/AddCartItemRequest.java springboot/src/main/java/com/example/controller/request/UpdateCartItemRequest.java springboot/src/main/java/com/example/mapper/CartMapper.java springboot/src/main/resources/mapper/CartMapper.xml springboot/src/main/java/com/example/service/CartService.java springboot/src/main/java/com/example/common/enums/ResultCodeEnum.java springboot/src/test/java/com/example/service/CartServiceTest.java
git commit -m "feat: add persistent customer cart"
```

---

### Task 2: Cart HTTP API and atomic checkout

**Files:**
- Create: `springboot/src/main/java/com/example/controller/CartController.java`
- Create: `springboot/src/test/java/com/example/controller/CartValidationTest.java`
- Modify: `springboot/src/main/java/com/example/service/CartService.java`
- Modify: `springboot/src/test/java/com/example/service/CartServiceTest.java`

**Interfaces:**
- Consumes: Task 1 cart requests, mapper, service CRUD methods, and `OrderService.placeOrder(PlaceOrderRequest)`.
- Produces: `GET /cart/items`, `POST /cart/items`, `PUT /cart/items/{id}`, `DELETE /cart/items/{id}`, and `POST /cart/checkout`.
- Produces: `CartService.checkout()` returning `List<CustomerOrder>`.

- [ ] **Step 1: Add failing checkout service tests**

Append tests that establish atomic orchestration and clearing behavior:

```java
@Test
void checkoutCreatesOneOrderPerLineThenClearsCart() {
    bindAccount(3, RoleEnum.USER);
    CartItem first = detailedCartItem(14, 3, 7, 2, 6);
    CartItem second = detailedCartItem(15, 3, 8, 1, 4);
    when(cartMapper.selectByUserId(3)).thenReturn(List.of(first, second));
    when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 6, "Headphones", "1990.00"));
    when(goodsService.selectPurchasableById(8)).thenReturn(goods(8, 4, "Keyboard", "990.00"));
    CustomerOrder firstOrder = new CustomerOrder();
    CustomerOrder secondOrder = new CustomerOrder();
    when(orderService.placeOrder(new PlaceOrderRequest(7, 2))).thenReturn(firstOrder);
    when(orderService.placeOrder(new PlaceOrderRequest(8, 1))).thenReturn(secondOrder);

    List<CustomerOrder> result = service.checkout();

    assertEquals(List.of(firstOrder, secondOrder), result);
    InOrder sequence = inOrder(orderService, cartMapper);
    sequence.verify(orderService).placeOrder(new PlaceOrderRequest(7, 2));
    sequence.verify(orderService).placeOrder(new PlaceOrderRequest(8, 1));
    sequence.verify(cartMapper).deleteByUserId(3);
}

@Test
void failedCheckoutDoesNotClearCart() {
    bindAccount(3, RoleEnum.USER);
    CartItem line = detailedCartItem(14, 3, 7, 3, 2);
    when(cartMapper.selectByUserId(3)).thenReturn(List.of(line));
    when(goodsService.selectPurchasableById(7)).thenReturn(goods(7, 2, "Headphones", "1990.00"));

    CustomException error = assertThrows(CustomException.class, service::checkout);

    assertEquals("4091", error.getCode());
    verifyNoInteractions(orderService);
    verify(cartMapper, never()).deleteByUserId(any());
}

@Test
void emptyCartCannotCheckout() {
    bindAccount(3, RoleEnum.USER);
    when(cartMapper.selectByUserId(3)).thenReturn(List.of());

    CustomException error = assertThrows(CustomException.class, service::checkout);

    assertEquals("4093", error.getCode());
}
```

- [ ] **Step 2: Run checkout tests and verify failure**

```powershell
cd E:\shop_project\springboot
.\mvnw.cmd -Dtest=CartServiceTest test
```

Expected: fails because `checkout()` is absent.

- [ ] **Step 3: Implement transactional checkout**

Inject `OrderService` into `CartService` and implement:

```java
@Transactional
public List<CustomerOrder> checkout() {
    Account account = currentCustomer();
    List<CartItem> items = cartMapper.selectByUserId(account.getId());
    if (items.isEmpty()) {
        throw new CustomException(ResultCodeEnum.CART_EMPTY);
    }

    for (CartItem item : items) {
        Goods goods = goodsService.selectPurchasableById(item.getGoodsId());
        if (item.getQuantity() > goods.getCount()) {
            throw new CustomException(ResultCodeEnum.INSUFFICIENT_STOCK);
        }
    }

    List<CustomerOrder> orders = new ArrayList<>();
    for (CartItem item : items) {
        orders.add(orderService.placeOrder(
                new PlaceOrderRequest(item.getGoodsId(), item.getQuantity())));
    }
    cartMapper.deleteByUserId(account.getId());
    return orders;
}
```

The outer `@Transactional` method and the existing default transaction propagation on `OrderService.placeOrder` form one transaction. Do not catch `CustomException`; propagation is required for rollback.

- [ ] **Step 4: Write failing controller validation tests**

Create standalone MockMvc tests:

```java
@Test
void addRejectsQuantityAboveNinetyNine() throws Exception {
    mockMvc.perform(post("/cart/items")
                    .contentType(APPLICATION_JSON)
                    .content("{\"goodsId\":7,\"quantity\":100}"))
            .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));
    verifyNoInteractions(cartService);
}

@Test
void updateRejectsZeroQuantity() throws Exception {
    mockMvc.perform(put("/cart/items/14")
                    .contentType(APPLICATION_JSON)
                    .content("{\"quantity\":0}"))
            .andExpect(jsonPath("$.code").value(ResultCodeEnum.PARAM_ERROR.code));
    verifyNoInteractions(cartService);
}
```

Expected failure: `CartController` is absent.

- [ ] **Step 5: Implement the cart controller**

Create `CartController` with class-level `@RequestMapping("/cart")`, `@Validated`, and `@RequireRoles(RoleEnum.USER)`. Implement:

```java
@GetMapping("/items")
public Result selectItems()

@PostMapping("/items")
public Result add(@Valid @RequestBody AddCartItemRequest request)

@PutMapping("/items/{id}")
public Result update(@PathVariable @Positive Integer id,
                     @Valid @RequestBody UpdateCartItemRequest request)

@DeleteMapping("/items/{id}")
public Result delete(@PathVariable @Positive Integer id)

@PostMapping("/checkout")
public Result checkout()
```

Return `Result.success(...)` for reads, adds, updates, and checkout; return empty success after delete.

- [ ] **Step 6: Run the complete cart backend tests**

```powershell
.\mvnw.cmd -Dtest=CartServiceTest,CartValidationTest test
```

Expected: all tests pass.

- [ ] **Step 7: Run all backend tests before committing**

```powershell
.\mvnw.cmd test
```

Expected: the existing order, goods, authorization, upload, migration, dashboard, and token tests remain green.

- [ ] **Step 8: Commit the API and checkout**

```powershell
git add springboot/src/main/java/com/example/controller/CartController.java springboot/src/main/java/com/example/service/CartService.java springboot/src/test/java/com/example/controller/CartValidationTest.java springboot/src/test/java/com/example/service/CartServiceTest.java
git commit -m "feat: checkout customer carts atomically"
```

---

### Task 3: Product-detail cart action, direct seller name, and login return path

**Files:**
- Modify: `vue/src/views/front/ProductDetail.vue`
- Modify: `vue/src/views/front/ProductDetail.test.js`
- Modify: `vue/src/views/Login.vue`
- Create: `vue/src/views/Login.test.js`

**Interfaces:**
- Consumes: `POST /cart/items` with `{ goodsId, quantity }`.
- Produces: a `cart-updated` component event after a successful add.
- Produces: a customer-only internal return path under `/front/` after sign-in.

- [ ] **Step 1: Change product detail tests to the new behavior**

Replace order-placement expectations with cart expectations:

```javascript
expect(wrapper.text()).toContain('Add to cart')
expect(wrapper.text()).not.toContain('Buy now')
expect(wrapper.text()).not.toContain('Demo checkout')

product.businessName = 'Nordic Sound AB'
const { wrapper } = mountDetail()
await flushPromises()
expect(wrapper.text()).toContain('Nordic Sound AB')
expect(wrapper.text()).not.toContain('Approved marketplace seller')

await wrapper.vm.addToCart()
expect(request.post).toHaveBeenCalledWith('/cart/items', { goodsId: 7, quantity: 2 })
expect(message.success).toHaveBeenCalledWith('Added to cart')
expect(wrapper.emitted('cart-updated')).toBeTruthy()
```

Keep the guest redirect assertion, but call `addToCart()`.

- [ ] **Step 2: Add failing login redirect tests**

Create `Login.test.js` and assert both safe return and unsafe fallback:

```javascript
it('returns a customer to a requested storefront page', async () => {
  const { router, wrapper } = mountLogin('/front/product/7')
  await wrapper.vm.login()
  await flushPromises()
  expect(router.push).toHaveBeenCalledWith('/front/product/7')
})

it('does not redirect a customer outside the storefront', async () => {
  const { router, wrapper } = mountLogin('https://example.com')
  await wrapper.vm.login()
  await flushPromises()
  expect(router.push).toHaveBeenCalledWith('/front/home')
})
```

The helper supplies a successful `USER` login response and `$route.query.redirect`.

- [ ] **Step 3: Run focused frontend tests and verify failure**

```powershell
cd E:\shop_project\vue
npm test -- --run src/views/front/ProductDetail.test.js src/views/Login.test.js
```

Expected: product assertions fail on `Buy now` and login always navigates home.

- [ ] **Step 4: Implement product add-to-cart behavior**

In `ProductDetail.vue`:

- Render `product.businessName || 'NorrByte Market'` without a numeric-name regular expression.
- Rename state `purchasing` to `addingToCart`.
- Rename method `buyNow` to `addToCart`.
- Post to `/cart/items`.
- On code `200`, show `Added to cart` and emit `cart-updated`.
- Remove the purchase-note paragraph and its CSS.
- Keep the current login and customer-role checks.

- [ ] **Step 5: Implement a validated customer return path**

In `Login.vue`, after a successful customer login:

```javascript
const requested = this.$route.query.redirect
const destination = typeof requested === 'string' && requested.startsWith('/front/')
  ? requested
  : '/front/home'
this.$router.push(destination)
```

Administrator and seller accounts continue to navigate to `/home`.

- [ ] **Step 6: Run focused frontend tests**

```powershell
npm test -- --run src/views/front/ProductDetail.test.js src/views/Login.test.js
```

Expected: all product-detail and login tests pass.

- [ ] **Step 7: Commit product and login behavior**

```powershell
git add vue/src/views/front/ProductDetail.vue vue/src/views/front/ProductDetail.test.js vue/src/views/Login.vue vue/src/views/Login.test.js
git commit -m "feat: add products to persistent cart"
```

---

### Task 4: Customer cart page, protected route, and header quantity

**Files:**
- Create: `vue/src/views/front/Cart.vue`
- Create: `vue/src/views/front/Cart.test.js`
- Modify: `vue/src/views/Front.vue`
- Create: `vue/src/views/Front.test.js`
- Modify: `vue/src/assets/css/front.css`
- Modify: `vue/src/router/index.js`
- Modify: `vue/src/router/index.test.js`

**Interfaces:**
- Consumes: all Task 2 cart endpoints and Task 3 `cart-updated` event.
- Produces: protected route name `CustomerCart` at `/front/cart` with `roles: ['USER']`.
- Produces: `Cart.vue` events `cart-updated` after load, quantity update, removal, and checkout.

- [ ] **Step 1: Add failing route and header tests**

Extend `router/index.test.js`:

```javascript
it('protects the customer cart from guests and sellers', async () => {
  const cartRoute = router.getRoutes().find(route => route.name === 'CustomerCart')
  expect(cartRoute.path).toBe('/front/cart')
  expect(cartRoute.meta.roles).toEqual(['USER'])

  await router.push('/front/cart')
  expect(router.currentRoute.value.path).toBe('/login')

  localStorage.setItem('xm-user', JSON.stringify({ id: 7, role: 'BUSINESS', token: 'token' }))
  await router.push('/front/cart')
  expect(router.currentRoute.value.path).toBe('/403')
})
```

Create `Front.test.js` with a signed-in customer, mock `/notice/selectAll` and `/cart/items`, and assert:

```javascript
expect(request.get).toHaveBeenCalledWith('/cart/items')
expect(wrapper.get('.cart-action').text()).toContain('Cart')
expect(wrapper.get('.cart-count').text()).toBe('3')
await wrapper.get('.cart-action').trigger('click')
expect(router.push).toHaveBeenCalledWith('/front/cart')
```

Use two lines with quantities one and two so the badge proves it sums quantities rather than rows.

- [ ] **Step 2: Add failing cart page tests**

Create `Cart.test.js` with two cart lines. Assert:

```javascript
expect(wrapper.text()).toContain('Your cart')
expect(wrapper.text()).toContain('Nordic Sound AB')
expect(wrapper.text()).toMatch(/4[\s\u00a0]970,00\s*kr/)

await wrapper.vm.updateQuantity(items[0], 3)
expect(request.put).toHaveBeenCalledWith('/cart/items/14', { quantity: 3 })

await wrapper.vm.removeItem(items[1])
expect(request.delete).toHaveBeenCalledWith('/cart/items/15')

await wrapper.vm.checkout()
expect(request.post).toHaveBeenCalledWith('/cart/checkout')
expect(message.success).toHaveBeenCalledWith('Checkout complete: 2 orders placed')
expect(router.push).toHaveBeenCalledWith('/front/orders')
```

Also test that checkout is disabled when a line has `quantity > stock` or `businessStatus` is present and not `APPROVED`.

- [ ] **Step 3: Run route, layout, and cart tests and verify failure**

```powershell
cd E:\shop_project\vue
npm test -- --run src/router/index.test.js src/views/Front.test.js src/views/front/Cart.test.js
```

Expected: fails because the route, page, and header cart state do not exist.

- [ ] **Step 4: Add the protected cart route**

Add this child route beneath `/front`:

```javascript
{
  path: 'cart',
  name: 'CustomerCart',
  meta: { name: 'Cart', roles: ['USER'] },
  component: () => import('../views/front/Cart.vue'),
}
```

Change the role-rejection branch in the global guard so authenticated `USER` accounts return to `/front/home`, while authenticated administrators or sellers are sent to `/403` for a customer-only route.

- [ ] **Step 5: Implement header cart state**

In `Front.vue`:

- Add `cartCount: 0` to data.
- Add a customer-only `.cart-action` button before the account dropdown.
- Render `.cart-count` only when count is greater than zero.
- Add `loadCartCount()` that gets `/cart/items` and sums `Number(item.quantity || 0)`.
- Call it on mount for a signed-in customer.
- Listen with `<router-view @cart-updated="loadCartCount" ...>`.
- Reset count during logout.

Add responsive header styles in `front.css`; the cart action remains visible at mobile width while the text may hide below 560 px.

- [ ] **Step 6: Implement `Cart.vue`**

Use Options API state:

```javascript
data() {
  return {
    items: [],
    loading: true,
    checkingOut: false,
    updatingIds: [],
    productFallback,
  }
}
```

Computed behavior:

```javascript
total() {
  return this.items.reduce(
    (sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0),
    0,
  )
},
hasUnavailableItems() {
  return this.items.some(item =>
    item.quantity > item.stock
    || (item.businessStatus && item.businessStatus !== 'APPROVED'))
},
canCheckout() {
  return this.items.length > 0 && !this.hasUnavailableItems && !this.checkingOut
}
```

Methods call the exact endpoints from Task 2. Reload lines after quantity updates and removals. After each successful load or mutation, emit `cart-updated`. On checkout success, use `res.data.length` in the success message, emit, then navigate to `/front/orders`. Keep lines and display `res.msg` on failure.

The template includes an empty-cart state with a `Continue shopping` action, responsive line cards, stock messaging, a summary panel, and a `Checkout` button. Use `formatSek` and `applyImageFallback` from existing utilities.

- [ ] **Step 7: Run focused cart tests**

```powershell
npm test -- --run src/router/index.test.js src/views/Front.test.js src/views/front/Cart.test.js
```

Expected: all route, header, cart total, mutation, disabled-state, and checkout tests pass.

- [ ] **Step 8: Commit the storefront cart**

```powershell
git add vue/src/views/front/Cart.vue vue/src/views/front/Cart.test.js vue/src/views/Front.vue vue/src/views/Front.test.js vue/src/assets/css/front.css vue/src/router/index.js vue/src/router/index.test.js
git commit -m "feat: add customer cart checkout page"
```

---

### Task 5: Category and password label layout fixes

**Files:**
- Modify: `vue/src/views/front/Home.vue`
- Modify: `vue/src/views/front/Home.test.js`
- Modify: `vue/src/views/manager/Password.vue`
- Create: `vue/src/views/manager/Password.test.js`

**Interfaces:**
- Consumes: existing Element Plus form components and current responsive breakpoints.
- Produces: `.category-heading` and `.password-form` layout hooks.

- [ ] **Step 1: Add failing layout structure tests**

Extend `Home.test.js`:

```javascript
const heading = wrapper.get('.category-heading')
expect(heading.get('.eyebrow').text()).toBe('Browse')
expect(heading.get('.section-title').text()).toBe('Shop by category')
```

Create `Password.test.js` with named Element Plus stubs and assert:

```javascript
const form = wrapper.get('.password-form')
expect(form.attributes('label-width')).toBe('150px')
expect(wrapper.findAll('.password-form-item')).toHaveLength(3)
```

- [ ] **Step 2: Run layout tests and verify failure**

```powershell
cd E:\shop_project\vue
npm test -- --run src/views/front/Home.test.js src/views/manager/Password.test.js
```

Expected: fails because the dedicated classes and 150 px label width are absent.

- [ ] **Step 3: Fix the category heading**

Add `category-heading` to the current panel heading. Give it a vertical grid layout and set its `.section-title` to `white-space: nowrap`. Keep category item text wrapping unchanged for narrow names.

```css
.category-heading {
  display: grid;
  gap: 7px;
}

.category-heading .section-title {
  margin-top: 0;
  white-space: nowrap;
}
```

- [ ] **Step 4: Fix password labels responsively**

Add `class="password-form"`, change `label-width` to `150px`, and add `class="password-form-item"` to the three fields. Replace the inline card width with a CSS class that has a sensible desktop maximum.

```css
.password-card {
  width: min(760px, 100%);
}

@media (max-width: 640px) {
  .password-form {
    padding-right: 0 !important;
  }

  :deep(.password-form-item) {
    display: block;
  }

  :deep(.password-form-item .el-form-item__label) {
    width: auto !important;
    justify-content: flex-start;
  }
}
```

- [ ] **Step 5: Run layout tests and the existing home suite**

```powershell
npm test -- --run src/views/front/Home.test.js src/views/manager/Password.test.js
```

Expected: all tests pass.

- [ ] **Step 6: Commit the layout fixes**

```powershell
git add vue/src/views/front/Home.vue vue/src/views/front/Home.test.js vue/src/views/manager/Password.vue vue/src/views/manager/Password.test.js
git commit -m "fix: keep storefront labels readable"
```

---

### Task 6: Documentation and complete verification

**Files:**
- Modify: `README.md`
- Modify: `docs/cv-project-description.md`

**Interfaces:**
- Consumes: the completed cart API and storefront behavior.
- Produces: accurate run instructions and portfolio wording for the implemented feature.

- [ ] **Step 1: Update user-facing project documentation**

In `README.md`:

- Replace direct-purchase wording with persistent cart and atomic checkout wording.
- Add `Cart` to the customer demo flow.
- Keep the statement that no real payment is collected, but keep it in documentation rather than product-page UI.
- Add cart endpoints to any API overview if present.

In `docs/cv-project-description.md`, describe the feature without production-scale claims:

```text
Implemented a database-backed customer cart and transactional checkout that validates server-side prices and stock, creates one fulfilment order per product, and rolls back the complete basket when any line cannot be purchased.
```

- [ ] **Step 2: Run complete backend verification**

```powershell
cd E:\shop_project\springboot
.\mvnw.cmd clean verify
```

Expected: `BUILD SUCCESS` and zero failed tests.

- [ ] **Step 3: Run complete frontend verification**

```powershell
cd E:\shop_project\vue
npm test -- --run
npm run lint
npm run build
npm audit --omit=dev --audit-level=high
```

Expected: every Vitest file passes, ESLint exits zero, Vite builds production assets, and npm reports zero high-severity production vulnerabilities.

- [ ] **Step 4: Inspect the complete branch diff**

```powershell
cd E:\shop_project
git diff --check master...HEAD
git status --short --branch
git diff --stat master...HEAD
```

Expected: no whitespace errors, only intended source/tests/docs changes, and no generated `target`, `dist`, cache, credential, or dependency directory staged.

- [ ] **Step 5: Commit documentation**

```powershell
git add README.md docs/cv-project-description.md
git commit -m "docs: describe cart checkout workflow"
```

- [ ] **Step 6: Re-run branch status after the final commit**

```powershell
git status --short --branch
git log --oneline master..HEAD
```

Expected: a clean feature branch with the design, implementation, tests, layout fixes, and documentation commits ahead of `master`.
