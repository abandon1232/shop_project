# Cart Checkout and Storefront UI Polish Design

**Date:** 2026-09-02
**Branch:** `feat/cart-checkout-ui-polish`
**Status:** Approved for implementation planning

## Context

The storefront currently places an order directly from the product detail page. The database already contains an unused `cart_item` table, while the active ordering workflow persists one product per row in `customer_order`. The storefront also has two visible layout defects: the category heading wraps unnecessarily and the password form labels break across multiple lines. Product details replace some numeric seller names with a generic label and include demo-checkout copy that should no longer be shown.

## Goals

- Keep the category heading and desktop password labels on one line.
- Show the merchant store name returned by the backend under `Sold by`.
- Remove the generic `Approved marketplace seller` replacement and the demo-checkout note.
- Replace direct purchase with a persistent, authenticated shopping cart.
- Let customers review quantities, remove items, see totals, and check out from a cart page.
- Preserve the existing understandable one-product-per-order model.
- Keep authorization, prices, inventory checks, and checkout decisions on the server.

## Non-goals

- Payment processing or storage of payment details
- Shipping addresses, delivery pricing, coupons, tax calculation, refunds, or invoices
- Guest carts
- A new aggregate order and order-item domain model
- Cross-seller fulfilment aggregation
- Message queues, distributed transactions, or other production-scale infrastructure

## Chosen Approach

Use the existing database-backed `cart_item` table and the existing `customer_order` workflow. Each cart line becomes one `customer_order` row during checkout. The complete checkout runs in one database transaction: either every selected item becomes an order and the cart is cleared, or every change rolls back.

This approach is preferred over browser-only storage because the cart survives refreshes and later logins. It is preferred over activating the unused `shop_order` and `order_item` tables because that would duplicate the current order-management implementation and add complexity without improving the portfolio goal.

## Storefront Design

### Category panel

- Place the `BROWSE` eyebrow above the main heading rather than beside it.
- Keep `Shop by category` on one line at supported desktop widths.
- Allow category names themselves to wrap only where needed on narrow screens.

### Password form

- Increase the Element Plus form label width so `Current password`, `New password`, and `Confirm password` stay on one line on desktop.
- Use top-positioned labels on narrow screens so the inputs retain usable width.

### Product detail

- Render `product.businessName` directly when it is present.
- Use `NorrByte Market` only when the product has no merchant name, representing a platform-owned product.
- Remove the numeric-name replacement that currently displays `Approved marketplace seller`.
- Remove `Demo checkout · No payment details are collected`.
- Rename `Buy now` to `Add to cart` and call the cart API instead of the order API.
- Preserve the selected quantity and existing stock-based disable state.
- Redirect unauthenticated visitors to sign-in with the current product URL as the return destination.
- Update customer sign-in to honor that validated internal return destination so the customer comes back to the product after authentication; administrator and seller sign-in keeps the management redirect.
- Reject administrator and seller accounts with the existing customer-only message pattern.

### Header and cart page

- Add a `Cart` action to the storefront header for signed-in customer accounts.
- Show a badge containing the total quantity across cart lines.
- Add the protected route `/front/cart`.
- Display each line's image, product name, seller, unit price, quantity, subtotal, and availability.
- Allow quantity changes and line removal.
- Display a basket total calculated for presentation from server-returned product prices.
- Disable checkout for an empty cart or when any line is unavailable.
- After successful checkout, show a success message, refresh the cart count, and navigate to `My orders`.

## Backend Design

### Cart model and queries

Add a small cart module consisting of a cart entity/response model, mapper, service, controller, and validated request records. Cart reads join `cart_item` to `goods` and optionally `business` so the response contains current product details, stock, and merchant name. Prices and seller data are never stored in the cart table.

All cart operations derive `user_id` from the authenticated token. A caller cannot provide or override another customer's ID.

### API

- `GET /cart/items`: return the current customer's cart lines.
- `POST /cart/items`: add `{ goodsId, quantity }`; adding an existing product increments its quantity.
- `PUT /cart/items/{id}`: replace the quantity for one owned cart line.
- `DELETE /cart/items/{id}`: remove one owned cart line.
- `POST /cart/checkout`: purchase every current cart line atomically and return the created orders.

Every endpoint requires the `USER` role. Quantities must be between 1 and 99. Add and update operations validate that the product exists, is available for storefront purchase, and has enough current stock.

### Checkout transaction

1. Resolve the authenticated customer.
2. Load the customer's cart lines with current product data.
3. Reject an empty cart.
4. Validate every product and quantity before making changes.
5. For each line, decrement stock using the existing conditional stock update.
6. Create one `customer_order` row using the database product name, image, merchant, unit price, and requested quantity.
7. If any stock decrement or insert fails, roll back all stock and order changes.
8. Delete the customer's cart lines only after every order insert succeeds.
9. Return the created orders to the frontend.

The existing fulfilment and cancellation rules continue to operate on each resulting order. Cancelling an order restores the stock for that order as it does today.

## Data Model

No new migration is required because migration V4 already creates `cart_item` with:

- a unique `(user_id, goods_id)` constraint;
- foreign keys to `user` and `goods`;
- a positive-quantity check;
- timestamps for creation and update.

The implementation will not modify historical Flyway migrations. The unused `shop_order` and `order_item` tables remain untouched for migration compatibility, but the application will not use them.

## Validation and Error Handling

- Missing or inaccessible products use the existing product-not-found behavior.
- Invalid quantities return the existing parameter error.
- Insufficient stock uses the existing insufficient-stock error.
- Missing or foreign cart lines return a cart-item-not-found error without revealing ownership information.
- Empty checkout returns a specific empty-cart error.
- The frontend keeps cart contents visible after any failed checkout and shows the backend message.
- Concurrent stock changes are handled by the conditional stock update inside the checkout transaction.

## Testing

### Backend

- Adding a new cart item and incrementing an existing line
- Rejecting invalid quantities and insufficient stock
- Reading, updating, and deleting only the authenticated customer's lines
- Rejecting administrator and seller access
- Successful multi-line checkout, order creation, stock decrement, and cart clearing
- Full rollback when any line cannot be purchased
- Empty-cart checkout rejection

### Frontend

- Product detail renders the merchant name directly and has no demo copy
- `Add to cart` login, role, success, and stock behavior
- Cart header navigation and quantity badge
- Cart line rendering, quantity updates, removal, totals, disabled states, and successful checkout navigation
- Category heading and password form use the intended non-wrapping desktop structure
- Existing product, routing, order, and management tests remain green

### Final verification

- Maven `clean verify`
- Vitest suite
- ESLint
- Vite production build
- Production dependency audit

## Delivery

All implementation commits remain on `feat/cart-checkout-ui-polish`. After local verification, the branch can be pushed and opened as a pull request against `master`. The `master` branch is not changed directly.
