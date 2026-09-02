<template>
  <div class="management-page dashboard-page">
    <header class="dashboard-welcome">
      <div>
        <span class="page-eyebrow">{{ isAdmin ? 'Business overview' : 'Store overview' }}</span>
        <h1>{{ greeting }}, {{ user.name || user.username }}</h1>
        <p>{{ isAdmin ? 'Here is what is happening across NorrByte Market.' : 'Track your catalogue, fulfilment and sales in one place.' }}</p>
      </div>
      <button type="button" class="primary-action" @click="$router.push('/goods')">{{ isAdmin ? 'Manage products' : 'Add a product' }}</button>
    </header>

    <section v-if="loading" class="dashboard-loading">Loading dashboard…</section>
    <section v-else class="metric-grid" aria-label="Business metrics">
      <article v-for="metric in visibleMetrics" :key="metric.key" class="metric-card">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.currency ? formatSek(summary[metric.key]) : summary[metric.key] || 0 }}</strong>
        <small>{{ metric.note }}</small>
      </article>
    </section>

    <section class="dashboard-lower-grid">
      <article class="dashboard-panel">
        <div class="panel-heading"><div><span class="page-eyebrow">Next steps</span><h2>Keep the shop moving</h2></div></div>
        <div class="quick-actions">
          <button type="button" @click="$router.push('/orders')"><span>Review orders</span><small>Process new purchases and update delivery progress.</small></button>
          <button type="button" @click="$router.push('/goods')"><span>Check stock</span><small>Restock products before they become unavailable.</small></button>
          <button v-if="isAdmin" type="button" @click="$router.push('/business')"><span>Review sellers</span><small>Approve pending stores and keep the marketplace trusted.</small></button>
        </div>
      </article>

      <article v-if="isAdmin" class="dashboard-panel">
        <div class="panel-heading">
          <div><span class="page-eyebrow">Updates</span><h2>Latest notices</h2></div>
          <button type="button" class="text-action" @click="$router.push('/notice')">Manage</button>
        </div>
        <div v-if="notices.length" class="notice-list">
          <div v-for="item in notices.slice(0, 4)" :key="item.id"><span aria-hidden="true"></span><div><strong>{{ item.title }}</strong><small>{{ item.time }}</small></div></div>
        </div>
        <p v-else class="empty-copy">No notices have been published yet.</p>
      </article>
    </section>
  </div>
</template>

<script>
import { formatSek } from '@/utils/format'

export default {
  name: 'ManagerHome',
  data() {
    return {
      user: JSON.parse(localStorage.getItem('xm-user') || '{}'),
      summary: {},
      notices: [],
      loading: true,
    }
  },
  computed: {
    isAdmin() {
      return this.user.role === 'ADMIN'
    },
    greeting() {
      const hour = new Date().getHours()
      if (hour < 12) return 'Good morning'
      if (hour < 18) return 'Good afternoon'
      return 'Good evening'
    },
    visibleMetrics() {
      const metrics = [
        { key: 'revenue', label: 'Revenue', note: 'Excludes cancelled orders', currency: true },
        { key: 'orders', label: 'Orders', note: 'All recorded purchases' },
        { key: 'products', label: 'Products', note: 'Products in the catalogue' },
        { key: 'lowStockProducts', label: 'Low stock', note: 'Five items or fewer' },
      ]
      if (this.isAdmin) {
        metrics.push(
          { key: 'categories', label: 'Categories', note: 'Storefront departments' },
          { key: 'customers', label: 'Customers', note: 'Registered shoppers' },
          { key: 'sellers', label: 'Sellers', note: 'Marketplace businesses' },
        )
      }
      return metrics
    },
  },
  created() {
    this.loadDashboard()
    if (this.isAdmin) this.loadNotices()
  },
  methods: {
    formatSek,
    loadDashboard() {
      return this.$request.get('/dashboard/summary').then(res => {
        if (res.code === '200') this.summary = res.data || {}
        else this.$message.error(res.msg)
      }).finally(() => { this.loading = false })
    },
    loadNotices() {
      return this.$request.get('/notice/selectAll').then(res => {
        if (res.code === '200') this.notices = res.data || []
      })
    },
  },
}
</script>
