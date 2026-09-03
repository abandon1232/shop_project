<template>
  <main class="customer-orders-page">
    <header class="orders-heading">
      <div>
        <span class="eyebrow">Your account</span>
        <h1>My orders</h1>
        <p>Follow the demo orders you have placed through NorrByte Market.</p>
      </div>
      <button type="button" @click="$router.push('/front/home')">Continue shopping</button>
    </header>

    <section v-if="loading" class="orders-panel loading-panel">Loading your orders…</section>

    <section v-else-if="orders.length" class="orders-list" aria-label="Order history">
      <article v-for="order in orders" :key="order.id" class="order-card">
        <button
          type="button"
          class="product-link order-product-image-link"
          :aria-label="`View ${order.productName}`"
          @click="openProduct(order)"
        >
          <img
            :src="order.productImg || productFallback"
            :alt="order.productName"
            @error="handleImageError"
          >
        </button>
        <div class="order-main">
          <div class="order-number">{{ order.orderNumber }}</div>
          <h2>
            <button type="button" class="product-link order-product-name-link" @click="openProduct(order)">
              {{ order.productName }}
            </button>
          </h2>
          <p>Quantity {{ order.quantity }} · {{ formatDate(order.createdAt) }}</p>
        </div>
        <div class="order-summary">
          <strong>{{ formatSek(order.totalPrice) }}</strong>
          <span class="status-pill" :class="`status-${orderStatusTone(order.status)}`">
            {{ orderStatusLabel(order.status) }}
          </span>
        </div>
      </article>

      <el-pagination
        v-if="total > pageSize"
        background
        :current-page="pageNum"
        :page-size="pageSize"
        layout="prev, pager, next"
        :total="total"
        @current-change="load"
      />
    </section>

    <section v-else class="orders-panel empty-orders">
      <span aria-hidden="true">□</span>
      <h2>No orders yet</h2>
      <p>Products you buy will appear here with their current fulfilment status.</p>
      <button type="button" @click="$router.push('/front/home')">Browse products</button>
    </section>
  </main>
</template>

<script>
import productFallback from '@/assets/imgs/product-placeholder.webp'
import { nextOrderStatuses, orderStatusLabel, orderStatusTone } from '@/constants/orderStatus'
import { formatSek } from '@/utils/format'
import { applyImageFallback } from '@/utils/imageFallback'

const dateFormatter = new Intl.DateTimeFormat('en-SE', { dateStyle: 'medium' })

export default {
  name: 'CustomerOrders',
  data() {
    return {
      orders: [],
      total: 0,
      pageNum: 1,
      pageSize: 10,
      loading: true,
      productFallback,
    }
  },
  created() {
    this.load(1)
  },
  methods: {
    formatSek,
    orderStatusLabel,
    orderStatusTone,
    nextOrderStatuses,
    formatDate(value) {
      const date = new Date(value)
      return Number.isNaN(date.getTime()) ? 'Date unavailable' : dateFormatter.format(date)
    },
    handleImageError(event) {
      applyImageFallback(event, productFallback)
    },
    openProduct(order) {
      this.$router.push({ name: 'ProductDetail', params: { id: order.goodsId } })
    },
    load(pageNum) {
      this.pageNum = pageNum || 1
      this.loading = true
      return this.$request.get('/orders/selectPage', {
        params: { pageNum: this.pageNum, pageSize: this.pageSize },
      }).then(res => {
        if (res.code === '200') {
          this.orders = res.data?.list || []
          this.total = res.data?.total || 0
        } else {
          this.$message.error(res.msg)
        }
      }).finally(() => {
        this.loading = false
      })
    },
  },
}
</script>

<style scoped>
.customer-orders-page {
  min-height: 100vh;
  padding: 42px max(22px, calc((100vw - 1180px) / 2)) 72px;
  background: #f3f5f7;
  color: #142033;
}

.orders-heading {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
  max-width: 1100px;
  margin: 0 auto 24px;
}

.eyebrow {
  color: #c94f13;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.orders-heading h1 {
  margin: 7px 0;
  font-size: 38px;
  letter-spacing: -0.035em;
}

.orders-heading p,
.order-main p,
.empty-orders p {
  color: #687586;
}

.orders-heading button,
.empty-orders button {
  padding: 11px 17px;
  border: 1px solid #d4dbe2;
  border-radius: 10px;
  background: #fff;
  color: #142033;
  font: inherit;
  font-weight: 750;
  cursor: pointer;
}

.orders-list,
.orders-panel {
  max-width: 1100px;
  margin: 0 auto;
}

.orders-list {
  display: grid;
  gap: 14px;
}

.order-card {
  display: grid;
  grid-template-columns: 112px minmax(0, 1fr) auto;
  align-items: center;
  gap: 22px;
  padding: 18px;
  border: 1px solid #dce2e8;
  border-radius: 16px;
  background: #fff;
}

.order-card img {
  width: 112px;
  height: 92px;
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

.order-product-image-link {
  border-radius: 10px;
}

.order-product-image-link img {
  display: block;
}

.order-product-name-link {
  font-weight: 700;
}

.order-number {
  color: #758293;
  font-size: 12px;
  font-weight: 750;
}

.order-main h2 {
  margin: 6px 0;
  font-size: 18px;
}

.order-main p {
  margin: 0;
  font-size: 13px;
}

.order-summary {
  display: grid;
  justify-items: end;
  gap: 10px;
}

.order-summary strong {
  color: #c94f13;
  font-size: 20px;
}

.status-pill {
  padding: 5px 10px;
  border-radius: 999px;
  background: #e9edf2;
  color: #4f6074;
  font-size: 12px;
  font-weight: 750;
}

.status-warning { background: #fff0d9; color: #89510e; }
.status-primary { background: #e3edf8; color: #275b8a; }
.status-success { background: #deefe6; color: #26734d; }
.status-danger { background: #f7e2e0; color: #9b3b36; }

.orders-panel {
  padding: 54px;
  border: 1px solid #dce2e8;
  border-radius: 18px;
  background: #fff;
  text-align: center;
}

.empty-orders > span {
  color: #d2d9e0;
  font-size: 56px;
}

@media (max-width: 680px) {
  .orders-heading {
    align-items: flex-start;
    flex-direction: column;
  }

  .order-card {
    grid-template-columns: 84px 1fr;
  }

  .order-card img {
    width: 84px;
    height: 76px;
  }

  .order-summary {
    grid-column: 1 / -1;
    grid-template-columns: 1fr 1fr;
    justify-items: start;
  }
}
</style>
