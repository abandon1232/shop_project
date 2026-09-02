# Catalogue, Ordering, and Management Refresh

**Date:** 2026-09-02  
**Status:** Approved in chat

## Goal

Turn the current English storefront into a convincing but deliberately straightforward graduate portfolio project. The catalogue should feel populated, every product should have an image and a useful detail page, customers should be able to place an order, and administrators and sellers should have clear role-specific management screens.

The implementation will stay within the existing Vue, Spring Boot, MyBatis, Flyway, and MySQL architecture. It will not add a payment provider, microservices, a message broker, or a complex client-side state library.

## Catalogue Data and Images

- Replace all existing `goods` and `type` rows through a new Flyway migration. This is intentional catalogue cleanup; administrator, seller, and customer accounts remain untouched.
- Add six English categories: Computers & Tablets, Phones & Wearables, TV & Audio, Gaming, Kitchen Appliances, and Smart Home.
- Add three products per category, for a total of eighteen products. Each product has an English name, retail-style description, Swedish-krona price, stock quantity, category, and image.
- Add one original image for each product and one original category image for each category. Images use a consistent Scandinavian catalogue style, contain no logos, text, or watermarks, and are stored under `vue/public/images/catalog`.
- Seeded products are assigned to the first approved seller when one exists. If no approved seller exists, they are treated as platform catalogue products and remain visible and manageable by an administrator.
- Public product queries allow both platform products and products owned by approved sellers.

## Price Presentation

- Customer-facing pages display only a formatted price such as `1 299 kr`.
- Unit suffixes such as `/ p`, `/ each`, or `/ unit` are removed from the home, category, search, detail, and management displays.
- The existing database `unit` column remains for compatibility, but the refreshed UI neither requests nor renders it and the seeded data leaves it empty.

## Storefront Flow

- Introduce a reusable product card used by home, category, and search results.
- Every product card routes to `/front/product/:id`.
- The product detail page is public and shows product image, category, seller or `NorrByte Market`, description, stock, price, and quantity selection.
- `Buy now` requires a signed-in customer account. Guests are redirected to sign-in with the product page as the return destination.
- A successful purchase creates a persisted order and immediately shows a confirmation containing the order number.
- Signed-in customers receive a `My orders` page showing order number, product snapshot, quantity, total, status, and creation time.
- No real payment details are collected. The action represents placing an order in a portfolio/demo marketplace.

## Order Model and Rules

Add a `customer_order` table with:

- order number;
- product, customer, and seller references where available;
- product name and image snapshots so history remains readable after product edits;
- quantity, unit price, and total price snapshots;
- status, created timestamp, and updated timestamp.

Supported statuses are `PLACED`, `PROCESSING`, `SHIPPED`, and `CANCELLED`.

Order creation is transactional:

1. Read the authenticated account and require the `USER` role.
2. Validate a positive quantity and an available, publicly purchasable product.
3. Decrease stock with a conditional database update so stock cannot become negative.
4. Calculate the total on the server and insert the order.

Administrators may view every order. Sellers may view and update only orders for their products. Customers may view only their own orders. Administrators and the owning seller may move an order from `PLACED` to `PROCESSING` or `CANCELLED`, and from `PROCESSING` to `SHIPPED` or `CANCELLED`. Cancelling a non-terminal order restores stock exactly once.

## Management Experience

The existing manager shell remains one Vue layout but adapts to the authenticated role.

- Refresh the header, sidebar, page spacing, colours, responsive behaviour, role badge, and account menu.
- Replace the plain welcome panel with a dashboard containing role-appropriate summary cards and direct links.
- Administrators see catalogue totals, customer and seller totals, orders, seller review, categories, and all products.
- Sellers see their own product count, low-stock count, order count, revenue from non-cancelled orders, product management, and order fulfilment.
- Refresh product and category pages with consistent page headers, toolbars, image previews, stock indicators, empty states, and dialogs.
- Add an Orders management page. The backend enforces role scoping even if a user manually changes a URL.
- Keep the current CRUD approach and Element Plus components so the code remains understandable at new-graduate level.

## Backend API

Add these endpoints using the project’s existing response envelope:

- `POST /orders/add` — customer places an order.
- `GET /orders/selectPage` — role-scoped paginated order list.
- `PUT /orders/status` — administrator or owning seller changes status.
- `GET /dashboard/summary` — role-scoped dashboard counts.

The existing public `GET /goods/selectById` endpoint supplies the detail page. Existing product and category CRUD endpoints remain compatible.

## Error Handling

- Missing products return the existing not-found/domain error response.
- Invalid quantity, insufficient stock, invalid status transitions, and attempts to access another account’s order return explicit English messages through the existing global error handler.
- The frontend disables purchasing when stock is zero and shows API failures through Element Plus messages.
- Broken or missing images use the current product fallback image.

## Testing and Acceptance Criteria

Backend tests cover:

- order total calculation and snapshot creation;
- stock decrement and insufficient-stock rejection;
- role-scoped order access;
- seller ownership checks;
- valid and invalid status transitions;
- stock restoration after cancellation.

Frontend tests cover:

- new detail and order routes;
- reusable product-card navigation;
- price rendering without unit suffixes;
- guest sign-in redirect and customer purchase request;
- role-specific manager navigation and order controls.

Completion requires:

- all Maven tests passing on Java 25;
- all Vitest tests and ESLint checks passing on Node 24;
- a successful production frontend build;
- manual API verification against MySQL for catalogue loading, detail retrieval, order placement, stock change, customer history, administrator visibility, and seller scoping;
- a responsive visual check of the storefront and both management roles.
