<template>
  <main class="customer-cart-page">
    <header class="cart-heading">
      <div>
        <span class="eyebrow">Your account</span>
        <h1>Your cart</h1>
        <p>Review your items before placing your orders.</p>
      </div>
      <button type="button" @click="$router.push('/front/home')">Continue shopping</button>
    </header>

    <section v-if="loading" class="cart-panel loading-panel" aria-label="Loading cart">Loading your cart…</section>

    <section v-else-if="!items.length" class="cart-panel empty-cart">
      <span aria-hidden="true">□</span>
      <h2>Your cart is empty</h2>
      <p>Add products from the store to place an order.</p>
      <button type="button" @click="$router.push('/front/home')">Continue shopping</button>
    </section>

    <section v-else class="cart-layout">
      <div class="cart-lines" aria-label="Cart items">
        <article v-for="item in items" :key="item.id" class="cart-line">
          <button
            type="button"
            class="product-link cart-product-image-link"
            :aria-label="`View ${item.productName}`"
            @click="openProduct(item)"
          >
            <img :src="item.productImg || productFallback" :alt="item.productName" @error="handleImageError">
          </button>
          <div class="cart-line-main">
            <span class="cart-seller">{{ item.businessName || 'NorrByte Market' }}</span>
            <h2>
              <button type="button" class="product-link cart-product-name-link" @click="openProduct(item)">
                {{ item.productName }}
              </button>
            </h2>
            <strong>{{ formatSek(item.unitPrice) }}</strong>
            <p v-if="item.quantity > item.stock" class="availability unavailable">Only {{ item.stock }} available</p>
            <p v-else-if="item.businessStatus && item.businessStatus !== 'APPROVED'" class="availability unavailable">Seller is unavailable</p>
            <p v-else class="availability">{{ item.stock }} in stock</p>
          </div>
          <div class="cart-line-actions">
            <label :for="`cart-quantity-${item.id}`">Quantity</label>
            <el-input-number
              :id="`cart-quantity-${item.id}`"
              :model-value="item.quantity"
              :min="1"
              :max="99"
              :disabled="updatingIds.includes(item.id)"
              @change="updateQuantity(item, $event)"
            />
            <strong class="cart-line-subtotal">{{ formatSek(Number(item.unitPrice || 0) * Number(item.quantity || 0)) }}</strong>
            <button type="button" :disabled="updatingIds.includes(item.id)" @click="removeItem(item)">Remove</button>
          </div>
        </article>
      </div>

      <aside class="cart-summary">
        <h2>Order summary</h2>
        <div><span>Items</span><strong>{{ items.reduce((sum, item) => sum + Number(item.quantity || 0), 0) }}</strong></div>
        <div class="cart-total"><span>Total</span><strong>{{ formatSek(total) }}</strong></div>
        <p v-if="hasUnavailableItems" class="unavailable">Remove or update unavailable items before checkout.</p>
        <el-button class="checkout-button" type="primary" :disabled="!canCheckout" :loading="checkingOut" @click="checkout">Checkout</el-button>
      </aside>
    </section>
  </main>
</template>

<script>
import productFallback from '@/assets/imgs/product-placeholder.webp'
import { formatSek } from '@/utils/format'
import { applyImageFallback } from '@/utils/imageFallback'

export default {
  name: 'CustomerCart',
  data() {
    return {
      items: [],
      loading: true,
      checkingOut: false,
      updatingIds: [],
      productFallback,
    }
  },
  computed: {
    total() {
      return this.items.reduce(
        (sum, item) => sum + Number(item.unitPrice || 0) * Number(item.quantity || 0),
        0,
      )
    },
    hasUnavailableItems() {
      return this.items.some(item =>
        item.quantity > item.stock
        || (item.businessStatus && item.businessStatus !== 'APPROVED'),
      )
    },
    canCheckout() {
      return this.items.length > 0 && !this.hasUnavailableItems && !this.checkingOut
    },
  },
  created() {
    this.loadCart()
  },
  methods: {
    formatSek,
    handleImageError(event) {
      applyImageFallback(event, productFallback)
    },
    openProduct(item) {
      this.$router.push({ name: 'ProductDetail', params: { id: item.goodsId } })
    },
    loadCart() {
      this.loading = true
      return this.$request.get('/cart/items').then(res => {
        if (res.code === '200') {
          this.items = res.data || []
          this.$emit('cart-updated')
        } else {
          this.$message.error(res.msg)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    async updateQuantity(item, quantity) {
      if (this.updatingIds.includes(item.id)) return

      this.updatingIds.push(item.id)
      try {
        const res = await this.$request.put(`/cart/items/${item.id}`, { quantity })
        if (res.code === '200') {
          await this.loadCart()
        } else {
          this.$message.error(res.msg)
        }
      } finally {
        this.updatingIds = this.updatingIds.filter(id => id !== item.id)
      }
    },
    async removeItem(item) {
      if (this.updatingIds.includes(item.id)) return

      this.updatingIds.push(item.id)
      try {
        const res = await this.$request.delete(`/cart/items/${item.id}`)
        if (res.code === '200') {
          await this.loadCart()
        } else {
          this.$message.error(res.msg)
        }
      } finally {
        this.updatingIds = this.updatingIds.filter(id => id !== item.id)
      }
    },
    async checkout() {
      if (!this.canCheckout) return

      this.checkingOut = true
      try {
        const res = await this.$request.post('/cart/checkout')
        if (res.code === '200') {
          this.$message.success(`Checkout complete: ${(res.data || []).length} orders placed`)
          this.$emit('cart-updated')
          this.$router.push('/front/orders')
        } else {
          this.$message.error(res.msg)
        }
      } finally {
        this.checkingOut = false
      }
    },
  },
}
</script>

<style scoped>
.customer-cart-page {
  min-height: 100vh;
  padding: 42px max(22px, calc((100vw - 1180px) / 2)) 72px;
  background: #f3f5f7;
  color: #142033;
}

.cart-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto 24px;
}

.eyebrow,
.cart-seller {
  color: #c94f13;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.cart-heading h1 {
  margin: 7px 0;
  font-size: 38px;
  letter-spacing: -0.035em;
}

.cart-heading p,
.availability,
.empty-cart p {
  color: #687586;
}

.cart-heading button,
.empty-cart button,
.cart-line-actions button {
  padding: 9px 13px;
  border: 1px solid #d4dbe2;
  border-radius: 9px;
  background: #fff;
  color: #142033;
  font: inherit;
  font-weight: 750;
  cursor: pointer;
}

.cart-layout,
.cart-panel {
  max-width: 1100px;
  margin: 0 auto;
}

.cart-layout {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 300px;
  align-items: start;
  gap: 20px;
}

.cart-lines {
  display: grid;
  gap: 14px;
}

.cart-line,
.cart-summary,
.cart-panel {
  border: 1px solid #dce2e8;
  border-radius: 16px;
  background: #fff;
}

.cart-line {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr) auto;
  align-items: center;
  gap: 20px;
  padding: 18px;
}

.cart-line img {
  width: 120px;
  height: 96px;
  border-radius: 10px;
  background: #f4f2ed;
  object-fit: contain;
}

.product-link {
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.product-link:focus-visible {
  border-radius: 5px;
  outline: 3px solid rgba(64, 158, 255, 0.35);
  outline-offset: 3px;
}

.cart-product-image-link {
  border-radius: 10px;
}

.cart-product-image-link img {
  display: block;
}

.cart-product-name-link {
  font-weight: 700;
}

.cart-line-main h2 {
  margin: 7px 0;
  font-size: 18px;
}

.cart-line-main > strong,
.cart-line-subtotal,
.cart-total strong {
  color: #c94f13;
}

.availability {
  margin: 8px 0 0;
  font-size: 13px;
}

.unavailable {
  color: #9b3b36 !important;
}

.cart-line-actions {
  display: grid;
  justify-items: end;
  gap: 8px;
}

.cart-line-actions label {
  color: #687586;
  font-size: 12px;
  font-weight: 750;
}

.cart-summary {
  display: grid;
  gap: 16px;
  padding: 22px;
}

.cart-summary h2 {
  margin: 0;
}

.cart-summary > div {
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.cart-total {
  padding-top: 16px;
  border-top: 1px solid #e2e7ec;
  font-size: 19px;
}

.checkout-button {
  min-height: 43px;
  border-color: #e76f2e;
  background: #e76f2e;
  font-weight: 800;
}

.cart-summary p {
  margin: 0;
  font-size: 13px;
}

.cart-panel {
  padding: 54px;
  text-align: center;
}

.empty-cart > span {
  color: #d2d9e0;
  font-size: 56px;
}

@media (max-width: 800px) {
  .cart-layout {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 680px) {
  .cart-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .cart-line {
    grid-template-columns: 84px minmax(0, 1fr);
  }

  .cart-line img {
    width: 84px;
    height: 76px;
  }

  .cart-line-actions {
    grid-column: 1 / -1;
    grid-template-columns: auto 1fr auto auto;
    align-items: center;
    justify-items: start;
  }
}
</style>
