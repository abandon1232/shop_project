<template>
  <article class="product-card">
    <button class="product-card-button" type="button" @click="$emit('select', product)">
      <span class="product-card-image">
        <img
          :src="product.img || productFallback"
          :alt="product.name"
          loading="lazy"
          @error="handleImageError"
        >
      </span>
      <span class="product-card-content">
        <span class="product-category">{{ product.typeName || 'Marketplace product' }}</span>
        <strong class="product-name">{{ product.name }}</strong>
        <span class="product-card-footer">
          <span class="product-price">{{ formatSek(product.price) }}</span>
          <span class="stock-label" :class="{ 'out-of-stock': !product.count }">
            {{ product.count ? 'In stock' : 'Out of stock' }}
          </span>
        </span>
      </span>
    </button>
  </article>
</template>

<script>
import productFallback from '@/assets/imgs/product-placeholder.webp'
import { formatSek } from '@/utils/format'
import { applyImageFallback } from '@/utils/imageFallback'

export default {
  name: 'ProductCard',
  props: {
    product: {
      type: Object,
      required: true,
    },
  },
  emits: ['select'],
  data() {
    return { productFallback }
  },
  methods: {
    formatSek,
    handleImageError(event) {
      applyImageFallback(event, productFallback)
    },
  },
}
</script>

<style scoped>
.product-card {
  height: 100%;
  overflow: hidden;
  border: 1px solid #dfe5eb;
  border-radius: 16px;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 14px 30px rgba(15, 39, 66, 0.12);
}

.product-card-button {
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  color: inherit;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.product-card-button:focus-visible {
  outline: 3px solid rgba(231, 111, 46, 0.4);
  outline-offset: -3px;
}

.product-card-image {
  display: block;
  width: 100%;
  aspect-ratio: 1;
  padding: 14px;
  background: #f4f2ed;
}

.product-card-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-card-content {
  display: flex;
  flex: 1;
  flex-direction: column;
  width: 100%;
  padding: 17px;
}

.product-category {
  color: #748091;
  font-size: 11px;
  font-weight: 800;
  letter-spacing: 0.08em;
  text-transform: uppercase;
}

.product-name {
  min-height: 45px;
  margin: 7px 0 18px;
  color: #142033;
  font-size: 16px;
  line-height: 1.4;
}

.product-card-footer {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 12px;
  margin-top: auto;
}

.product-price {
  color: #c94f13;
  font-size: 20px;
  font-weight: 800;
}

.stock-label {
  color: #26734d;
  font-size: 12px;
  font-weight: 700;
}

.stock-label.out-of-stock {
  color: #9b3b36;
}
</style>
