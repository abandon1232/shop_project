<template>
  <main class="store-home">
    <section class="hero-section" aria-label="Store highlights">
      <el-carousel class="hero-carousel" height="430px" :interval="6500">
        <el-carousel-item v-for="slide in heroSlides" :key="slide.title">
          <div class="hero-slide">
            <img :src="slide.image" :alt="slide.imageAlt">
            <div class="hero-shade"></div>
            <div class="hero-copy" :class="slide.copyPosition">
              <span class="eyebrow">{{ slide.eyebrow }}</span>
              <h1>{{ slide.title }}</h1>
              <p>{{ slide.description }}</p>
              <el-button class="hero-button" @click="scrollToProducts">Explore products</el-button>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>

    <section class="trust-strip" aria-label="Marketplace benefits">
      <div>
        <strong>Clear catalogue</strong>
        <span>Compare products, categories and stock in one place.</span>
      </div>
      <div>
        <strong>Approved sellers</strong>
        <span>Products are published by reviewed marketplace sellers.</span>
      </div>
      <div>
        <strong>Built for Sweden</strong>
        <span>Prices are presented in Swedish kronor.</span>
      </div>
    </section>

    <section class="catalogue-layout">
      <aside class="category-panel">
        <div class="panel-heading category-heading">
          <span class="eyebrow">Browse</span>
          <h2 class="section-title">Shop by category</h2>
        </div>
        <div v-if="typeData.length" class="category-list">
          <button
            v-for="item in typeData"
            :key="item.id"
            class="category-item"
            type="button"
            @click="navTo('/front/type?id=' + item.id)"
          >
            <img :src="item.img || productPlaceholder" :alt="item.name" @error="handleImageError">
            <span>{{ item.name }}</span>
            <span aria-hidden="true">→</span>
          </button>
        </div>
        <p v-else class="empty-copy">Categories will appear here when they are available.</p>
      </aside>

      <section id="featured-products" class="product-panel">
        <div class="section-header">
          <div>
            <span class="eyebrow">New in the catalogue</span>
            <h2>Featured products</h2>
          </div>
          <span class="section-note">From approved sellers</span>
        </div>

        <div v-if="featuredData.length" class="products-grid">
          <ProductCard
            v-for="item in featuredData"
            :key="item.id"
            :product="item"
            @select="openProduct"
          />
        </div>
        <div v-else class="product-empty">
          <img :src="productPlaceholder" alt="Generic wireless speaker">
          <div>
            <h3>The catalogue is being prepared</h3>
            <p>Featured products will appear here after an approved seller publishes them.</p>
          </div>
        </div>
      </section>
    </section>
  </main>
</template>

<script>
import heroHome from '@/assets/imgs/hero-home.webp'
import heroWorkspace from '@/assets/imgs/hero-workspace.webp'
import productPlaceholder from '@/assets/imgs/product-placeholder.webp'
import ProductCard from '@/components/ProductCard.vue'
import { applyImageFallback } from '@/utils/imageFallback'

export default {
  name: 'StoreHome',
  components: { ProductCard },
  data() {
    return {
      typeData: [],
      featuredData: [],
      productPlaceholder,
      heroSlides: [
        {
          eyebrow: 'Everyday technology',
          title: 'A calmer setup for work and study',
          description: 'Browse practical electronics for a focused Scandinavian workspace.',
          image: heroWorkspace,
          imageAlt: 'Laptop, headphones and desk accessories in a bright home workspace',
          copyPosition: 'copy-left',
        },
        {
          eyebrow: 'Made for home',
          title: 'Simple upgrades for every room',
          description: 'Discover useful products from approved sellers in one clear catalogue.',
          image: heroHome,
          imageAlt: 'Television and audio equipment in a Scandinavian living room',
          copyPosition: 'copy-right',
        },
      ],
    }
  },
  mounted() {
    this.loadType()
    this.loadFeatured()
  },
  methods: {
    handleImageError(event) {
      applyImageFallback(event, productPlaceholder)
    },
    loadFeatured() {
      this.$request.get('/goods/featured').then(res => {
        if (res.code === '200') {
          this.featuredData = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadType() {
      this.$request.get('/type/selectAll').then(res => {
        if (res.code === '200') {
          this.typeData = res.data || []
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    navTo(url) {
      this.$router.push(url)
    },
    openProduct(product) {
      this.$router.push({ name: 'ProductDetail', params: { id: product.id } })
    },
    scrollToProducts() {
      document.getElementById('featured-products')?.scrollIntoView({ behavior: 'smooth' })
    },
  },
}
</script>

<style scoped>
.store-home {
  min-height: 100vh;
  padding: 28px max(24px, calc((100vw - 1440px) / 2)) 72px;
  background: #f3f5f7;
  color: #142033;
}

.hero-section,
.trust-strip,
.catalogue-layout {
  max-width: 1380px;
  margin: 0 auto;
}

.hero-carousel {
  overflow: hidden;
  border-radius: 24px;
  box-shadow: 0 20px 50px rgba(12, 32, 56, 0.14);
}

.hero-slide {
  position: relative;
  height: 430px;
  overflow: hidden;
  background: #13243b;
}

.hero-slide > img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.hero-shade {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, rgba(9, 28, 51, 0.88) 0%, rgba(9, 28, 51, 0.5) 35%, rgba(9, 28, 51, 0.06) 68%);
}

.hero-copy {
  position: absolute;
  top: 50%;
  width: min(480px, 44%);
  transform: translateY(-50%);
  color: #fff;
}

.copy-left {
  left: 7%;
}

.copy-right {
  right: 7%;
  padding: 28px;
  border-radius: 18px;
  background: rgba(9, 28, 51, 0.82);
  backdrop-filter: blur(6px);
}

.eyebrow {
  color: #e76f2e;
  font-size: 12px;
  font-weight: 800;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.hero-copy h1 {
  max-width: 620px;
  margin: 12px 0 14px;
  font-size: clamp(34px, 4vw, 58px);
  line-height: 1.03;
  letter-spacing: -0.04em;
}

.hero-copy p {
  max-width: 470px;
  margin-bottom: 24px;
  color: rgba(255, 255, 255, 0.84);
  font-size: 17px;
  line-height: 1.55;
}

.hero-button {
  border: 0;
  background: #e76f2e;
  color: #fff;
  font-weight: 700;
}

.trust-strip {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  margin-top: 18px;
  overflow: hidden;
  border: 1px solid #dce2e8;
  border-radius: 16px;
  background: #fff;
}

.trust-strip div {
  display: grid;
  gap: 5px;
  padding: 20px 28px;
}

.trust-strip div + div {
  border-left: 1px solid #e6e9ed;
}

.trust-strip strong {
  color: #13243b;
  font-size: 15px;
}

.trust-strip span,
.empty-copy,
.section-note,
.product-empty p {
  color: #687586;
  line-height: 1.5;
}

.catalogue-layout {
  display: grid;
  grid-template-columns: 270px minmax(0, 1fr);
  gap: 22px;
  margin-top: 22px;
}

.category-panel,
.product-panel {
  border: 1px solid #dce2e8;
  border-radius: 18px;
  background: #fff;
}

.category-panel {
  align-self: start;
  padding: 24px;
}

.panel-heading,
.section-header {
  margin-bottom: 20px;
}

.category-heading {
  display: grid;
  gap: 7px;
}

.section-title,
.section-header h2 {
  margin-top: 7px;
  color: #13243b;
  font-size: 25px;
  letter-spacing: -0.025em;
}

.category-heading .section-title {
  margin-top: 0;
  white-space: nowrap;
}

.category-list {
  display: grid;
  gap: 8px;
}

.category-item {
  display: grid;
  grid-template-columns: 38px 1fr auto;
  align-items: center;
  gap: 12px;
  width: 100%;
  padding: 10px;
  border: 0;
  border-radius: 10px;
  background: transparent;
  color: #28384d;
  font: inherit;
  text-align: left;
  cursor: pointer;
}

.category-item:hover {
  background: #eef2f6;
}

.category-item img {
  width: 38px;
  height: 38px;
  border-radius: 9px;
  object-fit: cover;
}

.product-panel {
  padding: 28px;
}

.section-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  gap: 24px;
}

.products-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(210px, 1fr));
  gap: 16px;
}

.product-card {
  overflow: hidden;
  border: 1px solid #e0e5ea;
  border-radius: 14px;
  background: #fff;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 12px 28px rgba(15, 39, 66, 0.11);
}

.product-image {
  aspect-ratio: 4 / 3;
  padding: 16px;
  background: #f4f6f8;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.product-info {
  padding: 17px;
}

.product-kicker {
  color: #778393;
  font-size: 11px;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
}

.product-name {
  min-height: 44px;
  margin: 7px 0 14px;
  color: #142033;
  font-size: 16px;
  line-height: 1.4;
}

.product-price {
  display: flex;
  align-items: baseline;
  gap: 5px;
}

.amount {
  color: #c94f13;
  font-size: 20px;
  font-weight: 800;
}

.unit {
  color: #7b8693;
  font-size: 12px;
}

.product-empty {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 24px;
  border-radius: 14px;
  background: #f4f6f8;
}

.product-empty img {
  width: 140px;
  height: 110px;
  object-fit: contain;
}

.product-empty h3 {
  margin-bottom: 8px;
}

@media (max-width: 900px) {
  .hero-copy {
    width: 58%;
  }

  .trust-strip,
  .catalogue-layout {
    grid-template-columns: 1fr;
  }

  .trust-strip div + div {
    border-top: 1px solid #e6e9ed;
    border-left: 0;
  }
}

@media (max-width: 600px) {
  .store-home {
    padding: 14px 14px 48px;
  }

  .hero-carousel,
  .hero-slide {
    height: 420px !important;
  }

  .hero-slide > img {
    opacity: 0.68;
  }

  .hero-copy,
  .copy-left,
  .copy-right {
    right: 24px;
    left: 24px;
    width: auto;
    padding: 0;
    background: transparent;
  }

  .hero-copy h1 {
    font-size: 36px;
  }

  .section-header,
  .product-empty {
    align-items: flex-start;
    flex-direction: column;
  }

  .product-panel,
  .category-panel {
    padding: 20px;
  }
}
</style>
