<template>
  <main class="product-detail-page">
    <button class="back-link" type="button" @click="$router.push('/front/home')">← Back to store</button>

    <section v-if="loading" class="detail-shell loading-shell" aria-label="Loading product">
      <el-skeleton :rows="8" animated />
    </section>

    <section v-else-if="product" class="detail-shell">
      <div class="detail-image-panel">
        <img
          :src="product.img || productFallback"
          :alt="product.name"
          @error="handleImageError"
        >
      </div>

      <div class="detail-content">
        <span class="detail-category">{{ product.typeName || 'Marketplace product' }}</span>
        <h1>{{ product.name }}</h1>
        <div class="detail-price">{{ formatSek(product.price) }}</div>

        <div class="detail-meta">
          <div>
            <span>Sold by</span>
            <strong>{{ product.businessName || 'NorrByte Market' }}</strong>
          </div>
          <div>
            <span>Availability</span>
            <strong :class="{ unavailable: !product.count }">
              {{ product.count ? `${product.count} in stock` : 'Out of stock' }}
            </strong>
          </div>
        </div>

        <p class="detail-description">{{ product.description || 'Product details will be added soon.' }}</p>

        <div class="purchase-panel">
          <label for="product-quantity">Quantity</label>
          <el-input-number
            id="product-quantity"
            v-model="quantity"
            :min="1"
            :max="Math.max(product.count || 1, 1)"
            :disabled="!product.count"
          />
          <el-button
            class="buy-button"
            type="primary"
            size="large"
            :disabled="!product.count"
            @click="buyNow"
          >
            Buy now
          </el-button>
        </div>

        <p class="purchase-note">Demo checkout · No payment details are collected</p>
      </div>
    </section>

    <section v-else class="detail-shell empty-detail">
      <h1>Product unavailable</h1>
      <p>This product could not be found in the current catalogue.</p>
    </section>
  </main>
</template>

<script>
import productFallback from '@/assets/imgs/product-placeholder.webp'
import { formatSek } from '@/utils/format'
import { applyImageFallback } from '@/utils/imageFallback'

export default {
  name: 'ProductDetail',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      product: null,
      productFallback,
      quantity: 1,
      loading: true,
    }
  },
  mounted() {
    this.loadProduct()
  },
  methods: {
    formatSek,
    handleImageError(event) {
      applyImageFallback(event, productFallback)
    },
    loadProduct() {
      this.loading = true
      return this.$request.get('/goods/selectById', {
        params: { id: this.$route.params.id },
      }).then(res => {
        if (res.code === '200') {
          this.product = res.data
          this.quantity = 1
        } else {
          this.product = null
          this.$message.error(res.msg)
        }
      }).finally(() => {
        this.loading = false
      })
    },
    buyNow() {
      if (!this.user.token) {
        return this.$router.push({
          path: '/login',
          query: { redirect: this.$route.fullPath },
        })
      }
      return undefined
    },
  },
}
</script>

<style scoped>
.product-detail-page {
  min-height: 100vh;
  padding: 34px max(22px, calc((100vw - 1280px) / 2)) 72px;
  background: #f3f5f7;
  color: #142033;
}

.back-link {
  margin-bottom: 20px;
  padding: 0;
  border: 0;
  background: transparent;
  color: #4f6074;
  font: inherit;
  font-weight: 700;
  cursor: pointer;
}

.back-link:hover {
  color: #c94f13;
}

.detail-shell {
  display: grid;
  grid-template-columns: minmax(0, 1.05fr) minmax(360px, 0.95fr);
  max-width: 1180px;
  margin: 0 auto;
  overflow: hidden;
  border: 1px solid #dce2e8;
  border-radius: 24px;
  background: #fff;
  box-shadow: 0 22px 52px rgba(15, 39, 66, 0.1);
}

.loading-shell,
.empty-detail {
  display: block;
  padding: 52px;
}

.detail-image-panel {
  display: grid;
  min-height: 560px;
  padding: 42px;
  place-items: center;
  background: #f4f2ed;
}

.detail-image-panel img {
  width: 100%;
  height: 100%;
  max-height: 520px;
  object-fit: contain;
}

.detail-content {
  display: flex;
  flex-direction: column;
  padding: 58px 52px;
}

.detail-category {
  color: #c94f13;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.detail-content h1 {
  margin: 12px 0 18px;
  font-size: clamp(32px, 4vw, 48px);
  line-height: 1.08;
  letter-spacing: -0.04em;
}

.detail-price {
  color: #c94f13;
  font-size: 30px;
  font-weight: 850;
}

.detail-meta {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin: 30px 0;
  padding: 20px 0;
  border-top: 1px solid #e3e7eb;
  border-bottom: 1px solid #e3e7eb;
}

.detail-meta div {
  display: grid;
  gap: 5px;
}

.detail-meta span {
  color: #748091;
  font-size: 12px;
  font-weight: 700;
  text-transform: uppercase;
}

.unavailable {
  color: #9b3b36;
}

.detail-description {
  color: #58677a;
  font-size: 16px;
  line-height: 1.75;
}

.purchase-panel {
  display: grid;
  grid-template-columns: auto 150px 1fr;
  align-items: center;
  gap: 14px;
  margin-top: auto;
  padding-top: 34px;
}

.purchase-panel label {
  font-weight: 750;
}

.buy-button {
  min-height: 42px;
  border-color: #e76f2e;
  background: #e76f2e;
  font-weight: 800;
}

.purchase-note {
  margin-top: 14px;
  color: #7c8794;
  font-size: 12px;
  text-align: right;
}

@media (max-width: 820px) {
  .detail-shell {
    grid-template-columns: 1fr;
  }

  .detail-image-panel {
    min-height: 360px;
  }

  .detail-content {
    padding: 36px 28px;
  }
}

@media (max-width: 520px) {
  .product-detail-page {
    padding: 20px 14px 48px;
  }

  .purchase-panel {
    grid-template-columns: 1fr;
  }

  .purchase-note {
    text-align: left;
  }
}
</style>
