<template>
  <div class="main-content">
    <div class="content-container">
      <div class="goods-section">
        <div class="section-header">
          <h2 class="section-title">{{typeData.name}}</h2>
          <div class="section-divider"></div>
        </div>
        <div class="goods-grid">
          <el-row :gutter="30">
            <el-col :lg="6" :md="8" :sm="12" :xs="24" v-for="item in goodsData" :key="item.id" class="goods-item">
              <ProductCard :product="item" @select="openProduct" />
            </el-col>
          </el-row>
        </div>
      </div>
      
      <div class="recommend-section">
        <div class="section-header">
          <h2 class="section-title">Latest products</h2>
          <div class="section-divider"></div>
        </div>
        <div class="recommend-list">
          <ProductCard
            v-for="item in recommendData"
            :key="item.id"
            :product="item"
            @select="openProduct"
          />
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import ProductCard from '@/components/ProductCard.vue'

export default {
  name: 'StoreType',
  components: { ProductCard },
  data() {
    let typeId = this.$route.query.id
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      typeId: typeId,
      goodsData: [],
      recommendData: [],
      typeData: {}
    }
  },
  mounted() {
    this.loadGoods()
    this.loadType()
    this.loadRecommend()
  },
  // Click handlers and data loaders for this page.
  methods: {
    loadRecommend() {
      this.$request.get('/goods/featured').then(res => {
        if (res.code === '200') {
          this.recommendData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadType() {
      this.$request.get('/type/selectById/' + this.typeId).then(res => {
        if (res.code === '200') {
          this.typeData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    loadGoods() {
      this.$request.get('/goods/selectByTypeId?id=' + this.typeId).then(res => {
        if (res.code === '200') {
          this.goodsData = res.data
        } else {
          this.$message.error(res.msg)
        }
      })
    },
    openProduct(product) {
      this.$router.push({ name: 'ProductDetail', params: { id: product.id } })
    },
  }
}
</script>

<style scoped>
.main-content {
  background-color: #f5f7fa;
  min-height: 100vh;
  padding: 20px 0;
}

.content-container {
  display: flex;
  width: 80%;
  margin: 0 auto;
  gap: 30px;
}

.section-header {
  margin-bottom: 30px;
}

.section-title {
  font-size: 24px;
  color: #2c3e50;
  font-weight: 600;
  margin-bottom: 10px;
}

.section-divider {
  width: 50px;
  height: 3px;
  background: linear-gradient(90deg, #409EFF, #67C23A);
  border-radius: 3px;
}

.goods-section {
  flex: 1;
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.recommend-section {
  width: 300px;
  background: white;
  border-radius: 15px;
  padding: 30px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.goods-card, .recommend-item {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s ease;
  cursor: pointer;
  margin-bottom: 25px;
}

.goods-card:hover, .recommend-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.goods-image-wrapper {
  position: relative;
  padding-top: 100%;
  overflow: hidden;
  border-radius: 12px;
}

.goods-image {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.goods-info {
  padding: 15px;
}

.goods-name {
  font-size: 16px;
  color: #2c3e50;
  margin-bottom: 10px;
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.goods-price {
  display: flex;
  align-items: baseline;
}

.price-symbol {
  color: #ff6b6b;
  font-size: 14px;
  margin-right: 2px;
}

.price-value {
  color: #ff6b6b;
  font-size: 20px;
  font-weight: 600;
}

.price-unit {
  color: #999;
  font-size: 14px;
  margin-left: 5px;
}

@media screen and (max-width: 1400px) {
  .content-container {
    width: 90%;
  }
}

@media screen and (max-width: 992px) {
  .content-container {
    flex-direction: column;
  }
  
  .recommend-section {
    width: 100%;
  }
  
  .recommend-list {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: 20px;
  }
}

@media screen and (max-width: 576px) {
  .content-container {
    width: 95%;
    padding: 15px;
  }
  
  .goods-section,
  .recommend-section {
    padding: 20px;
  }
  
  .section-title {
    font-size: 20px;
  }
}
</style>
